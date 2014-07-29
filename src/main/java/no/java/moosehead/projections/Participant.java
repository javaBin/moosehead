package no.java.moosehead.projections;

public class Participant {
    private final String name;
    private final String email;
    private boolean emailConfirmed = false;

    public Participant(String email, String name) {
        if (email == null) {
            throw new NullPointerException("Email can not be null");
        }
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void confirmEmail() {
        emailConfirmed = true;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

        if (!email.equals(that.email)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}
