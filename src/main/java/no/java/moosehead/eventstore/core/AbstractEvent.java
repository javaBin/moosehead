package no.java.moosehead.eventstore.core;


public abstract class AbstractEvent {
    private Long systemTimeInMillis;
    private Long revisionId;

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
