package pitiupi.chat.control;

public interface PingSender {
    public void sendPingTo(String receiverIp);

    public void sendContinuousPingTo(String receiverIp);

    public void broadcastPing();
}
