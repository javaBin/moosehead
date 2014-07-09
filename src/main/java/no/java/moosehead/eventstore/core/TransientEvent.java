package no.java.moosehead.eventstore.core;

/**
 * A interface indicating that the Event should not be persisted. Mainly used for system events, like boostrap and shutdown etc.
 */
public interface TransientEvent {
}
