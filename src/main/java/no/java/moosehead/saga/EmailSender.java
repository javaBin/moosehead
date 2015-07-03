package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.repository.WorkshopRepository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class EmailSender {
    public abstract void send(EmailType type,String to,Map<String,String> values);

    public final void sendEmailConfirmation(String to,String token,String workshopId) {
        sendWorkshopInfo(to,workshopId,EmailType.CONFIRM_EMAIL,token);
    }

    public final void sendReservationConfirmation(String to,String workshopId,String reservationToken) {
        sendWorkshopInfo(to, workshopId, EmailType.RESERVATION_CONFIRMED, reservationToken);
    }

        private void sendWorkshopInfo(String to, String workshopId, EmailType emailType, String token) {
        WorkshopRepository workshopRepository = SystemSetup.instance().workshopRepository();
        String wstitle = workshopRepository != null ? workshopRepository.workshopById(workshopId).map(ws -> ws.getTitle()).orElse("Unknown") : "Unknown";
        Map<String, String> values = new HashMap<>();
        values.put("workshop",wstitle);
        if (token != null) {
            values.put("token",token);
        }
        send(emailType, to, values);
    }

    public final void sendCancellationConfirmation(String to,String workshopId) {
        sendWorkshopInfo(to, workshopId, EmailType.RESERVATION_CANCELLED, null);
    }

    public final void sendWaitingListInfo(String to,String workshopId) {
        sendWorkshopInfo(to,workshopId,EmailType.WAITING_LIST, null);
    }

    protected String readFromTemplate(EmailType type, Map<String, String> values) {
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
