package no.java.moosehead.projections;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.repository.WorkshopData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkshopListProjection implements EventSubscription {
    public List<Workshop> workshops = new ArrayList<Workshop>();

    @Override
    public void eventAdded(AbstractEvent event) {
        if (event instanceof WorkshopAddedByAdmin) {
            WorkshopAddedByAdmin workshopAddedByAdmin = (WorkshopAddedByAdmin) event;
            WorkshopData workshopData = SystemSetup.instance().workshopRepository().workshopById(workshopAddedByAdmin.getWorkshopId()).get();
            workshops.add(new Workshop(workshopData));
        } else if (event instanceof ReservationAddedByUser) {
            ReservationAddedByUser reservationAddedByUser = (ReservationAddedByUser) event;
            Optional<Workshop> optWs = workshops.stream().filter(ws -> ws.getWorkshopData().getId().equals(reservationAddedByUser.getWorkshopId())).findFirst();
            if (!optWs.isPresent()) {
                throw new IllegalArgumentException("No workshop with id " + reservationAddedByUser.getWorkshopId());
            }
            Workshop workshop = optWs.get();
            Participant participant = new Participant(reservationAddedByUser.getEmail(), reservationAddedByUser.getFullname());
            workshop.addParticipant(participant);
        }
    }

    public List<Workshop> getWorkshops() {
        return new ArrayList<Workshop>(workshops);
    }
}

