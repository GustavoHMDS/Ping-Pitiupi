package pitiupi.chat.net;

import pitiupi.models.Messages.Message;

public class PongMessage extends Message {
    final String pingId;
    final long sentAtmillis;
    final String receiverName;
    final String receiverIp;
    final boolean isBroadcast;

    public PongMessage(PingMessage pingMessage, String receiverName, String receiverIp) {
        this.pingId = pingMessage.pingId;
        this.sentAtmillis = pingMessage.sentAtMillis;
        this.receiverName = receiverName;
        this.receiverIp = receiverIp;
        this.isBroadcast = pingMessage.isBroadcast;
    }

    public String getPingId() {
        return pingId;
    }

    public long getSentAtmillis() {
        return sentAtmillis;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverIp() {
        return receiverIp;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }
}
