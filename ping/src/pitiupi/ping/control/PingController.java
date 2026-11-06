package pitiupi.chat.control;

import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.chat.net.PingMessage;
import pitiupi.chat.net.PongMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.PeerIpv4;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PingController {
    final ConnectionManager connectionManager;
    PingLogSink pingLogSink;

    final Map<String, PingData>  pendingPings;

    Timer continuousTimer;
    Timer collectorTimer;

    static final int CONTINUOUS_INTERVAL_MS = 1000;
    static final int COLLECTOR_INTERVAL_MS = 5000;

    public PingController(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;

        pendingPings = new ConcurrentHashMap<>();

        configureCollector();
        collectorTimer.start();
    }

    private void configureCollector() {
        collectorTimer = new Timer(COLLECTOR_INTERVAL_MS, e -> {
            long currentTime = System.currentTimeMillis();

            for(String key : pendingPings.keySet()) {
                PingData pingData = pendingPings.get(key);
                if (currentTime - pingData.sentTime() > COLLECTOR_INTERVAL_MS) {
                    pingLogSink.appendFailure(pingData.receiverIp());
                    pendingPings.remove(key);
                }
            }
        });
    }

    public void setPingLogSink(PingLogSink pingLogSink) {
        this.pingLogSink = pingLogSink;
    }

    private PingMessage generatePingMessage(boolean isBroadcast) {
        String pingId = UUID.randomUUID().toString();
        long millis = System.currentTimeMillis();

        String userName = connectionManager.getUserName();
        String userIp = LocalNetworkInfo.bestLocalIpv4();

        return new PingMessage(pingId, millis, userName, userIp, isBroadcast);
    }

    public void sendPing(String receiverIp) {
        try {
            PingMessage pingMessage = generatePingMessage(false);

            String normalizedReceiverIp = PeerIpv4.normalize(receiverIp);
            InetAddress receiverAddress = InetAddress.getByName(normalizedReceiverIp);
            MessageDispatcher.send(connectionManager, pingMessage, SendTarget.singlePeer(receiverAddress), TransportMode.UNICAST_UDP);

            PingData pingData = new PingData(receiverIp, pingMessage.getSentAtMillis());
            pendingPings.put(pingMessage.getPingId(), pingData);
        }
        catch (UnknownHostException ex) {
            pingLogSink.appendError("Falha em conseguir o InetAdress do remetente");
        }
    }

    public void sendContinuousPing(String receiverIp) {
        if (continuousTimer != null) {
            pingLogSink.appendError("Você já está enviando ping contínuos para outro usuário");
            return;
        }
        continuousTimer = new Timer(CONTINUOUS_INTERVAL_MS, e -> {
            sendPing(receiverIp);
        });
        continuousTimer.start();
    }

    public void stopContinuousPing() {
        if (continuousTimer != null) {
            continuousTimer.stop();
            continuousTimer = null;
        }
    }

    public void pingAll() {
        PingMessage pingMessage = generatePingMessage(true);

        MessageDispatcher.send(connectionManager, pingMessage, SendTarget.broadcastAll(), TransportMode.MULTICAST_UDP);
    }

    public void sendPong(PingMessage pingMessage) {
        try {
            String userName = connectionManager.getUserName();
            String userIp = LocalNetworkInfo.bestLocalIpv4();

            PongMessage pongMessage = new PongMessage(pingMessage, userName, userIp);

            InetAddress senderAddress = InetAddress.getByName(pingMessage.getSenderIp());

            MessageDispatcher.send(connectionManager, pongMessage, SendTarget.singlePeer(senderAddress), TransportMode.MULTICAST_UDP);
        }
        catch (UnknownHostException ex) {
            Logger.getLogger(PingController.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    public void receivePong(PongMessage pongMessage) {
        long currentMillis = System.currentTimeMillis();

        if (pongMessage.isBroadcast()) {
            long tts = currentMillis - pongMessage.getSentAtmillis();
            pingLogSink.appendBroadcastSuccess(pongMessage.getReceiverName(), pongMessage.getReceiverIp(), tts);
        }
        else {
            PingData pingData = pendingPings.remove(pongMessage.getPingId());

            long tts = currentMillis - pingData.sentTime();
            pingLogSink.appendSendSuccess(pongMessage.getReceiverName(), pongMessage.getReceiverIp(), tts);
        }
    }
}
