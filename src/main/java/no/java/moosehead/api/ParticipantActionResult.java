package no.java.moosehead.api;

public class ParticipantActionResult {
    public static enum Status {
        OK,CONFIRM_EMAIL,WAITING_LIST,WRONG_CAPTCHA,ERROR
    }
    private final Status status;
    private final String errormessage;

    private ParticipantActionResult(Status status, String errormessage) {
        this.status = status;
        this.errormessage = errormessage;
    }

    public static ParticipantActionResult ok() {
        return new ParticipantActionResult(Status.OK,null);
    }

    public static ParticipantActionResult okWithMessage(String message) {
        return new ParticipantActionResult(Status.OK,message);
    }

    public static ParticipantActionResult error(String message) {
        return new ParticipantActionResult(Status.ERROR,message);
    }

    public static ParticipantActionResult wrongCaptcha() {
        return new ParticipantActionResult(Status.WRONG_CAPTCHA,null);
    }

    public static ParticipantActionResult confirmEmail() {
        return new ParticipantActionResult(Status.CONFIRM_EMAIL,null);
    }

    public static ParticipantActionResult waitingList(String message) {
        return new ParticipantActionResult(Status.WAITING_LIST,message);
    }


    public Status getStatus() {
        return status;
    }

    public String getErrormessage() {
        return errormessage;
    }
}
