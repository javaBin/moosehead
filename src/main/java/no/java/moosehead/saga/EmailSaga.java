package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class EmailSaga implements EventSubscription {

    private boolean sagaIsInitialized = false;
    private List<ReservationAddedByUser> unconfirmedReservations = new ArrayList<>();

    @Override
    public void eventAdded(AbstractEvent event) {
        if(event instanceof SystemBootstrapDone) {
            sagaIsInitialized = true;
            return;
        }
        if (event instanceof ReservationAddedByUser) {
            ReservationAddedByUser res = (ReservationAddedByUser) event;
            unconfirmedReservations.add(res);
            if (sagaIsInitialized) {
                SystemSetup.instance().emailSender().sendEmailConfirmation(res.getEmail(), "" + res.getRevisionId());
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
                    emailSender.sendReservationConfirmation(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId());
                }
                unconfirmedReservations.remove(reservationAddedByUser);
            }
        }
        if (event instanceof ReservationCancelledByUser) {
            ReservationCancelledByUser cancelledByUser = (ReservationCancelledByUser) event;
            if (sagaIsInitialized) {
                SystemSetup.instance().emailSender().sendCancellationConfirmation(cancelledByUser.getEmail(),cancelledByUser.getWorkshopId());
            }
        }
    }



}
