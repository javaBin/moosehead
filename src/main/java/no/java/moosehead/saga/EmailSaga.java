package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;

import java.util.*;
import java.util.stream.Collectors;


public class EmailSaga implements EventSubscription {
    private static class ReservationInfo {
        private final ReservationAddedByUser res;
        private int spacesReserved;

        public ReservationInfo(ReservationAddedByUser res) {
            this.res = res;
            this.spacesReserved = res.getNumberOfSeatsReserved();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ReservationInfo)) {
                return false;
            }
            return res.equals(((ReservationInfo) obj).res);
        }

        @Override
        public int hashCode() {
            return res.hashCode();
        }
    }
    private static class WorkshopReservationInfo {
        private int spacesLeft;
        private int totalSize;
        private List<ReservationInfo> spaces = new LinkedList<>();
        private List<ReservationInfo> waitingList = new LinkedList<>();

        private WorkshopReservationInfo(int spacesLeft) {
            this.spacesLeft = spacesLeft;
            this.totalSize = spacesLeft;
        }
    }

    private boolean sagaIsInitialized = false;
    private List<ReservationInfo> unconfirmedReservations = new ArrayList<>();
    private Set<String> confirmedEmails = new HashSet<>();
    private Map<String,WorkshopReservationInfo> participants = new HashMap<>();

    private boolean addParticipant(ReservationInfo res) {
        WorkshopReservationInfo workshopReservationInfo = participants.get(res.res.getWorkshopId());
        boolean waitingList = (!workshopReservationInfo.waitingList.isEmpty()) ||
                (workshopReservationInfo.spacesLeft < res.spacesReserved);
        if (waitingList) {
            workshopReservationInfo.waitingList.add(res);
        } else {
            workshopReservationInfo.spaces.add(res);
            workshopReservationInfo.spacesLeft-=res.spacesReserved;
        }
        return waitingList;
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
                unconfirmedReservations.add(new ReservationInfo(res));
                if (sagaIsInitialized) {
                    emailSender.sendEmailConfirmation(res.getEmail(), res.getReservationToken() ,res.getWorkshopId());
                }
                return;
            }
            boolean isWaiting = addParticipant(new ReservationInfo(res));
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
            List<ReservationInfo> toConfirm = unconfirmedReservations.stream()
                    .filter(res -> res.res.getEmail().equals(emailConfirmedByUser.getEmail()))
                    .collect(Collectors.toList());

            EmailSender emailSender = SystemSetup.instance().emailSender();
            for (ReservationInfo reservationInfo : toConfirm) {
                unconfirmedReservations.remove(reservationInfo);
                boolean isWaiting = addParticipant(reservationInfo);
                if (sagaIsInitialized) {
                    if (isWaiting) {
                        emailSender.sendWaitingListInfo(reservationInfo.res.getEmail(), reservationInfo.res.getWorkshopId());
                    } else {
                        emailSender.sendReservationConfirmation(reservationInfo.res.getEmail(), reservationInfo.res.getWorkshopId(), reservationInfo.res.getReservationToken());
                    }
                }
            }
            confirmedEmails.add(emailConfirmedByUser.getEmail());
        }
        if (event instanceof ReservationCancelledByUser) {
            ReservationCancelledByUser cancelledByUser = (ReservationCancelledByUser) event;
            Optional<ReservationInfo> reservation = findUnconfirmedReservation(cancelledByUser.getEmail(),cancelledByUser.getWorkshopId());
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
        if (event instanceof ReservationPartallyCancelled) {
            ReservationPartallyCancelled reservationPartallyCancelled = (ReservationPartallyCancelled) event;
            Optional<ReservationInfo> reservation = findUnconfirmedReservation(reservationPartallyCancelled.getEmail(),reservationPartallyCancelled.getWorkshopId());
            if (reservation.isPresent()) {
                reservation.get().spacesReserved-=reservationPartallyCancelled.getNumSpotsCancelled();
                return;
            }
            partCancel(reservationPartallyCancelled);
        }
        if (event instanceof WorkshopSizeChangedByAdmin) {
            WorkshopSizeChangedByAdmin workshopSizeChangedByAdmin = (WorkshopSizeChangedByAdmin) event;
            WorkshopReservationInfo workshopReservationInfo = participants.get(workshopSizeChangedByAdmin.getWorkshopid());
            int newPlaces = workshopSizeChangedByAdmin.getNumspaces() - workshopReservationInfo.totalSize;
            workshopReservationInfo.spacesLeft = workshopReservationInfo.spacesLeft + newPlaces;
            workshopReservationInfo.totalSize = workshopSizeChangedByAdmin.getNumspaces();
            while (workshopReservationInfo.spacesLeft > 0) {
                if (workshopReservationInfo.waitingList.isEmpty()) {
                    break;
                }
                ReservationInfo reservationInfo = workshopReservationInfo.waitingList.get(0);
                if (reservationInfo.spacesReserved > workshopReservationInfo.spacesLeft) {
                    break;
                }
                workshopReservationInfo.waitingList.remove(0);
                workshopReservationInfo.spaces.add(reservationInfo);
                workshopReservationInfo.spacesLeft -= reservationInfo.spacesReserved;
                if (sagaIsInitialized) {
                    EmailSender emailSender = SystemSetup.instance().emailSender();
                    emailSender.sendReservationConfirmation(reservationInfo.res.getEmail(),workshopSizeChangedByAdmin.getWorkshopid(),reservationInfo.res.getReservationToken());
                }
            }

        }
    }

    private void partCancel(ReservationPartallyCancelled reservationPartallyCancelled) {
        WorkshopReservationInfo workshopReservationInfo = participants.get(reservationPartallyCancelled.getWorkshopId());
        int indexOfWaitingList = indexOfReservation(workshopReservationInfo.waitingList, reservationPartallyCancelled.getEmail());
        if (indexOfWaitingList >= 0) {
            workshopReservationInfo.waitingList.get(indexOfWaitingList).spacesReserved-=reservationPartallyCancelled.getNumSpotsCancelled();
            return;
        }
        int pos = indexOfReservation(workshopReservationInfo.spaces,reservationPartallyCancelled.getEmail());
        workshopReservationInfo.spaces.get(pos).spacesReserved-=reservationPartallyCancelled.getNumSpotsCancelled();
        workshopReservationInfo.spacesLeft+=reservationPartallyCancelled.getNumSpotsCancelled();
        EmailSender emailSender = SystemSetup.instance().emailSender();

        while (!workshopReservationInfo.waitingList.isEmpty()) {
            ReservationInfo waiting = workshopReservationInfo.waitingList.get(0);
            if (workshopReservationInfo.spacesLeft < waiting.spacesReserved) {
                break;
            }
            workshopReservationInfo.waitingList.remove(0);
            workshopReservationInfo.spaces.add(waiting);
            workshopReservationInfo.spacesLeft-=waiting.spacesReserved;
            if (sagaIsInitialized) {
                emailSender.sendReservationConfirmation(waiting.res.getEmail(), reservationPartallyCancelled.getWorkshopId(), waiting.res.getReservationToken());
            }
        }
    }


    private void cancelReservation(String wsid, String email) {
        WorkshopReservationInfo workshopReservationInfo = participants.get(wsid);
        int indexOfWaitingList = indexOfReservation(workshopReservationInfo.waitingList, email);
        if (indexOfWaitingList >= 0) {
            workshopReservationInfo.waitingList.remove(indexOfWaitingList);
        } else {
            int index = indexOfReservation(workshopReservationInfo.spaces,email);
            ReservationInfo remove = workshopReservationInfo.spaces.remove(index);
            workshopReservationInfo.spacesLeft+=remove.spacesReserved;
        }
        EmailSender emailSender = SystemSetup.instance().emailSender();
        if (sagaIsInitialized) {
            emailSender.sendCancellationConfirmation(email, wsid);
        }
        while (!workshopReservationInfo.waitingList.isEmpty()) {
            ReservationInfo waiting = workshopReservationInfo.waitingList.get(0);
            if (workshopReservationInfo.spacesLeft < waiting.spacesReserved) {
                break;
            }
            workshopReservationInfo.waitingList.remove(0);
            workshopReservationInfo.spaces.add(waiting);
            workshopReservationInfo.spacesLeft-=waiting.spacesReserved;
            if (sagaIsInitialized) {
                emailSender.sendReservationConfirmation(waiting.res.getEmail(), wsid, waiting.res.getReservationToken());
            }
        }

    }


    private Optional<ReservationInfo> findUnconfirmedReservation(String enail,String workshopid) {
        return unconfirmedReservations.stream()
                        .filter(ur -> ur.res.getEmail().equals(enail) && ur.res.getWorkshopId().equals(workshopid))
                        .findAny();
    }

    private int indexOfReservation(List<ReservationInfo> reservationAddedByUsers, String email) {
        for (int i=0;i<reservationAddedByUsers.size();i++) {
            if (email.equals(reservationAddedByUsers.get(i).res.getEmail())) {
                return i;
            }
        }
        return -1;
    }


}
