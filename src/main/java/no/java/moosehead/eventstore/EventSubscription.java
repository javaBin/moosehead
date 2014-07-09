package no.java.moosehead.eventstore;

public interface EventSubscription {
    public void eventAdded(AbstractEvent event);
}
