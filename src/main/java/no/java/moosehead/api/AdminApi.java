package no.java.moosehead.api;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;

public interface AdminApi {
    ParticipantActionResult createWorkshop(WorkshopData workshopData,Instant startTime,Instant endTime,Instant openTime,int maxParticipants);
}
