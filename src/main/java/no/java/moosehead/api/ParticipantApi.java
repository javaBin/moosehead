package no.java.moosehead.api;

import java.util.List;

public interface ParticipantApi {
    public List<WorkshopInfo> workshops();
    public ParticipantActionResult reservation(String workshopid, String email, String fullname);
    public ParticipantActionResult confirmEmail(String token);
    public ParticipantActionResult cancellation(String reservationId);
    public List<ParticipantReservation> myReservations(String email);
}
