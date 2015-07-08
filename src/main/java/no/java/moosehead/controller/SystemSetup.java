package no.java.moosehead.controller;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.WorkshopAddedEvent;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.projections.WorkshopListProjection;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import no.java.moosehead.saga.*;
import no.java.moosehead.web.Configuration;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class SystemSetup {
    private static SystemSetup setup = new SystemSetup();


    private Eventstore eventstore;
    private WorkshopRepository workshopRepository;
    private WorkshopAggregate workshopAggregate;
    private WorkshopController workshopController;
    private WorkshopListProjection workshopListProjection;
    private EmailSender emailSender;
    private TokenGenerator tokenGenerator;
    private boolean initLoaded = false;

    private SystemSetup() {

    }

    private synchronized void setup() {
        if (!needToLoadSetup()) {
            return;
        }
        initLoaded = true;
        tokenGenerator = new TokenGenerator();
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

        new ManualConfirmationSender(emailSender).doManual(eventstore);

        eventstore.playbackEventsToSubscribers();

    }

    public boolean needToLoadSetup() {
        return !initLoaded;
    }



    private void createAllWorkshops() {
        List<WorkshopData> workshopDatas = workshopRepository.allWorkshops();
        workshopDatas.forEach(wd -> {
            AddWorkshopCommand addWorkshopCommand;
            if (wd.hasStartAndEndTime()) {
                addWorkshopCommand = new AddWorkshopCommand(wd.getId(), AddWorkshopCommand.Author.SYSTEM, Configuration.placesPerWorkshop(), wd.getStartTime(), wd.getEndTime());
            } else {
                addWorkshopCommand = new AddWorkshopCommand(wd.getId(), AddWorkshopCommand.Author.SYSTEM, Configuration.placesPerWorkshop());
            }
            WorkshopAddedEvent event = workshopAggregate.createEvent(addWorkshopCommand);
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


    public static void main(String[] args) {
        System.setProperty("mooseheadConfFile",args[0]);
        List<String> ids = Arrays.asList(
          "angularjs_crash_course",
          "hands_on_elasticsearch__kibana",
          "introduksjon_til_docker",
          "workshop_designing_event_sourced",
          "a_handson_introduction_to_neo4j",
          "akka_handson"
        );
        List<Workshop> workshops = SystemSetup.instance().workshopListProjection().getWorkshops();
        workshops.stream()
                .sequential()
                .filter(ws -> ids.contains(ws.getWorkshopData().getId()))
                .forEach(ws -> {
                    System.out.println(ws.getWorkshopData().getId());
                    ws.getParticipants().stream()
                            .filter(pa -> pa.isEmailConfirmed() && !pa.isWaiting())
                            .forEach(pa -> {
                                SystemSetup.instance().emailSender().send(EmailType.WELCOME,pa.getEmail(),new Hashtable<>());
                                System.out.println(pa.getWorkshopId() + ";" + pa.getEmail());
                            });
                });



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

    public TokenGenerator revisionGenerator() {
        return tokenGenerator;
    }

}
