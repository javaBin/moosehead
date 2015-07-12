package no.java.moosehead.controller;

import no.java.moosehead.MoosheadException;
import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.aggregate.WorkshopNotFoundException;
import no.java.moosehead.api.*;
import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.Author;
import no.java.moosehead.commands.CancelReservationCommand;
import no.java.moosehead.commands.ConfirmEmailCommand;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.web.Configuration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopController implements ParticipantApi {
    @Override
    public WorkshopInfo getWorkshop(String workshopid) {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        Optional<Workshop> workshopOptional = workshops.stream()
                .filter(ws -> ws.getWorkshopData().getId().equals(workshopid))
                .findFirst();

        if (workshopOptional.isPresent()) {
            Workshop ws = workshopOptional.get();
            WorkshopData wd = ws.getWorkshopData();
            WorkshopStatus status = computeWorkshopStatus(ws);
            return new WorkshopInfo(wd.getId(), wd.getTitle(), wd.getDescription(), ws.getParticipants(), status,ws.getCreatedRevisionId());
        } else
            throw new WorkshopNotFoundException();
    }

    @Override
    public List<WorkshopInfo> workshops() {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        return workshops.stream()
                .map(ws -> {
                    WorkshopData wd = ws.getWorkshopData();
                    WorkshopStatus status = computeWorkshopStatus(ws);

                    return new WorkshopInfo(wd.getId(),wd.getTitle(),wd.getDescription(), ws.getParticipants(), status,ws.getCreatedRevisionId());
                })
                .collect(Collectors.toList())
        ;
    }

    protected WorkshopStatus computeWorkshopStatus(Workshop ws) {
        if (Configuration.closedWorkshops().contains(ws.getWorkshopData().getId()) ||
                (ws.getWorkshopData().hasStartAndEndTime() && ws.getWorkshopData().getStartTime().isBefore(Instant.now()))) {
            return WorkshopStatus.CLOSED;
        }
        if (Configuration.openTime().isAfter(OffsetDateTime.now())) {
            return WorkshopStatus.NOT_OPENED;
        }
        int confirmedParticipants = (int) ws.getParticipants().stream().filter(pa -> pa.isEmailConfirmed()).count();
        int seatsLeft = ws.getNumberOfSeats() - confirmedParticipants;
        if (seatsLeft <= -Configuration.veryFullNumber()) {
            return WorkshopStatus.VERY_FULL;
        }
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
        AddReservationCommand arc = new AddReservationCommand(email,fullname,workshopid, Author.USER);
        AbstractReservationAdded event;

        WorkshopAggregate workshopAggregate = SystemSetup.instance().workshopAggregate();
        synchronized (workshopAggregate) {
            try {
                event = workshopAggregate.createEvent(arc);
            } catch (MoosheadException e) {
                return ParticipantActionResult.error(e.getMessage());
            }
            SystemSetup.instance().eventstore().addEvent(event);
        }
        if (SystemSetup.instance().workshopListProjection().isEmailConfirmed(event.getEmail())) {
            return ParticipantActionResult.ok();
        }
        return ParticipantActionResult.confirmEmail();
    }

    @Override
    public ParticipantActionResult cancellation(String reservationId) {
        long id;
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
        CancelReservationCommand cancelReservationCommand = new CancelReservationCommand(participant.getEmail(), participant.getWorkshopId(), Author.USER);
        AbstractReservationCancelled event;
        WorkshopAggregate workshopAggregate = SystemSetup.instance().workshopAggregate();
        synchronized (workshopAggregate) {
            try {
                event = workshopAggregate.createEvent(cancelReservationCommand);
            } catch (MoosheadException e) {
                return ParticipantActionResult.error(e.getMessage());
            }
            SystemSetup.instance().eventstore().addEvent(event);
        }
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult confirmEmail(String token) {

        ConfirmEmailCommand confirmEmailCommand = new ConfirmEmailCommand(token);
        EmailConfirmedByUser emailConfirmedByUser;
        WorkshopAggregate workshopAggregate = SystemSetup.instance().workshopAggregate();
        synchronized (workshopAggregate) {
            try {
                emailConfirmedByUser = workshopAggregate.createEvent(confirmEmailCommand);
            } catch (MoosheadException e) {
                return ParticipantActionResult.error(e.getMessage());
            }
            SystemSetup.instance().eventstore().addEvent(emailConfirmedByUser);
        }
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
