package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.EmailConfimationSentByEmailSaga;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.UserWorkshopEvent;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;

import java.util.ArrayList;


public class EmailSaga implements EventSubscription {

    private boolean sagaIsInitialized = false;

    @Override
    public void eventAdded(AbstractEvent event) {
        if(event instanceof SystemBootstrapDone) {
            sagaIsInitialized = true;
            return;
        }
        if (!sagaIsInitialized) {
            return;
        }
        if (event instanceof ReservationAddedByUser) {
            ReservationAddedByUser res = (ReservationAddedByUser) event;
            SystemSetup.instance().emailSender().sendEmailConfirmation(res.getEmail(),"" + res.getRevisionId());
        }
    }



}
