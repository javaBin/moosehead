package no.java.moosehead.controller;

import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.saga.EmailSender;

import java.io.*;
import java.util.List;

public class ManualConfirmationSender {
    private EmailSender emailSender;

    public ManualConfirmationSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void doManual(Eventstore eventstore) {
        File file = new File("manual.txt");
        if (!file.exists()) {
            return;
        }
        System.out.println("Manual context found");
        String manualContent;
        try (InputStream is = new FileInputStream(file)) {
            manualContent = toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<AbstractEvent> eventstorageCopy = eventstore.getEventstorageCopy();
        for (String reservationId : manualContent.split("\n")) {
            ReservationAddedByUser reservationAddedByUser = eventstorageCopy.stream()
                    .filter(ae -> {
                        String id = "" + ae.getRevisionId();
                        return (ae instanceof ReservationAddedByUser) && id.equals(reservationId);
                    })
                    .map(ae -> (ReservationAddedByUser) ae)
                    .findAny().orElse(null);
            if (reservationAddedByUser == null) {
                System.out.println(String.format("Event %s not found",reservationId));
                continue;
            }
            System.out.println(String.format("Sending confirmation to %s id %s", reservationAddedByUser.getEmail(), reservationId));
            emailSender.sendReservationConfirmation(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId(), reservationAddedByUser.getReservationToken());
        }
        System.out.println("Manual context done");
        file.delete();


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
