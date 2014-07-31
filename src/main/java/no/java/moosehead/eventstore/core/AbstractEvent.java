package no.java.moosehead.eventstore.core;


public abstract class AbstractEvent implements BaseEvent{
    private long systemTimeInMillis;
    private long revisionId;

    public AbstractEvent(long systemTimeInMillis, long revisionId) {
        this.systemTimeInMillis = systemTimeInMillis;
        this.revisionId = revisionId;
    }

    public AbstractEvent() {
        //do nothing
    }

    public long getSystemTimeInMillis() {
        return systemTimeInMillis;
    }

    public long getRevisionId() {
        return revisionId;
    }
}
