package no.java.moosehead.saga;

import no.java.moosehead.eventstore.EmailConfimationSentByEmailSaga;
import no.java.moosehead.eventstore.UserWorkshopEvent;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;

import java.util.ArrayList;


public class EmailSaga implements EventSubscription {

    private boolean sagaIsInitialized = false;

    private ArrayList<UserWorkshopEvent> userWorkshopEvents = new ArrayList<>();
    private ArrayList<AbstractEvent> emailSagaArrayList = new ArrayList<>();

    @Override
    public void eventAdded(AbstractEvent event) {
        if (event instanceof UserWorkshopEvent) {
            UserWorkshopEvent userWorkshopEvent = (UserWorkshopEvent) event;
            userWorkshopEvents.add(userWorkshopEvent);
            if (sagaIsInitialized) {
                // since we are synchronized we can assume the EmailSaga knows the full truth...
                if ( isUserNew(userWorkshopEvent) && !isEmailAlreadySent(userWorkshopEvent)) {
                    // TODO
                    System.out.println("Sending email now");
                }
            }
        } else if (event instanceof EmailConfimationSentByEmailSaga) {
            emailSagaArrayList.add(event);
        } else if(event instanceof SystemBootstrapDone) {
            sagaIsInitialized = true;
        }

    }

    public boolean isUserNew(UserWorkshopEvent event) {
        return userWorkshopEvents.stream()
                      .filter(e -> e.getEmail().equals(event.getEmail()))
                      .count() == 0;

    }

    public boolean isEmailAlreadySent(UserWorkshopEvent event) {
        return emailSagaArrayList.stream()
                .filter(e -> e.getRevisionId() == event.getRevisionId())
                .count() == 0;

    }

}
