package no.java.moosehead.saga;

public enum EmailType {
    CONFIRM_EMAIL("confirmEmail.txt","Javazone Workshop: Email confirmation needed"),
    RESERVATION_CONFIRMED("confirmReservation.txt","Javazone Workshop: Reservation confirmed"),
    RESERVATION_CANCELLED("confirmationCancelled.txt","Javazone Workshop: Cancellation confirmed"),
    WAITING_LIST("waitingList.txt","Javazone Workshop: You are on the waiting list");

    private final String template;
    private final String subject;

    private EmailType(String template,String subject) {
        this.template = template;
        this.subject = subject;
    }

    public String getTemplate() {
        return template;
    }

    public String getSubject() {
        return subject;
    }
}
