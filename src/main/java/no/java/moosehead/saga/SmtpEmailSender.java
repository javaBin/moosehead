package no.java.moosehead.saga;

import no.java.moosehead.web.Configuration;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.Map;

public class SmtpEmailSender extends EmailSender {
    @Override
    public void send(EmailType type, String to, Map<String, String> values) {
        values.put("to", to);
        values.put("mooseheadLocation", Configuration.mooseheadLocation());

        String message = readFromTemplate(type, values);

        try {
            sendEmail(type, message, to);
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendEmail(EmailType type, String message, String to) throws EmailException {
        String subject = type.getSubject();
        if (!Configuration.isProdEnviroment()) {
            message = "[This message is just a test. Please disregart and delete]\n" + message;
            subject = "[TEST] " + subject;
        }

        SimpleEmail mail = new SimpleEmail();
        mail.setHostName(Configuration.smtpServer());
        mail.setSmtpPort(Configuration.smtpPort());
        mail.setFrom("program@java.no");
        mail.addTo(to);
        mail.setSubject(subject);
        mail.setMsg(message);

        String bcc = Configuration.bccTo();
        if (bcc != null) {
            for (String tobc : bcc.split(";")) {
                mail.addBcc(tobc);
            }
        }

        mail.send();
    }



}
