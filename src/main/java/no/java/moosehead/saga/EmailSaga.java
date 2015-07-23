package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
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
    private Map<String,List<ReservationAddedByUser>> participants = new HashMap<>();

    private void addParticipant(ReservationAddedByUser res) {
        List<ReservationAddedByUser> partList = participants.get(res.getWorkshopId());
        if (partList == null) {
            partList = new ArrayList<>();
            participants.put(res.getWorkshopId(),partList);
        }
        partList.add(res);
    }

    private boolean removeParticipant(String wsid, String email) {
        List<ReservationAddedByUser> partList = participants.get(wsid);
        if (partList == null) {
            return false;
        }
        Optional<ReservationAddedByUser> res = partList.stream()
                .filter(rau -> rau.getEmail().equals(email))
                .findAny();
        if (!res.isPresent()) {
            return false;
        }
        return partList.remove(res.get());
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
            if (res.getGoogleUserEmail().filter(email -> email.equals(res.getEmail())).isPresent()) {
                confirmedEmails.add(res.getGoogleUserEmail().get());
            }
            boolean emailIsConfirmed = confirmedEmails.contains(res.getEmail());
            if (sagaIsInitialized) {
                if (emailIsConfirmed) {
                    if (haveFreeSpots(res.getWorkshopId())) {
                        emailSender.sendReservationConfirmation(res.getEmail(), res.getWorkshopId(),res.getReservationToken());
                    } else {
                        emailSender.sendWaitingListInfo(res.getEmail(),res.getWorkshopId());
                    }
                } else {
                    emailSender.sendEmailConfirmation(res.getEmail(), res.getReservationToken() ,res.getWorkshopId());
                }
            }
            if (emailIsConfirmed) {
                addParticipant(res);
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
                        emailSender.sendReservationConfirmation(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId(),reservationAddedByUser.getReservationToken());
                    } else {
                        emailSender.sendWaitingListInfo(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId());
                    }
                }
                addParticipant(reservationAddedByUser);
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
            boolean full = !haveFreeSpots(cancelledByUser.getWorkshopId());
            boolean removed = removeParticipant(cancelledByUser.getWorkshopId(), cancelledByUser.getEmail());
            if (sagaIsInitialized) {
                EmailSender emailSender = SystemSetup.instance().emailSender();
                emailSender.sendCancellationConfirmation(cancelledByUser.getEmail(), cancelledByUser.getWorkshopId());
                if (full && removed) {
                    ReservationAddedByUser res = participants.get(cancelledByUser.getWorkshopId()).get(Configuration.placesPerWorkshop() - 1);
                    if (!Configuration.closedWorkshops().contains(cancelledByUser.getWorkshopId())) {
                         emailSender.sendReservationConfirmation(res.getEmail(),cancelledByUser.getWorkshopId(),res.getReservationToken());
                    }
                }
            }

        }
    }



}
