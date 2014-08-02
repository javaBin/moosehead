package no.java.moosehead.saga;

import java.util.Map;

public class DummyEmailSender implements EmailSender {
    @Override
    public void send(EmailType type, String to, Map<String, String> values) {
        System.out.println(String.format("Sending <%s> to %s, para : %s",type,to,values));
    }
}
