package no.java.moosehead.eventstore.core;

/**
 * Created by Tor Egil Refsahl on 09.07.2014.
 */
public interface BaseEvent {
    public long getSystemTimeInMillis();
    public long getRevisionId();
}
