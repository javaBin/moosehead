package no.java.moosehead.projections;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.domain.WorkshopReservation;
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
        } else if (event instanceof ShowUpRegisteredByAdmin) {
            handleShowUp((ShowUpRegisteredByAdmin) event);
        }
    }

    private void handleShowUp(ShowUpRegisteredByAdmin showUpRegisteredByAdmin) {
        Optional<Participant> participantOptional = workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(part -> showUpRegisteredByAdmin.getReservationToken()
                        .equals(Optional.ofNullable(part.getWorkshopReservation()).map(WorkshopReservation::getReservationToken).orElse(null)))
                .findAny();
        if (!participantOptional.isPresent()) {
            System.out.println("Warning did not find reservation with token " + showUpRegisteredByAdmin.getReservationToken() + ". Ignoring");
            return;
        }
        participantOptional.get().setHasShownUp(showUpRegisteredByAdmin.isShownUp());
    }

    private void handleWorkshopAdded(WorkshopAddedEvent workshopAdded) {
        Optional<WorkshopData> dataOptional = workshopAdded.getWorkshopData();
        WorkshopData workshopData;
        if (dataOptional.isPresent()) {
            workshopData = dataOptional.get();
        } else {
            Optional<WorkshopData> workshopDataOptional = SystemSetup.instance().workshopRepository().workshopById(workshopAdded.getWorkshopId());
            if (!workshopDataOptional.isPresent()) {
                System.out.println("Warning did not find event " + workshopAdded.getWorkshopId() + ". Ignoring");
                return;
            }
            workshopData = workshopDataOptional.get();
        }
        workshops.add(new Workshop(workshopData, workshopAdded.getNumberOfSeats()));
    }

    private void handleEmailConfirmedByUser(EmailConfirmedByUser emailConfirmedByUser) {
        List<Participant> toConfirm = workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(participant -> participant.getWorkshopReservation().getEmail().equals(emailConfirmedByUser.getEmail()))
                .collect(Collectors.toList());

        for (Participant part : toConfirm) {
            part.confirmEmail(emailConfirmedByUser);
        }
        confirmedEmails.add(emailConfirmedByUser.getEmail());
    }

    private void handleReservationCancelled(AbstractReservationCancelled reservationCancelled) {
        Workshop workshop = findWorkshop(reservationCancelled.getWorkshopId());
        Optional<Participant> participantOptional = workshop.getParticipants().stream()
                .filter(participant -> participant.getWorkshopReservation().getEmail().equals(reservationCancelled.getEmail()))
                .findAny();
        if (!participantOptional.isPresent()) {
            return;
        }
        Participant participant = participantOptional.get();
        if (participant.getNumberOfSeatsReserved() == reservationCancelled.getNumSpotsCancelled() || reservationCancelled.getNumSpotsCancelled() == 0) {
            workshop.removeParticipant(reservationCancelled.getEmail());
        } else {
            participant.reduceReservedSeats(reservationCancelled.getNumSpotsCancelled());
        }
    }


    private void handleReservationAdded(AbstractReservationAdded reservationAdded) {
        Workshop workshop = findWorkshop(reservationAdded.getWorkshopId());
        Participant participant;

        if (reservationAdded instanceof ReservationAddedByUser) {
            ReservationAddedByUser reservationAddedByUser = (ReservationAddedByUser) reservationAdded;
            boolean isReservingWithGoogle = reservingWithGoogle(reservationAddedByUser);
            if (isReservingWithGoogle) {
                confirmedEmails.add(reservationAddedByUser.getGoogleUserEmail().get());
            }
            if (isReservingWithGoogle || hasConfirmedBefore(reservationAdded)) {
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

    private boolean reservingWithGoogle(ReservationAddedByUser reservationAddedByUser) {
        Optional<String> googleUserEmail = reservationAddedByUser.getGoogleUserEmail();
        return googleUserEmail.isPresent() && googleUserEmail.get().equals(reservationAddedByUser.getEmail());
    }

    private boolean hasConfirmedBefore(AbstractReservationAdded reservationAdded) {
        Optional<String> confirmedPart = confirmedEmails.stream()
                .filter(email -> email.equals(reservationAdded.getEmail()))
                .findAny();

        return confirmedPart.isPresent();
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
        return new ArrayList<>(workshops);
    }

    public Optional<Participant> findByReservationToken(String reservationToken) {
        Optional<Participant>  op = workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(pa -> pa.getWorkshopReservation().getReservationToken().equals(reservationToken))
                .findFirst();
        return op;
    }
    public List<Participant> findAllReservations(String email) {
        return workshops.stream()
                .flatMap(ws -> ws.getParticipants().stream())
                .filter(pa -> pa.getWorkshopReservation().getEmail().equals(email))
                .collect(Collectors.toList());
    }
}

