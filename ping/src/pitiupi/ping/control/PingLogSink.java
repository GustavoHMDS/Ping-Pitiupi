package pitiupi.chat.control;

public interface PingLogSink {
    public void appendSendSuccess(String receiverName, String receiverIp, long tts);
    public void appendBroadcastSuccess(String receiverName, String receiverIp, long tts);
    public void appendFailure(String receiverIp);
    public void appendError(String errorText);
}
