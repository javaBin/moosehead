package no.java.moosehead.eventstore.core;

public interface EventSubscription {
    public void eventAdded(AbstractEvent event);
}
