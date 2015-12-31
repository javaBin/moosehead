package no.java.moosehead.api;

import no.java.moosehead.commands.AuthorEnum;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.web.Configuration;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ParticipantApi {
    public WorkshopInfo getWorkshop(String workshopid);
    public List<WorkshopInfo> workshops();
    public ParticipantActionResult reservation(WorkshopReservation workshopReservation,AuthorEnum authorEnum);
    public ParticipantActionResult confirmEmail(String token);
    public ParticipantActionResult cancellation(String reservationId, AuthorEnum authorEnum);
    public List<ParticipantReservation> myReservations(String email);

    public static JsonObject asAdminJson(WorkshopInfo workshop) {
        JsonObject jsonObject = JsonFactory.jsonObject();
            jsonObject.put("id", workshop.getId());
            jsonObject.put("title", workshop.getTitle());
            jsonObject.put("description", workshop.getDescription());
            jsonObject.put("status", workshop.getStatus().name());
            jsonObject.put("workshopType", Optional.ofNullable(workshop.getWorkshopTypeEnum()).map(Object::toString).orElse(null));

            List<JsonObject> partList = workshop.getParticipants().stream().sequential()
                    .map(ParticipantApi::participantAsAdminJson)
                    .collect(Collectors.toList());
            jsonObject.put("participants", JsonArray.fromNodeList(partList));

        return jsonObject;

    }

    public static JsonObject participantAsAdminJson(Participant participant) {
        JsonObject partObj = JsonFactory.jsonObject();

        partObj.put("email", participant.getWorkshopReservation().getEmail());
        partObj.put("numberOfSeats", participant.getNumberOfSeatsReserved());
        partObj.put("name", participant.getWorkshopReservation().getFullname());
        partObj.put("isEmailConfirmed", participant.isEmailConfirmed());
        partObj.put("confirmedAt", participant.getConfirmedAt().map(ca -> ca.toString()).orElse("-"));
        partObj.put("isWaiting",participant.isWaiting());
        partObj.put("additionalInfo",participant.getWorkshopReservation().getAdditionalInfo());
        String cancelLink = Configuration.mooseheadLocation() + "/#/cancel/" + participant.getWorkshopReservation().getReservationToken();
        partObj.put("cancelLink",cancelLink);
        return partObj;
    }

    public static JsonObject participantAsJson(Participant participant) {
        JsonObject partObj = JsonFactory.jsonObject();

        partObj.put("email", participant.getWorkshopReservation().getEmail());
        partObj.put("numberOfSeats", participant.getNumberOfSeatsReserved());
        partObj.put("name", participant.getWorkshopReservation().getFullname());
        partObj.put("isEmailConfirmed", participant.isEmailConfirmed());
        partObj.put("confirmedAt", participant.getConfirmedAt().map(ca -> ca.toString()).orElse("-"));
        partObj.put("isWaiting",participant.isWaiting());

        return partObj;
    }
}
