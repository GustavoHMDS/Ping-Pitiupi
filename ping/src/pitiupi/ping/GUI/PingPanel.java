package pitiupi.chat.GUI;

import pitiupi.chat.control.PingController;
import pitiupi.chat.control.PingLogSink;
import pitiupi.chat.control.PingSender;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PingPanel extends JPanel implements PingLogSink, PingSender {

    private final PingController controller;

    private final JTextField txtIp;
    private final JButton btnSend = new JButton();
    private final JButton btnBroadcast = new JButton();
    private final JToggleButton btnContinuous = new JToggleButton();
    private final JTextPane txtLog = new JTextPane();

    public PingPanel(PingController controller) {
        this.controller = controller;

        txtIp = new JTextField(15);

        configLog();
        configSoloPingButton();
        configBroadcastPingButton();
        configContinuousPingButton();

        setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new FlowLayout());

        top.add(new JLabel("IP:"));
        top.add(txtIp);
        top.add(btnSend);
        top.add(btnContinuous);
        top.add(btnBroadcast);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(txtLog), BorderLayout.CENTER);
    }

    private void configLog() {
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(35, 35, 35));
        txtLog.setForeground(Color.WHITE);
        txtLog.setCaretColor(Color.WHITE);

        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
    }

    private void configSoloPingButton() {
        btnSend.setText("Enviar ping");

        btnSend.addActionListener(e -> controller.sendPing(txtIp.getText()));
    }

    private void configBroadcastPingButton() {
        btnBroadcast.setText("Enviar para todos");

        btnBroadcast.addActionListener(e -> controller.pingAll());
    }

    private void configContinuousPingButton() {
        btnContinuous.setText("Começar envio contínuo");

        btnContinuous.addActionListener(e -> {
            if (btnContinuous.isSelected()) {
                controller.sendContinuousPing(txtIp.getText());
                btnContinuous.setText("Parar envio contínuo");
            } else {
                controller.stopContinuousPing();
                btnContinuous.setText("Começar envio contínuo");
            }
        });
    }

    private void appendText(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = txtLog.getStyledDocument();

            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, color);

            try {
                doc.insertString(doc.getLength(), text, style);
            } catch (BadLocationException e) {
                Logger.getLogger(PingController.class.getName()).log(Level.WARNING, null, e);
            }
        });
    }

    @Override
    public void appendSendSuccess(String receiverName, String receiverIp, long rtt) {
        String line = "Pong recebido de " + receiverName + " (" + receiverIp + ") - RTT: " + rtt + " ms\n";
        SwingUtilities.invokeLater(() -> appendText(line + "\n", new Color(100, 220, 120)));
    }

    @Override
    public void appendBroadcastSuccess(String receiverName, String receiverIp, long rtt) {
        String line = "Pong recebido de " + receiverName + " (" + receiverIp + ") - RTT: " + rtt + " ms\n";
        SwingUtilities.invokeLater(() -> appendText(line + "\n", new Color(0, 255, 255)));
    }

    @Override
    public void appendFailure(String receiverIp) {
        String line = "Ping para " + " (" + receiverIp + ") expirou!\n";
        SwingUtilities.invokeLater(() -> appendText(line + "\n", new Color(255, 255, 0)));
    }

    @Override
    public void appendError(String errorText) {
        SwingUtilities.invokeLater(() -> appendText(errorText + "\n", new Color(255, 100, 100)));
    }

    @Override
    public void sendPingTo(String receiverIp) {
        txtIp.setText(receiverIp);
        btnSend.doClick();
    }

    @Override
    public void sendContinuousPingTo(String receiverIp) {
        txtIp.setText(receiverIp);
        btnContinuous.doClick();
    }

    @Override
    public void broadcastPing() {
        btnBroadcast.doClick();
    }
}
