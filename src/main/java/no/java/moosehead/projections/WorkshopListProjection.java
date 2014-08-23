package no.java.moosehead.projections;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.repository.WorkshopData;

import java.util.*;
import java.util.stream.Collectors;

public class WorkshopListProjection implements EventSubscription {
    public List<Workshop> workshops = new ArrayList<Workshop>();
    public Set<String> confirmedEmails = new HashSet<>();


    @Override
    public void eventAdded(AbstractEvent event) {
        if (event instanceof WorkshopAddedByAdmin) {
            handleWorkshopAddedByAdmin((WorkshopAddedByAdmin) event);
        } else if (event instanceof ReservationAddedByUser) {
            handleReservationAddedByUser((ReservationAddedByUser) event);
        } else if (event instanceof ReservationCancelledByUser) {
            handleReservationCancelledByUser((ReservationCancelledByUser) event);
        } else if (event instanceof EmailConfirmedByUser) {
            handleEmailConfirmedByUser((EmailConfirmedByUser) event);
        }
    }

    private void handleWorkshopAddedByAdmin(WorkshopAddedByAdmin workshopAddedByAdmin) {
        WorkshopData workshopData = SystemSetup.instance().workshopRepository().workshopById(workshopAddedByAdmin.getWorkshopId()).get();
        workshops.add(new Workshop(workshopData,workshopAddedByAdmin.getNumberOfSeats(),workshopAddedByAdmin.getRevisionId()));
    }

    private void handleEmailConfirmedByUser(EmailConfirmedByUser emailConfirmedByUser) {
        List<Participant> toConfirm = workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(participant -> participant.getEmail().equals(emailConfirmedByUser.getEmail()))
                .collect(Collectors.toList());

        for (Participant part : toConfirm) {
            part.confirmEmail(emailConfirmedByUser);
        }
        confirmedEmails.add(emailConfirmedByUser.getEmail());
    }

    private void handleReservationCancelledByUser(ReservationCancelledByUser reservationCancelledByUser) {
        Workshop workshop = findWorkshop(reservationCancelledByUser.getWorkshopId());
        workshop.removeParticipant(reservationCancelledByUser.getEmail());
    }

    private void handleReservationAddedByUser(ReservationAddedByUser reservationAddedByUser) {
        Workshop workshop = findWorkshop(reservationAddedByUser.getWorkshopId());

        Optional<String> confirmedPart = confirmedEmails.stream()
                .filter(email -> email.equals(reservationAddedByUser.getEmail()))
                .findAny();

        Participant participant;
        if (confirmedPart.isPresent()) {
            participant = Participant.confirmedParticipant(reservationAddedByUser, workshop);
        } else {
            participant = Participant.unconfirmedParticipant(reservationAddedByUser, workshop);
        }
        workshop.addParticipant(participant);
    }

    private Workshop findWorkshop(String workshopId) {
        Optional<Workshop> optWs = workshops.stream().filter(ws -> ws.getWorkshopData().getId().equals(workshopId)).findFirst();
        if (!optWs.isPresent()) {
            throw new IllegalArgumentException("No workshop with id " + workshopId);
        }
        return optWs.get();
    }

    public boolean isEmailConfirmed(String email) {
        return confirmedEmails.contains(email);
    }

    public List<Workshop> getWorkshops() {
        return new ArrayList<Workshop>(workshops);
    }

    public Optional<Participant> findByReservationId(long reservationid) {
        return workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(pa -> pa.getReservationEventRevisionId() == reservationid)
                .findAny();
    }
    public List<Participant> findAllReservations(String email) {
        return workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(pa -> pa.getEmail().equals(email))
                .collect(Collectors.toList());
    }
}

