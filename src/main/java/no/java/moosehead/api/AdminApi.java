package no.java.moosehead.api;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;
import java.util.Optional;

public interface AdminApi {
    ParticipantActionResult createWorkshop(WorkshopData workshopData,Instant startTime,Instant endTime,Instant openTime,int maxParticipants);

    ParticipantActionResult partialCancel(String email, String workshopid, int numSpotCanceled);

    ParticipantActionResult registerShowUp(String reservationToken, boolean shownUp);

    ParticipantActionResult changeWorkshopSize(String workshopid,int updatedNumberOfSpaces);
}
