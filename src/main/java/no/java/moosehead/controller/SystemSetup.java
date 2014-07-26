package no.java.moosehead.controller;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.projections.WorkshopListProjection;

import java.util.List;

public class SystemSetup {
    private static final SystemSetup setup = new SystemSetup();

    static {
        setup.createAllWorkshops();
    }

    private Eventstore eventstore;
    private WorkshopRepository workshopRepository;
    private WorkshopAggregate workshopAggregate;
    private WorkshopController workshopController;
    private final WorkshopListProjection workshopListProjection;

    private SystemSetup() {
        eventstore = new Eventstore();
        workshopRepository = new WorkshopRepository();
        workshopAggregate = new WorkshopAggregate();
        workshopListProjection = new WorkshopListProjection();
        eventstore.addEventSubscriber(workshopAggregate);
        eventstore.addEventSubscriber(workshopListProjection);
        workshopController = new WorkshopController();
    }



    private void createAllWorkshops() {
        List<WorkshopData> workshopDatas = workshopRepository.allWorkshops();
        workshopDatas.forEach(wd -> {
            AddWorkshopCommand addWorkshopCommand = new AddWorkshopCommand(wd.getId());
            WorkshopAddedByAdmin event = workshopAggregate.createEvent(addWorkshopCommand);
            eventstore.addEvent(event);
        });
    }


    public static Eventstore eventstore() {
        return setup.eventstore;
    }

    public static WorkshopRepository workshopRepository() {
        return setup.workshopRepository;
    }

    public static WorkshopController workshopController() {
        return setup.workshopController;
    }

    public static WorkshopListProjection workshopListProjection() {
        return setup.workshopListProjection;
    }
}
