package no.java.moosehead.eventstore;


public abstract class AbstractEvent {
    private long systemTimeInMillis;
    private long revisionId;

    public AbstractEvent(long systemTimeInMillis, long revisionId) {
        this.systemTimeInMillis = systemTimeInMillis;
        this.revisionId = revisionId;
    }

    public long getSystemTimeInMillis() {
        return systemTimeInMillis;
    }

    public long getRevisionId() {
        return revisionId;
    }
}
