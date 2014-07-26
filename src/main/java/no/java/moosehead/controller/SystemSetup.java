package no.java.moosehead.controller;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;

import java.util.List;

public class SystemSetup {
    private static final SystemSetup setup = new SystemSetup();

    private Eventstore eventstore;
    private WorkshopRepository workshopRepository;
    private final WorkshopAggregate workshopAggregate;

    private SystemSetup() {
        eventstore = new Eventstore();
        workshopRepository = new WorkshopRepository();
        workshopAggregate = new WorkshopAggregate();
        eventstore.addEventSubscriber(workshopAggregate);
        createAllWorkshops();
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
}
