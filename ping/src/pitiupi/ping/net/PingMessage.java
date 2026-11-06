package pitiupi.chat.net;

import pitiupi.models.Messages.Message;

public class PingMessage extends Message {
    final String pingId;
    final long sentAtMillis;
    final String senderName;
    final String senderIp;
    final boolean isBroadcast;

    public PingMessage(String pingId, long sentAtMillis, String senderName, String senderIp, boolean isBroadcast) {
        this.pingId = pingId;
        this.sentAtMillis = sentAtMillis;
        this.senderName = senderName;
        this.senderIp = senderIp;
        this.isBroadcast = isBroadcast;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public long getSentAtMillis() {
        return sentAtMillis;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getPingId() {
        return pingId;
    }
    public boolean isBroadcast() {
        return isBroadcast;
    }
}
