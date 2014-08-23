package no.java.moosehead.api;

import no.java.moosehead.projections.Participant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public interface ParticipantApi {
    public WorkshopInfo getWorkshop(String workshopid);
    public List<WorkshopInfo> workshops();
    public ParticipantActionResult reservation(String workshopid, String email, String fullname);
    public ParticipantActionResult confirmEmail(String token);
    public ParticipantActionResult cancellation(String reservationId);
    public List<ParticipantReservation> myReservations(String email);

    public static JSONObject asAdminJson(WorkshopInfo workshop) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", workshop.getId());
            jsonObject.put("title", workshop.getTitle());
            jsonObject.put("description", workshop.getDescription());
            jsonObject.put("status", workshop.getStatus().name());
            jsonObject.put("createdRevisionId",workshop.getCreatedRevisionId());

            List<JSONObject> partList = workshop.getParticipants().stream().sequential()
                    .map(ParticipantApi::participantAsJson)
                    .collect(Collectors.toList());
            jsonObject.put("participants",new JSONArray(partList));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;

    }

    public static JSONObject participantAsJson(Participant participant) {
        JSONObject partObj = new JSONObject();

        try {
            partObj.put("email", participant.getEmail());
            partObj.put("name", participant.getName());
            partObj.put("isEmailConfirmed", participant.isEmailConfirmed());
            partObj.put("confirmedAt", participant.getConfirmedAt().map(ca -> ca.toString()).orElse("-"));
            partObj.put("isWaiting",participant.isWaiting());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return partObj;
    }
}
