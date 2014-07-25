package no.java.moosehead.eventstore;

public class EmailConfirmedByUser {
    private String email;

    public EmailConfirmedByUser(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
