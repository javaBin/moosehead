package no.java.moosehead.saga;

import no.java.moosehead.web.Configuration;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.io.*;
import java.util.Map;

public class SmtpEmailSender extends EmailSender {
    @Override
    public void send(EmailType type, String to, Map<String, String> values) {
        values.put("to",to);
        values.put("mooseheadLocation", Configuration.mooseheadLocation());

        String message = readFromTemplate(type,values);

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

    private String readFromTemplate(EmailType type, Map<String, String> values) {
        String template;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(type.getTemplate())) {
            template = toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String,String> replace : values.entrySet()) {
            String search = "#" + replace.getKey() + "#";
            template = template.replaceAll(search,replace.getValue());
        }
        return template;
    }

    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }

}
