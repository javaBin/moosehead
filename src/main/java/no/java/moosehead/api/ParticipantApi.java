package no.java.moosehead.api;

import java.util.List;

public interface ParticipantApi {
    public List<WorkshopInfo> workshops();
    public ParticipantActionResult reservation(String workshopid, String email, String fullname);
    public ParticipantActionResult cancellation(String workshopid, String email);
    public List<ParticipantReservation> myReservations(String email);
}
