package no.java.moosehead.saga;

import no.java.moosehead.web.Configuration;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.LinkedList;
import java.util.Map;

public class SmtpEmailSender extends EmailSender {
    public static class EmailMessage {

        public final EmailType type;
        public final String message;
        public final String to;

        public EmailMessage(EmailType type, String message, String to) {
            this.type = type;
            this.message = message;
            this.to = to;
        }
    }

    private final LinkedList<EmailMessage> messages = setupQue();

    private LinkedList<EmailMessage> setupQue() {
        LinkedList<EmailMessage> emailMessages = new LinkedList<>();
        long millis = Configuration.emailSleepTime();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                }
                EmailMessage emailMessage;
                synchronized (messages) {
                    if (emailMessages.isEmpty()) {
                        continue;
                    }
                    emailMessage = emailMessages.poll();
                }
                try {
                    System.out.println("Sending email to " + emailMessage.to);
                    sendEmail(emailMessage.type,emailMessage.message,emailMessage.to);
                } catch (EmailException e) {
                    System.out.println("Mail send failed: " + e.getMessage());
                }

            }
        }).start();
        return emailMessages;
    }

    @Override
    public void send(EmailType type, String to, Map<String, String> values) {
        values.put("to", to);
        values.put("mooseheadLocation", Configuration.mooseheadLocation());

        String message = readFromTemplate(type, values);
        synchronized (messages) {
            messages.add(new EmailMessage(type, message, to));
        }

    }


    private void sendEmailAsync(EmailType type, String message, String to) throws EmailException {
        new EmailMessage(type,message,to);
    }

    private void sendEmail(EmailType type, String message, String to) throws EmailException {
        String subject = type.getSubject();
        if (!Configuration.isProdEnviroment()) {
            int index = message.indexOf("<body>");
            if (index == -1) {
                message = "[This message is just a test. Please disregard and delete]\n" + message;
            } else {
                StringBuilder newMess = new StringBuilder(message);
                index = index + "<body>".length();
                newMess.insert(index,"<p>[This message is just a test. Please disregard and delete]</p>");
                message = newMess.toString();
            }
            subject = "[TEST] " + subject;
        }

        sendSingleMail(message, to, subject);
    }

    protected void sendSingleMail(String message, String to, String subject) throws EmailException {
        SimpleEmail mail = new SimpleEmail();
        mail.setHostName(Configuration.smtpServer());
        mail.setFrom("program@java.no");
        mail.addTo(to);
        mail.setSubject(subject);
        String contentType = message.contains("<body>") ? "text/html" : "text/plain";
        mail.setContent(message,contentType);

        if (Configuration.useMailSSL()) {
            mail.setSSLOnConnect(true);
            mail.setSslSmtpPort("" + Configuration.smtpPort());
        } else {
            mail.setSmtpPort(Configuration.smtpPort());

        }
        String mailUser = Configuration.mailUser();
        if (mailUser != null) {
            mail.setAuthentication(mailUser, Configuration.mailPassword());
        }

        String bcc = Configuration.bccTo();
        if (bcc != null) {
            for (String tobc : bcc.split(";")) {
                mail.addBcc(tobc);
            }
        }

        mail.send();
    }


}
