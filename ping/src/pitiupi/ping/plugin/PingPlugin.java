package pitiupi.chat.plugin;

import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.application.presence.UserPeerView;
import pitiupi.chat.GUI.PingPanel;
import pitiupi.chat.control.PingController;
import pitiupi.chat.control.PingSender;
import pitiupi.chat.net.PingMessage;
import pitiupi.chat.net.PongMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import java.util.List;

public class PingPlugin extends Plugin {
    PingController pingController;
    PingPanel pingPanel;
    PingSender pingSender;
    PluginWorkspace pluginWorkspace;


    @Override
    public String getName() {
        return "Ping";
    }

    @Override
    public String getAuthor() {
        return "Gustavo H";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getIdentifier() {
        return "Pitiupi.Exemplo.Plugin.Ping.GustavoH";
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace pluginWorkspace) {
        pingController = new PingController(connectionManager);
        pingPanel = new PingPanel(pingController);
        pingSender = pingPanel;
        this.pluginWorkspace = pluginWorkspace;

        pingController.setPingLogSink(pingPanel);

        bindShellWorkspaceContent(pingPanel);

        installPeerActions(pluginWorkspace.peerActions());
    }


    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(PingMessage.class, PongMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof PingMessage ping) {
            pingController.sendPong(ping);
        }
        else if (message instanceof PongMessage pong) {
            pingController.receivePong(pong);
        }
    }

    @Override
    public void installPeerActions(PeerActionsRegistrar registrar) {
        registrar.register(PeerAction.of(
            "singlePing.peer",
            "Enviar ping para esta pessoa",
            PeerActionTarget.SINGLE_PEER,
            target -> {
                if (target instanceof PeerTarget.Single) {
                    PeerTarget.Single single = (PeerTarget.Single) target;
                    UserPeerView peer = single.peer();

                    openPingPanel();

                    peer.getInetAddress().ifPresent(address -> {
                        pingSender.sendPingTo(address.getHostAddress());
                    });
                }
            }
        ));
        registrar.register(PeerAction.of(
            "continuousPing.peer",
            "Enviar pings contínuos para esta pessoa",
            PeerActionTarget.SINGLE_PEER,
            target -> {
                if (target instanceof PeerTarget.Single) {
                    PeerTarget.Single single = (PeerTarget.Single) target;
                    UserPeerView peer = single.peer();

                    openPingPanel();

                    peer.getInetAddress().ifPresent(address -> {
                        pingSender.sendContinuousPingTo(address.getHostAddress());
                    });
                }
            }
        ));
        registrar.register(PeerAction.of(
            "broadcastPing.peer",
            "Enviar ping para todos os usuários",
            PeerActionTarget.ALL_PEERS,
            target -> {
                if (target instanceof PeerTarget.All) {
                    openPingPanel();

                    pingSender.broadcastPing();
                }
            }
        ));
    }

    private void openPingPanel() {
        pluginWorkspace.openOrFocusTab(
                getIdentifier(),
                getDisplayName()
        );
    }
}
