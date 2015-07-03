package no.java.moosehead.saga;

import no.java.moosehead.web.Configuration;

import java.util.Map;

public class DummyEmailSender extends EmailSender {
    @Override
    public void send(EmailType type, String to, Map<String, String> values) {

        values.put("to",to);
        values.put("mooseheadLocation", Configuration.mooseheadLocation());

        String message = readFromTemplate(type,values);

        System.out.println(String.format("Sending <%s> with [%s] and message \n%s", type, values, message));
    }
}
