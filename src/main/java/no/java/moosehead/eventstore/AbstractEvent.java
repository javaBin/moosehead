package no.java.moosehead.eventstore;


public abstract class AbstractEvent {
    private long systemTimeInMillis;

    protected AbstractEvent(long systemTimeInMillis) {
        this.systemTimeInMillis = systemTimeInMillis;
    }

    public long getSystemTimeInMillis() {
        return systemTimeInMillis;
    }
}
