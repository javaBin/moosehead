package no.java.moosehead.controller;

import no.java.moosehead.MoosheadException;
import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.aggregate.WorkshopNotFoundException;
import no.java.moosehead.api.*;
import no.java.moosehead.commands.*;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.AbstractReservationAdded;
import no.java.moosehead.eventstore.AbstractReservationCancelled;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.WorkshopAddedEvent;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.web.Configuration;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopController implements ParticipantApi,AdminApi {
    @Override
    public WorkshopInfo getWorkshop(String workshopid) {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        Optional<Workshop> workshopOptional = workshops.stream()
                .filter(ws -> ws.getWorkshopData().getId().equals(workshopid))
                .findFirst();

        if (workshopOptional.isPresent()) {
            Workshop ws = workshopOptional.get();
            return createWorkshopInfo(ws);
        } else
            throw new WorkshopNotFoundException();
    }

    private WorkshopInfo createWorkshopInfo(Workshop ws) {
        WorkshopData wd = ws.getWorkshopData();
        WorkshopStatus status = computeWorkshopStatus(ws);
        return new WorkshopInfo(wd.getId(), wd.getTitle(), wd.getDescription(), ws.getParticipants(), status,ws.getWorkshopData().getWorkshopTypeEnum(),ws.getNumberOfSeats());
    }

    @Override
    public List<WorkshopInfo> workshops() {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        return workshops.stream()
                .map(this::createWorkshopInfo)
                .collect(Collectors.toList())
        ;
    }

    protected WorkshopStatus computeWorkshopStatus(Workshop ws) {
        if (Configuration.closedWorkshops().contains(ws.getWorkshopData().getId()) ||
                (ws.getWorkshopData().hasStartAndEndTime() && ws.getWorkshopData().getStartTime().isBefore(Instant.now()))) {
            return WorkshopStatus.CLOSED;
        }
        Instant openTime = Configuration.openTime().toInstant();
        Optional<Instant> registrationOpens = ws.getWorkshopData().getRegistrationOpens();
        if (registrationOpens.isPresent()) {
            openTime = registrationOpens.get();
        }
        if (openTime.isAfter(Instant.now())) {
            return WorkshopStatus.NOT_OPENED;
        }
        int confirmedParticipants = ws.getParticipants().stream()
                .filter(Participant::isEmailConfirmed)
                .mapToInt(Participant::getNumberOfSeatsReserved)
                .sum();
        int seatsLeft = ws.getNumberOfSeats() - confirmedParticipants;
        if (seatsLeft <= -Configuration.veryFullNumber()) {
            return WorkshopStatus.VERY_FULL;
        }
        if (seatsLeft <= 0) {
            return WorkshopStatus.FULL;
        }
        if (seatsLeft < Configuration.fewSpotsNumber()) {
            return WorkshopStatus.FEW_SPOTS;
        }
        return WorkshopStatus.FREE_SPOTS;
    }

    @Override
    public ParticipantActionResult reservation(WorkshopReservation workshopReservation, AuthorEnum authorEnum) {
        AddReservationCommand arc = new AddReservationCommand(workshopReservation,authorEnum);
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
            return readStatus(event.getReservationToken());
        }
        return ParticipantActionResult.confirmEmail();
    }

    @Override
    public ParticipantActionResult cancellation(String reservationId, AuthorEnum authorEnum) {

        Optional<Participant> optByReservationId = SystemSetup.instance().workshopListProjection().findByReservationToken(reservationId);

        if (!optByReservationId.isPresent()) {
            return ParticipantActionResult.error("Unknown token, reservation not found");
        }

        Participant participant = optByReservationId.get();
        CancelReservationCommand cancelReservationCommand = new CancelReservationCommand(participant.getWorkshopReservation().getEmail(), participant.getWorkshopId(), authorEnum);
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
        return readStatus(token);
    }



    @Override
    public List<ParticipantReservation> myReservations(String email) {
        List<Participant> allReservations = SystemSetup.instance().workshopListProjection().findAllReservations(email);
        WorkshopRepository workshopRepository = SystemSetup.instance().workshopRepository();
        return allReservations.stream()
                .map(pa -> {
                    Optional<WorkshopData> workshopDataOptional = workshopRepository.workshopById(pa.getWorkshopId());
                    String name = workshopDataOptional.map(wd -> wd.getTitle()).orElse("xxx");
                    int waitingListNumber = pa.waitingListNumber();
                    Optional<Integer> opwl = Optional.of(waitingListNumber).filter(wl -> wl > 0);
                    ParticipantReservationStatus status = !pa.isEmailConfirmed() ?
                            ParticipantReservationStatus.NOT_CONFIRMED :
                            waitingListNumber <= 0 ? ParticipantReservationStatus.HAS_SPACE : ParticipantReservationStatus.WAITING_LIST;
                    return new ParticipantReservation(pa.getWorkshopReservation().getEmail(), pa.getWorkshopId(), name, status, pa.getNumberOfSeatsReserved(), opwl);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ParticipantActionResult createWorkshop(WorkshopData workshopData, Instant startTime, Instant endTime, Instant openTime, int maxParticipants) {
        AddWorkshopCommand addWorkshopCommand = AddWorkshopCommand.builder()
                .withWorkshopId(workshopData.getId())
                .withWorkshopData(Optional.of(workshopData))
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withNumberOfSeats(maxParticipants)
                .withAuthor(AuthorEnum.ADMIN)
                .withWorkshopType(workshopData.getWorkshopTypeEnum())
                .create();
        WorkshopAggregate workshopAggregate = SystemSetup.instance().workshopAggregate();
        synchronized (workshopAggregate) {
            WorkshopAddedEvent event;
            try {
                event = workshopAggregate.createEvent(addWorkshopCommand);
            } catch (MoosheadException e) {
                return ParticipantActionResult.error(e.getMessage());
            }
            SystemSetup.instance().eventstore().addEvent(event);
        }
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult partialCancel(String email, String workshopid, int numSpotCanceled) {
        ParitalCancellationCommand cancellationCommand = new ParitalCancellationCommand(email, workshopid, numSpotCanceled);
        WorkshopAggregate workshopAggregate = SystemSetup.instance().workshopAggregate();
        synchronized (workshopAggregate) {
            AbstractReservationCancelled event;
            try {
                event = workshopAggregate.createEvent(cancellationCommand);
            } catch (MoosheadException e) {
                return ParticipantActionResult.error(e.getMessage());
            }
            SystemSetup.instance().eventstore().addEvent(event);
        }
        return ParticipantActionResult.ok();
    }

    private ParticipantActionResult readStatus(String token) {
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        Optional<Workshop> workshopOptional = workshops.stream()
                .filter(ws -> ws.getParticipants().stream().filter(pa -> token.equals(pa.getWorkshopReservation().getReservationToken())).findAny().isPresent())
                .findAny();
        if (!workshopOptional.isPresent()) {
            return ParticipantActionResult.error("Internal error : Did not find reservation " + token);
        }
        Workshop workshop = workshopOptional.get();
        String workshopInfo = workshop.getWorkshopData().infoText();
        Participant participant = workshop.getParticipants().stream()
                .filter(pa -> token.equals(pa.getWorkshopReservation().getReservationToken()))
                .findAny().get();

        int waitingListNumber = workshop.waitingListNumber(participant);
        if (waitingListNumber < 0) {
            return ParticipantActionResult.confirmEmail();
        }

        if (waitingListNumber == 0) {
            StringBuilder res = new StringBuilder();
            res.append("You have ");
            res.append(participant.getNumberOfSeatsReserved());
            res.append(" space");
            if (participant.getNumberOfSeatsReserved() > 1) {
                res.append("s");
            }
            res.append(" reserved at ");
            res.append(workshopInfo);
            return ParticipantActionResult.okWithMessage(res.toString());
        }

        StringBuilder res = new StringBuilder();
        res.append("You are number ");
        res.append(waitingListNumber);
        res.append(" on the waiting list for ");
        res.append(workshopInfo);
        res.append(". We will send you a mail if you get a spot later");
        return ParticipantActionResult.waitingList(res.toString());



    }
}
