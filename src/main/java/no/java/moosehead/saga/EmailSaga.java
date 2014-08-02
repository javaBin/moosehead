package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import no.java.moosehead.web.Configuration;

import java.util.*;
import java.util.stream.Collectors;


public class EmailSaga implements EventSubscription {

    private boolean sagaIsInitialized = false;
    private List<ReservationAddedByUser> unconfirmedReservations = new ArrayList<>();
    private Set<String> confirmedEmails = new HashSet<>();
    private Map<String,List<String>> participants = new HashMap<>();

    private void addParticipant(String wsid, String email) {
        List<String> partList = participants.get(wsid);
        if (partList == null) {
            partList = new ArrayList<>();
            participants.put(wsid,partList);
        }
        partList.add(email);
    }

    private void removeParticipant(String wsid, String email) {
        List<String> partList = participants.get(wsid);
        if (partList == null) {
            return;
        }
        partList.remove(email);
    }

    private boolean haveFreeSpots(String wsid) {
        int numSpots = Configuration.placesPerWorkshop();
        int takenSpots = participants.getOrDefault(wsid,new ArrayList<>()).size();
        return (numSpots > takenSpots);
    }

    @Override
    public void eventAdded(AbstractEvent event) {
        if(event instanceof SystemBootstrapDone) {
            sagaIsInitialized = true;
            return;
        }
        if (event instanceof ReservationAddedByUser) {
            ReservationAddedByUser res = (ReservationAddedByUser) event;
            unconfirmedReservations.add(res);
            EmailSender emailSender = SystemSetup.instance().emailSender();
            boolean emailIsConfirmed = confirmedEmails.contains(res.getEmail());
            if (sagaIsInitialized) {
                if (emailIsConfirmed) {
                    if (haveFreeSpots(res.getWorkshopId())) {
                        emailSender.sendReservationConfirmation(res.getEmail(), res.getWorkshopId());
                    } else {
                        emailSender.sendWaitingListInfo(res.getEmail(),res.getWorkshopId());
                    }
                } else {
                    emailSender.sendEmailConfirmation(res.getEmail(), "" + res.getRevisionId());
                }
            }
            if (emailIsConfirmed) {
                addParticipant(res.getWorkshopId(), res.getEmail());
            }
        }
        if (event instanceof EmailConfirmedByUser) {
            EmailConfirmedByUser emailConfirmedByUser = (EmailConfirmedByUser) event;
            List<ReservationAddedByUser> toConfirm = unconfirmedReservations.stream()
                    .filter(res -> res.getEmail().equals(emailConfirmedByUser.getEmail()))
                    .collect(Collectors.toList());

            EmailSender emailSender = SystemSetup.instance().emailSender();
            for (ReservationAddedByUser reservationAddedByUser : toConfirm) {
                if (sagaIsInitialized) {
                    if (haveFreeSpots(reservationAddedByUser.getWorkshopId())) {
                        emailSender.sendReservationConfirmation(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId());
                    } else {
                        emailSender.sendWaitingListInfo(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId());
                    }
                }
                addParticipant(reservationAddedByUser.getWorkshopId(), reservationAddedByUser.getEmail());
                unconfirmedReservations.remove(reservationAddedByUser);
            }
            confirmedEmails.add(emailConfirmedByUser.getEmail());
        }
        if (event instanceof ReservationCancelledByUser) {
            ReservationCancelledByUser cancelledByUser = (ReservationCancelledByUser) event;
            Optional<ReservationAddedByUser> reservation = unconfirmedReservations.stream()
                    .filter(ur -> ur.getEmail().equals(cancelledByUser.getEmail()) && ur.getWorkshopId().equals(cancelledByUser.getWorkshopId()))
                    .findAny();
            if (reservation.isPresent()) {
                unconfirmedReservations.remove(reservation.get());
            }
            removeParticipant(cancelledByUser.getWorkshopId(),cancelledByUser.getEmail());
            if (sagaIsInitialized) {
                SystemSetup.instance().emailSender().sendCancellationConfirmation(cancelledByUser.getEmail(),cancelledByUser.getWorkshopId());
            }

        }
    }



}
