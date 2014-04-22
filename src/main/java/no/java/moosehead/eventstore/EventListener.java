package no.java.moosehead.eventstore;

public interface EventListener {
    public void eventAdded(AbstractEvent event);
}
