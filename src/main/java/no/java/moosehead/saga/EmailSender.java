package no.java.moosehead.saga;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class EmailSender {
    private static Map<String,String> preperationsTexts = readPreperationsText();

    private static Map<String, String> readPreperationsText() {
        String prepfilecontent;
        try (InputStream is = EmailSender.class.getClassLoader().getResourceAsStream("preperations.html")) {
            prepfilecontent = toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> result = new HashMap<>();
        for (String line : prepfilecontent.split("\n")) {
            if (line.trim().isEmpty() || line.startsWith("#")) {
                continue;
            }
            int pos = line.indexOf("=");
            result.put(line.substring(0,pos),line.substring(pos+1));
        }
        return result;
    }

    public abstract void send(EmailType type,String to,Map<String,String> values);

    public final void sendEmailConfirmation(String to, String token, String workshopId) {
        sendWorkshopInfo(to,workshopId,EmailType.CONFIRM_EMAIL,token);
    }

    public final void sendReservationConfirmation(String to,String workshopId,String reservationToken) {
        sendWorkshopInfo(to, workshopId, EmailType.RESERVATION_CONFIRMED, reservationToken);
    }

    private void sendWorkshopInfo(String to, String workshopId, EmailType emailType, String token) {
        WorkshopRepository workshopRepository = SystemSetup.instance().workshopRepository();
        Optional<WorkshopData> workshopData = workshopRepository != null ? workshopRepository.workshopById(workshopId) : Optional.empty();
        String wstitle = workshopData.map(ws -> ws.getTitle()).orElse("Unknown");
        String start = workshopData.map(ws -> Optional.ofNullable(ws.getStartTime()).map(EmailSender::formatInstant).orElse("Unknown")).orElse("Unknown");
        Map<String, String> values = new HashMap<>();
        values.put("workshop",wstitle);
        values.put("starts",start);
        workshopData.ifPresent(wd -> values.put("workshopType",wd.getWorkshopTypeEnum().toString()));
        if (token != null) {
            values.put("token",token);
        }
        readPreperations(workshopData,workshopId).ifPresent(prep -> values.put("preparation",prep));

        send(emailType, to, values);
    }

    private Optional<String> readPreperations(Optional<WorkshopData> workshopData, String workshopId) {
        if (workshopData.map(wd -> wd.getWorkshopTypeEnum()).orElse(null) != WorkshopTypeEnum.NORMAL_WORKSHOP) {
            return Optional.empty();
        }
        return Optional.of(Optional.ofNullable(preperationsTexts.get(workshopId)).orElse("Please bring your laptop"));
    }


    public static String formatInstant(Instant instant) {
        ZonedDateTime offsetDateTime = instant.atZone(ZoneId.of("Europe/Oslo"));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE MMMM d'th,' 'at' HH:mm");
        return dateTimeFormatter.format(offsetDateTime);
    }

    public final void sendCancellationConfirmation(String to,String workshopId) {
        sendWorkshopInfo(to, workshopId, EmailType.RESERVATION_CANCELLED, null);
    }

    public final void sendWaitingListInfo(String to,String workshopId) {
        sendWorkshopInfo(to,workshopId,EmailType.WAITING_LIST, null);
    }

    protected String readFromTemplate(EmailType type, Map<String, String> values) {
        String templateName = WorkshopTypeEnum.fromValue(values.get("workshopType")).map(wt -> type.getSpecialHandling().get(wt)).orElse(type.getTemplate());
        String template;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templateName)) {
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
