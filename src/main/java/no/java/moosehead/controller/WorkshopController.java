package no.java.moosehead.controller;

import no.java.moosehead.api.*;
import no.java.moosehead.projections.Workshop;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorkshopController implements ParticipantApi {
    @Override
    public List<WorkshopInfo> workshops() {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        return workshops.stream()
                .map(ws -> ws.getWorkshopData())
                .map(wd -> new WorkshopInfo(wd.getId(),wd.getTitle(),wd.getDescription(), WorkshopStatus.FREE_SPOTS))
                .collect(Collectors.toList())
        ;
    }

    @Override
    public ParticipantActionResult reservation(String workshopid, String email, String fullname) {
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult confirmEmail(String token) {
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult cancellation(String workshopid, String email) {
        return ParticipantActionResult.ok();
    }

    @Override
    public List<ParticipantReservation> myReservations(String email) {
        return new ArrayList<>();
    }
}
