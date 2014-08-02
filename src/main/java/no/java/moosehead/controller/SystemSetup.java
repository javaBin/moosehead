package no.java.moosehead.controller;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.projections.WorkshopListProjection;
import no.java.moosehead.saga.EmailSender;

import java.util.List;

public class SystemSetup {
    private static SystemSetup setup;

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


    private static void ensureInit() {
        if (setup == null) {
            setup = new SystemSetup();
            setup.createAllWorkshops();
        }
    }

    public static SystemSetup instance() {
        ensureInit();
        return setup;
    }

    public static void setSetup(SystemSetup setup) {
        SystemSetup.setup = setup;
    }

    public Eventstore eventstore() {
        return eventstore;
    }

    public WorkshopRepository workshopRepository() {
        return workshopRepository;
    }

    public WorkshopController workshopController() {
        return setup.workshopController;
    }

    public WorkshopListProjection workshopListProjection() {
        return setup.workshopListProjection;
    }

    public WorkshopAggregate workshopAggregate() {
        return workshopAggregate;
    }

    public EmailSender emailSender() {
        return null;
    };

}
