package no.java.moosehead.controller;

import no.java.moosehead.MoosheadException;
import no.java.moosehead.api.*;
import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.CancelReservationCommand;
import no.java.moosehead.commands.ConfirmEmailCommand;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.web.Configuration;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopController implements ParticipantApi {
    @Override
    public List<WorkshopInfo> workshops() {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        return workshops.stream()
                .map(ws -> {
                    WorkshopData wd = ws.getWorkshopData();
                    WorkshopStatus status = computeWorkshopStatus(ws);

                    return new WorkshopInfo(wd.getId(),wd.getTitle(),wd.getDescription(), status);
                })
                .collect(Collectors.toList())
        ;
    }

    private WorkshopStatus computeWorkshopStatus(Workshop ws) {
        if (Configuration.openTime().isAfter(OffsetDateTime.now())) {
            return WorkshopStatus.NOT_OPENED;
        }
        int seatsLeft = ws.getNumberOfSeats() - ws.getParticipants().size();
        if (seatsLeft <= 0) {
            return WorkshopStatus.FULL;
        }
        if (seatsLeft < 5) {
            return WorkshopStatus.FEW_SPOTS;
        }
        return WorkshopStatus.FREE_SPOTS;
    }

    @Override
    public ParticipantActionResult reservation(String workshopid, String email, String fullname) {
        AddReservationCommand arc = new AddReservationCommand(email,fullname,workshopid);
        ReservationAddedByUser event;

        try {
            event = SystemSetup.instance().workshopAggregate().createEvent(arc);
        } catch (MoosheadException e) {
            return ParticipantActionResult.error(e.getMessage());
        }
        SystemSetup.instance().eventstore().addEvent(event);
        if (SystemSetup.instance().workshopListProjection().isEmailConfirmed(event.getEmail())) {
            return ParticipantActionResult.ok();
        }
        return ParticipantActionResult.confirmEmail();
    }

    @Override
    public ParticipantActionResult cancellation(String reservationId) {
        long id = 0;
        try {
            id = Long.parseLong(reservationId);
        } catch (NumberFormatException e) {
            return ParticipantActionResult.error("Unknown token, reservation not found");
        }
        Optional<Participant> optByReservationId = SystemSetup.instance().workshopListProjection().findByReservationId(id);

        if (!optByReservationId.isPresent()) {
            return ParticipantActionResult.error("Unknown token, reservation not found");
        }

        Participant participant = optByReservationId.get();
        CancelReservationCommand cancelReservationCommand = new CancelReservationCommand(participant.getEmail(), participant.getWorkshopId());
        ReservationCancelledByUser event;
        try {
            event = SystemSetup.instance().workshopAggregate().createEvent(cancelReservationCommand);
        } catch (MoosheadException e) {
            return ParticipantActionResult.error(e.getMessage());
        }
        SystemSetup.instance().eventstore().addEvent(event);
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult confirmEmail(String token) {
        long id = Long.parseLong(token);
        ConfirmEmailCommand confirmEmailCommand = new ConfirmEmailCommand(id);
        EmailConfirmedByUser emailConfirmedByUser = SystemSetup.instance().workshopAggregate().createEvent(confirmEmailCommand);
        SystemSetup.instance().eventstore().addEvent(emailConfirmedByUser);
        return ParticipantActionResult.ok();
    }



    @Override
    public List<ParticipantReservation> myReservations(String email) {
        List<Participant> allReservations = SystemSetup.instance().workshopListProjection().findAllReservations(email);
        WorkshopRepository workshopRepository = SystemSetup.instance().workshopRepository();
        return allReservations.stream()
                .map(pa -> {
                    Optional<WorkshopData> workshopDataOptional = workshopRepository.workshopById(pa.getWorkshopId());
                    String name = workshopDataOptional.map(wd -> wd.getTitle()).orElse("xxx");
                    ParticipantReservationStatus status = !pa.isEmailConfirmed() ?
                            ParticipantReservationStatus.NOT_CONFIRMED :
                            pa.waitingListNumber() <= 0 ? ParticipantReservationStatus.HAS_SPACE : ParticipantReservationStatus.WAITING_LIST;
                    return new ParticipantReservation(pa.getEmail(), pa.getWorkshopId(), name, status);
                })
                .collect(Collectors.toList());
    }
}
