package no.java.moosehead.saga;

import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;

import java.util.ArrayList;


public class EmailSaga implements EventSubscription {

    private ArrayList<AbstractEvent> eventArrayList = new ArrayList<>();

    @Override
    public void eventAdded(AbstractEvent event) {
        eventArrayList.add(event);
    }
}
