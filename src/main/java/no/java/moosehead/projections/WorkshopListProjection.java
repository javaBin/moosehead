package no.java.moosehead.projections;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
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
        if (event instanceof WorkshopAddedEvent) {
            handleWorkshopAdded((WorkshopAddedEvent) event);
        } else if (event instanceof AbstractReservationAdded) {
            handleReservationAdded((AbstractReservationAdded) event);
        } else if (event instanceof AbstractReservationCancelled) {
            handleReservationCancelled((AbstractReservationCancelled) event);
        } else if (event instanceof EmailConfirmedByUser) {
            handleEmailConfirmedByUser((EmailConfirmedByUser) event);
        }
    }

    private void handleWorkshopAdded(WorkshopAddedEvent workshopAdded) {
        WorkshopData workshopData = SystemSetup.instance().workshopRepository().workshopById(workshopAdded.getWorkshopId()).get();
        workshops.add(new Workshop(workshopData, workshopAdded.getNumberOfSeats(), workshopAdded.getRevisionId()));
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

    private void handleReservationCancelled(AbstractReservationCancelled reservationCancelled) {
        Workshop workshop = findWorkshop(reservationCancelled.getWorkshopId());
        workshop.removeParticipant(reservationCancelled.getEmail());
    }

    private void handleReservationAdded(AbstractReservationAdded reservationAdded) {
        Workshop workshop = findWorkshop(reservationAdded.getWorkshopId());
        Participant participant;

        if (reservationAdded instanceof ReservationAddedByUser) {
            ReservationAddedByUser reservationAddedByUser = (ReservationAddedByUser) reservationAdded;
            Optional<String> confirmedPart = confirmedEmails.stream()
                    .filter(email -> email.equals(reservationAdded.getEmail()))
                    .findAny();

            if (confirmedPart.isPresent()) {
                participant = Participant.confirmedParticipant(reservationAddedByUser, workshop);
            } else {
                participant = Participant.unconfirmedParticipant(reservationAddedByUser, workshop);
            }
        }else {
            // if added by admin we belive the email to be correct
            participant = Participant.confirmedParticipant(reservationAdded, workshop);
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

