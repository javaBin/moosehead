package no.java.moosehead.projections;

public class Participant {
    private String name;
    private String email;

    public Participant(String email, String name) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}
