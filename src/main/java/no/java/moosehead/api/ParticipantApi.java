package no.java.moosehead.api;

import no.java.moosehead.commands.AuthorEnum;
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
    public ParticipantActionResult reservation(String workshopid, String email, String fullname, AuthorEnum authorEnum, Optional<String> googleEmail, int numReservations);
    public ParticipantActionResult confirmEmail(String token);
    public ParticipantActionResult cancellation(String reservationId, AuthorEnum authorEnum);
    public List<ParticipantReservation> myReservations(String email);

    public static JsonObject asAdminJson(WorkshopInfo workshop) {
        JsonObject jsonObject = JsonFactory.jsonObject();
            jsonObject.withValue("id", workshop.getId());
            jsonObject.withValue("title", workshop.getTitle());
            jsonObject.withValue("description", workshop.getDescription());
            jsonObject.withValue("status", workshop.getStatus().name());
            jsonObject.withValue("workshopType", Optional.ofNullable(workshop.getWorkshopTypeEnum()).map(Object::toString).orElse(null));

            List<JsonObject> partList = workshop.getParticipants().stream().sequential()
                    .map(ParticipantApi::participantAsAdminJson)
                    .collect(Collectors.toList());
            jsonObject.withValue("participants", JsonArray.fromNodeList(partList));

        return jsonObject;

    }

    public static JsonObject participantAsAdminJson(Participant participant) {
        JsonObject partObj = JsonFactory.jsonObject();

        partObj.withValue("email", participant.getEmail());
        partObj.withValue("numberOfSeats", participant.getNumberOfSeatsReserved());
        partObj.withValue("name", participant.getName());
        partObj.withValue("isEmailConfirmed", participant.isEmailConfirmed());
        partObj.withValue("confirmedAt", participant.getConfirmedAt().map(ca -> ca.toString()).orElse("-"));
        partObj.withValue("isWaiting",participant.isWaiting());
        String cancelLink = Configuration.mooseheadLocation() + "/#/cancel/" + participant.getReservationToken();
        partObj.withValue("cancelLink",cancelLink);
        return partObj;
    }

    public static JsonObject participantAsJson(Participant participant) {
        JsonObject partObj = JsonFactory.jsonObject();

        partObj.withValue("email", participant.getEmail());
        partObj.withValue("numberOfSeats", participant.getNumberOfSeatsReserved());
        partObj.withValue("name", participant.getName());
        partObj.withValue("isEmailConfirmed", participant.isEmailConfirmed());
        partObj.withValue("confirmedAt", participant.getConfirmedAt().map(ca -> ca.toString()).orElse("-"));
        partObj.withValue("isWaiting",participant.isWaiting());

        return partObj;
    }
}
