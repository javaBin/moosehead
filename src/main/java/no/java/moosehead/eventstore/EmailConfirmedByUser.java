package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public class EmailConfirmedByUser extends AbstractEvent {
    private String email;

    public EmailConfirmedByUser(String email,long systemTimeInMillis, long revisionId) {
        super(systemTimeInMillis,revisionId);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
