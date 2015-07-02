package no.java.moosehead.eventstore.core;

public interface BaseEvent {
    public long getSystemTimeInMillis();
    public long getRevisionId();
}
