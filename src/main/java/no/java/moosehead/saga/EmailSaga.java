package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
import no.java.moosehead.eventstore.WorkshopAddedEvent;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;

import java.util.*;
import java.util.stream.Collectors;


public class EmailSaga implements EventSubscription {
    private static class WorkshopReservationInfo {
        private int spacesLeft;
        private List<ReservationAddedByUser> spaces = new LinkedList<>();
        private List<ReservationAddedByUser> waitingList = new LinkedList<>();

        private WorkshopReservationInfo(int spacesLeft) {
            this.spacesLeft = spacesLeft;
        }
    }

    private boolean sagaIsInitialized = false;
    private List<ReservationAddedByUser> unconfirmedReservations = new ArrayList<>();
    private Set<String> confirmedEmails = new HashSet<>();
    private Map<String,WorkshopReservationInfo> participants = new HashMap<>();

    private boolean addParticipant(ReservationAddedByUser res) {
        WorkshopReservationInfo workshopReservationInfo = participants.get(res.getWorkshopId());
        boolean waitingList = (!workshopReservationInfo.waitingList.isEmpty()) ||
                (workshopReservationInfo.spacesLeft < res.getNumberOfSeatsReserved());
        if (waitingList) {
            workshopReservationInfo.waitingList.add(res);
        } else {
            workshopReservationInfo.spaces.add(res);
            workshopReservationInfo.spacesLeft-=res.getNumberOfSeatsReserved();
        }
        return waitingList;
    }



    private void cancelReservation(String wsid, String email) {
        WorkshopReservationInfo workshopReservationInfo = participants.get(wsid);
        int indexOfWaitingList = indexOfReservation(workshopReservationInfo.waitingList, email);
        if (indexOfWaitingList >= 0) {
            workshopReservationInfo.waitingList.remove(indexOfWaitingList);
        } else {
            int index = indexOfReservation(workshopReservationInfo.spaces,email);
            ReservationAddedByUser remove = workshopReservationInfo.spaces.remove(index);
            workshopReservationInfo.spacesLeft+=remove.getNumberOfSeatsReserved();
        }
        EmailSender emailSender = SystemSetup.instance().emailSender();
        if (sagaIsInitialized) {
            emailSender.sendCancellationConfirmation(email, wsid);
        }
        while (!workshopReservationInfo.waitingList.isEmpty()) {
            ReservationAddedByUser waiting = workshopReservationInfo.waitingList.get(0);
            if (workshopReservationInfo.spacesLeft < waiting.getNumberOfSeatsReserved()) {
                break;
            }
            workshopReservationInfo.waitingList.remove(0);
            workshopReservationInfo.spaces.add(waiting);
            workshopReservationInfo.spacesLeft-=waiting.getNumberOfSeatsReserved();
            if (sagaIsInitialized) {
                emailSender.sendReservationConfirmation(waiting.getEmail(), wsid, waiting.getReservationToken());
            }
        }

    }


    @Override
    public void eventAdded(AbstractEvent event) {
        if(event instanceof SystemBootstrapDone) {
            sagaIsInitialized = true;
            return;
        }
        if (event instanceof WorkshopAddedEvent) {
            WorkshopAddedEvent workshopAddedEvent = (WorkshopAddedEvent) event;
            participants.put(workshopAddedEvent.getWorkshopId(),new WorkshopReservationInfo(workshopAddedEvent.getNumberOfSeats()));
        }
        if (event instanceof ReservationAddedByUser) {
            ReservationAddedByUser res = (ReservationAddedByUser) event;
            EmailSender emailSender = SystemSetup.instance().emailSender();
            if (res.getGoogleUserEmail().filter(email -> email.equals(res.getEmail())).isPresent()) {
                confirmedEmails.add(res.getGoogleUserEmail().get());
            }
            boolean emailIsConfirmed = confirmedEmails.contains(res.getEmail());
            if (!emailIsConfirmed) {
                unconfirmedReservations.add(res);
                if (sagaIsInitialized) {
                    emailSender.sendEmailConfirmation(res.getEmail(), res.getReservationToken() ,res.getWorkshopId());
                }
                return;
            }
            boolean isWaiting = addParticipant(res);
            if (sagaIsInitialized) {
                if (isWaiting) {
                    emailSender.sendWaitingListInfo(res.getEmail(), res.getWorkshopId());
                } else {
                    emailSender.sendReservationConfirmation(res.getEmail(), res.getWorkshopId(), res.getReservationToken());
                }
            }

        }
        if (event instanceof EmailConfirmedByUser) {
            EmailConfirmedByUser emailConfirmedByUser = (EmailConfirmedByUser) event;
            List<ReservationAddedByUser> toConfirm = unconfirmedReservations.stream()
                    .filter(res -> res.getEmail().equals(emailConfirmedByUser.getEmail()))
                    .collect(Collectors.toList());

            EmailSender emailSender = SystemSetup.instance().emailSender();
            for (ReservationAddedByUser reservationAddedByUser : toConfirm) {
                unconfirmedReservations.remove(reservationAddedByUser);
                boolean isWaiting = addParticipant(reservationAddedByUser);
                if (sagaIsInitialized) {
                    if (isWaiting) {
                        emailSender.sendWaitingListInfo(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId());
                    } else {
                        emailSender.sendReservationConfirmation(reservationAddedByUser.getEmail(), reservationAddedByUser.getWorkshopId(), reservationAddedByUser.getReservationToken());
                    }
                }
            }
            confirmedEmails.add(emailConfirmedByUser.getEmail());
        }
        if (event instanceof ReservationCancelledByUser) {
            ReservationCancelledByUser cancelledByUser = (ReservationCancelledByUser) event;
            Optional<ReservationAddedByUser> reservation = unconfirmedReservations.stream()
                    .filter(ur -> ur.getEmail().equals(cancelledByUser.getEmail()) && ur.getWorkshopId().equals(cancelledByUser.getWorkshopId()))
                    .findAny();
            if (reservation.isPresent()) {
                unconfirmedReservations.remove(reservation.get());
                if (sagaIsInitialized) {
                    EmailSender emailSender = SystemSetup.instance().emailSender();
                    emailSender.sendCancellationConfirmation(cancelledByUser.getEmail(), cancelledByUser.getWorkshopId());
                }
                return;
            }
            cancelReservation(cancelledByUser.getWorkshopId(),cancelledByUser.getEmail());

        }
    }

    private int indexOfReservation(List<ReservationAddedByUser> reservationAddedByUsers, String email) {
        for (int i=0;i<reservationAddedByUsers.size();i++) {
            if (email.equals(reservationAddedByUsers.get(i).getEmail())) {
                return i;
            }
        }
        return -1;
    }


}
