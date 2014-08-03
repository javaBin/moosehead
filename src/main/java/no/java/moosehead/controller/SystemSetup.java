package no.java.moosehead.controller;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.RevisionGenerator;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.projections.WorkshopListProjection;
import no.java.moosehead.saga.DummyEmailSender;
import no.java.moosehead.saga.EmailSaga;
import no.java.moosehead.saga.EmailSender;
import no.java.moosehead.saga.SmtpEmailSender;
import no.java.moosehead.web.Configuration;

import java.util.List;

public class SystemSetup {
    private static SystemSetup setup = new SystemSetup();


    private Eventstore eventstore;
    private WorkshopRepository workshopRepository;
    private WorkshopAggregate workshopAggregate;
    private WorkshopController workshopController;
    private WorkshopListProjection workshopListProjection;
    private EmailSender emailSender;
    private RevisionGenerator revisionGenerator;
    private boolean initLoaded = false;

    private SystemSetup() {

    }

    private synchronized void setup() {
        if (!needToLoadSetup()) {
            return;
        }
        initLoaded = true;
        revisionGenerator = new RevisionGenerator();
        if (Configuration.eventstoreFilename() != null) {
            eventstore = new Eventstore(new FileHandler(Configuration.eventstoreFilename()));
        } else {
            eventstore = new Eventstore();
        }
        workshopRepository = new WorkshopRepository();
        workshopAggregate = new WorkshopAggregate();
        workshopListProjection = new WorkshopListProjection();
        eventstore.addEventSubscriber(workshopAggregate);
        eventstore.addEventSubscriber(workshopListProjection);
        eventstore.addEventSubscriber(new EmailSaga());
        workshopController = new WorkshopController();
        emailSender = Configuration.smtpServer() != null ? new SmtpEmailSender() : new DummyEmailSender();

        if (eventstore.numberOfWorkshops() == 0L) {
            createAllWorkshops();
        }

        eventstore.playbackEventsToSubscribers();



    }

    public boolean needToLoadSetup() {
        return !initLoaded;
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
        setup.setup();
    }

    public static SystemSetup instance() {
        ensureInit();
        return setup;
    }

    public static void setSetup(SystemSetup setup) {
        SystemSetup.setup = setup;
        if (setup != null) {
            setup.setInitLoaded();
        }

    }

    private void setInitLoaded() {
        initLoaded = setup != null;
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
        return emailSender;
    };

    public RevisionGenerator revisionGenerator() {
        return revisionGenerator;
    }

}
