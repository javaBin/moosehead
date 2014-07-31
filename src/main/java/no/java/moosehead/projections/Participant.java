package no.java.moosehead.projections;

public class Participant {
    private final String name;
    private final String email;
    private boolean emailConfirmed;
    private Workshop workshop;


    private Participant(String email, String fullname, Workshop workshop,boolean emailConfirmed) {
        this.workshop = workshop;
        if (email == null) {
            throw new NullPointerException("Email can not be null");
        }
        this.name = fullname;
        this.email = email;
        this.emailConfirmed = emailConfirmed;
    }

    public static Participant confirmedParticipant(String email, String fullname,Workshop workshop) {
        return new Participant(email,fullname,workshop,true);
    }

    public static Participant unconfirmedParticipant(String email, String fullname,Workshop workshop) {
        return new Participant(email,fullname,workshop,false);
    }

    public static Participant dummyParticipant(String email) {
        return new Participant(email,null,null,false);
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void confirmEmail() {
        emailConfirmed = true;
        workshop.moveToConfirmed(this);
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

    public String getWorkshopId() {
        return (workshop != null && workshop.getWorkshopData().getId() != null) ? workshop.getWorkshopData().getId() : null;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}
