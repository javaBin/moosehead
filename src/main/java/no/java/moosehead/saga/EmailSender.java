package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.repository.WorkshopRepository;

import java.util.HashMap;
import java.util.Map;

public abstract class EmailSender {
    public abstract void send(EmailType type,String to,Map<String,String> values);

    public final void sendEmailConfirmation(String to,String token) {
        Map<String, String> values = new HashMap<>();
        values.put("token",token);
        send(EmailType.CONFIRM_EMAIL,to,values);
    }

    public final void sendReservationConfirmation(String to,String workshopId) {
        sendWorkshopInfo(to, workshopId, EmailType.RESERVATION_CONFIRMED);
    }

    private void sendWorkshopInfo(String to, String workshopId, EmailType emailType) {
        WorkshopRepository workshopRepository = SystemSetup.instance().workshopRepository();
        String wstitle = workshopRepository != null ? workshopRepository.workshopById(workshopId).map(ws -> ws.getTitle()).orElse("Unknown") : "Unknown";
        Map<String, String> values = new HashMap<>();
        values.put("workshop",wstitle);
        send(emailType,to,values);
    }

    public final void sendCancellationConfirmation(String to,String workshopId) {
        sendWorkshopInfo(to, workshopId, EmailType.RESERVATION_CANCELLED);
    }

    public final void sendWaitingListInfo(String to,String workshopId) {
        sendWorkshopInfo(to,workshopId,EmailType.WAITING_LIST);
    }
}
