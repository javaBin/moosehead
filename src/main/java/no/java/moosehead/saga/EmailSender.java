package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;

import java.util.HashMap;
import java.util.Map;

public interface EmailSender {
    public void send(EmailType type,String to,Map<String,String> values);

    public default void sendEmailConfirmation(String to,String token) {
        Map<String, String> values = new HashMap<>();
        values.put("token",token);
        send(EmailType.CONFIRM_EMAIL,to,values);
    }

    public default void sendReservationConfirmation(String to,String workshopId) {
        String wstitle = SystemSetup.instance().workshopRepository().workshopById(workshopId).map(ws -> ws.getTitle()).orElse("Unknown");
        Map<String, String> values = new HashMap<>();
        values.put("workshop",wstitle);
        send(EmailType.RESERVATION_CONFIRMED,to,values);
    }

    public default void sendCancellationConfirmation(String to,String workshopId) {
        String wstitle = SystemSetup.instance().workshopRepository().workshopById(workshopId).map(ws -> ws.getTitle()).orElse("Unknown");
        Map<String, String> values = new HashMap<>();
        values.put("workshop",wstitle);
        send(EmailType.RESERVATION_CANCELLED,to,values);
    }
}
