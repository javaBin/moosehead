package no.java.moosehead.projections;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.repository.WorkshopData;

import java.util.ArrayList;
import java.util.List;

public class WorkshopListProjection implements EventSubscription {
    public List<Workshop> workshops = new ArrayList<Workshop>();

    @Override
    public void eventAdded(AbstractEvent event) {
        if (event instanceof WorkshopAddedByAdmin) {
            WorkshopAddedByAdmin workshopAddedByAdmin = (WorkshopAddedByAdmin) event;
            WorkshopData workshopData = SystemSetup.instance().workshopRepository().workshopById(workshopAddedByAdmin.getWorkshopId()).get();
            workshops.add(new Workshop(workshopData));
        }
    }

    public List<Workshop> getWorkshops() {
        return new ArrayList<Workshop>(workshops);
    }
}

