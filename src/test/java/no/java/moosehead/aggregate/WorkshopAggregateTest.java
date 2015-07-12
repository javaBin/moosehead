package no.java.moosehead.aggregate;

import no.java.moosehead.commands.*;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import no.java.moosehead.web.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

public class WorkshopAggregateTest {

    private final String w2 = "W2";
    private final String w1 = "W1";

    private Eventstore eventstore;
    private WorkshopAggregate workshopAggregate;

    @Before
    public void beforeTest() {
        SystemSetup systemSetup = Mockito.mock(SystemSetup.class);
        TokenGenerator tokenGenerator = new TokenGenerator();
        Mockito.when(systemSetup.revisionGenerator()).thenReturn(tokenGenerator);
        SystemSetup.setSetup(systemSetup);
        eventstore = new Eventstore(new FileHandler());
        workshopAggregate = new WorkshopAggregate();
        eventstore.addEventSubscriber(workshopAggregate);



        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime opens = now.atOffset(ZoneOffset.ofHours(2)).minusDays(2);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        String datestr = opens.format(format);

        Map<String,String> confdata = new HashMap<>();
        confdata.put("openTime",datestr);
        Configuration.initData(confdata);
    }

    @Test()
    public void workshopAddedByAdminShouldBeOfTypeWorkshopAddedByAdmin() {
        AddWorkshopCommand command = new AddWorkshopCommand(w1, Author.ADMIN, 10);
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(WorkshopAddedByAdmin.class);
    }

    @Test()
    public void workshopAddedBySystemShouldBeOfTypeWorkshopAddedBySystem() {
        AddWorkshopCommand command = new AddWorkshopCommand(w1, Author.SYSTEM, 10);
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(WorkshopAddedBySystem.class);
    }

    @Test(expected = WorkshopCanNotBeAddedException.class)
    public void workshopShouldNotBeAddedWhenItExistsAlready() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddWorkshopCommand command = new AddWorkshopCommand(w1, Author.ADMIN, 10);
        workshopAggregate.createEvent(command);
    }

    @Test
    public void aUniqueWorkshopShouldBeAdded() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddWorkshopCommand command = new AddWorkshopCommand(w2, Author.ADMIN, 10);
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event.getWorkshopId()).isEqualTo(w2);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void ReservationIsNotOkWhenWorkshopDoesNotExists() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w2, Author.USER);
        workshopAggregate.createEvent(cmd);
    }

    @Test
    public void ReservationIsOkWhenWorkshopExists() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w1, Author.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);
    }

    @Test
    public void multipleReservationsAreOk() throws Exception {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w1, Author.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);
        eventstore.addEvent(reservationAddedByUser);
        AddReservationCommand cmd2 = new AddReservationCommand("haha@email","Darth Vader",w1, Author.USER);
        AbstractReservationAdded reservationAddedByUser2 =  workshopAggregate.createEvent(cmd2);
        assertThat(reservationAddedByUser2.getWorkshopId()).isEqualTo(w1);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void sameEmailIsNotAllowedToReserveTwice() throws Exception {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w1, Author.USER);
        AbstractReservationAdded event = workshopAggregate.createEvent(cmd);
        eventstore.addEvent(event);
        AddReservationCommand cmd2 = new AddReservationCommand("bla@email","Donnie Darko",w1, Author.USER);
        workshopAggregate.createEvent(cmd2);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void youShoudNotBeAllowedToRegisterBeforeRegistartionOpen() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime opens = now.atOffset(ZoneOffset.ofHours(2)).plusDays(2);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        String datestr = opens.format(format);
        Map<String,String> confdata = new HashMap<>();
        confdata.put("openTime",datestr);
        Configuration.initData(confdata);

        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w1, Author.USER);
        workshopAggregate.createEvent(cmd);

    }



    @Test
    public void shouldBeAbleToCancelReservation() throws Exception {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w1, Author.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        eventstore.addEvent(reservationAddedByUser);

        CancelReservationCommand cancel = new CancelReservationCommand("bla@email",w1, Author.USER);
        AbstractReservationCancelled rcbu = workshopAggregate.createEvent(cancel);

        assertThat(rcbu).isNotNull();
    }

   

    @Test(expected = NoReservationFoundException.class)
    public void shouldNotBeAbleToCancelNonExsistingReservation() throws Exception {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));

        CancelReservationCommand cancel = new CancelReservationCommand("bla@email",w1, Author.USER);
        workshopAggregate.createEvent(cancel);

    }

    @Test
    public void shouldConfirmEmail() throws Exception {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "bal@gmail.com", "Darth Vader", w1);
        eventstore.addEvent(reservationAddedByUser);
        ConfirmEmailCommand confirmEmailCommand = new ConfirmEmailCommand(reservationAddedByUser.getReservationToken());
        EmailConfirmedByUser emailConfirmedByUser = workshopAggregate.createEvent(confirmEmailCommand);
        assertThat(emailConfirmedByUser.getEmail()).isEqualToIgnoringCase("bal@gmail.com");
    }

    @Test(expected = NoReservationFoundException.class)
    public void shouldNotConfirmWhenReservationDoesNotExist() throws Exception {
        ConfirmEmailCommand confirmEmailCommand = new ConfirmEmailCommand("DribbleDrobbleTokenting");
        workshopAggregate.createEvent(confirmEmailCommand);

    }

    @After
    public void tearDown() throws Exception {
        Configuration.initData(null);

    }
}
