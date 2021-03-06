package no.java.moosehead.aggregate;

import no.java.moosehead.commands.*;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.FilehandlerEventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.saga.EmailSender;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class WorkshopAggregateTest {

    private final String w2 = "W2";
    private final String w1 = "W1";

    private FilehandlerEventstore eventstore;
    private WorkshopAggregate workshopAggregate;

    @Before
    public void beforeTest() {
        SystemSetup systemSetup = mock(SystemSetup.class);
        TokenGenerator tokenGenerator = new TokenGenerator();
        Mockito.when(systemSetup.revisionGenerator()).thenReturn(tokenGenerator);
        SystemSetup.setSetup(systemSetup);
        eventstore = new FilehandlerEventstore(new FileHandler());
        workshopAggregate = new WorkshopAggregate();
        eventstore.addEventSubscriber(workshopAggregate);

        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime opens = now.atOffset(ZoneOffset.ofHours(2)).minusDays(2);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        String datestr = opens.format(format);

        Map<String,String> confdata = new HashMap<>();
        confdata.put("openTime", datestr);
        Configuration.initData(confdata);
    }

    @Test
    public void workshopAddedByAdminShouldBeOfTypeWorkshopAddedByAdmin() {
        WorkshopData workshopData = new WorkshopData(w1,"Wstitle","A little description");
        AddWorkshopCommand command = AddWorkshopCommand.builder()
                .withWorkshopId(w1)
                .withAuthor(AuthorEnum.ADMIN)
                .withNumberOfSeats(10)
                .withWorkshopData(Optional.of(workshopData))
                .create();
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(WorkshopAddedByAdmin.class);
        assertThat(event.getWorkshopData().get()).isEqualTo(workshopData);
    }

    @Test
    public void sholdDemandWorkshopInfoOnWorkshopAddedByAdmin() throws Exception {
        AddWorkshopCommand command = AddWorkshopCommand.builder()
                .withWorkshopId(w1)
                .withAuthor(AuthorEnum.ADMIN)
                .withNumberOfSeats(10)
                .create();
        try {
            workshopAggregate.createEvent(command);
            fail("Expected WorkshopCanNotBeAddedException");
        } catch (WorkshopCanNotBeAddedException e) {
            assertThat(e.getMessage()).isEqualTo("Need WorkshopData for workshop added by admin");
        }

    }

    @Test()
    public void workshopAddedBySystemShouldBeOfTypeWorkshopAddedBySystem() {
        AddWorkshopCommand command = AddWorkshopCommand.builder().withWorkshopId(w1).withAuthor(AuthorEnum.SYSTEM).withNumberOfSeats(10).create();
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(WorkshopAddedBySystem.class);
    }

    @Test(expected = WorkshopCanNotBeAddedException.class)
    public void workshopShouldNotBeAddedWhenItExistsAlready() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, w1, 0));
        AddWorkshopCommand command = AddWorkshopCommand.builder().withWorkshopId(w1).withAuthor(AuthorEnum.ADMIN).withNumberOfSeats(10).create();
        workshopAggregate.createEvent(command);
    }

    @Test
    public void workshopTypeIsSetToBeerShouldCreatBeerEvent() {
        AddWorkshopCommand command = AddWorkshopCommand.builder()
                .withWorkshopId(w1)
                .withAuthor(AuthorEnum.ADMIN)
                .withNumberOfSeats(10)
                .withWorkshopType(WorkshopTypeEnum.BEER_WORKSHOP)
                .withWorkshopData(Optional.of(new WorkshopData(w1, "Wstitle", "A little description")))
                .create();
        assertThat(command.getWorkshopTypeEnum()).isEqualTo(WorkshopTypeEnum.BEER_WORKSHOP);
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(BeerWorkshopAddedByAdmin.class);
    }

    @Test
    public void workshopTypeIsSetToKidsaKoderShouldCreateKidsaKoderEvent() {
        AddWorkshopCommand command = AddWorkshopCommand.builder()
                .withWorkshopId(w1)
                .withAuthor(AuthorEnum.ADMIN)
                .withNumberOfSeats(10)
                .withWorkshopType(WorkshopTypeEnum.KIDSAKODER_WORKSHOP)
                .withWorkshopData(Optional.of(new WorkshopData(w1, "Wstitle", "A little description")))
                .create();
        assertThat(command.getWorkshopTypeEnum()).isEqualTo(WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(KidsaKoderWorkshopAddedByAdmin.class);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void tooManySeatsReservedForKidsaKoder() {
        WorkshopData wd = new WorkshopData("sdf","sdfsd","dsgsd",null,null,Optional.empty(),WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, w1, 10,null,null,wd));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(5)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        workshopAggregate.createEvent(cmd);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void tooManySeatsReservedForNormalWorkshop() {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 10));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(2)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        workshopAggregate.createEvent(cmd);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void tooFewSeatsReservedForNormalWorkshop() {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 10));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(0)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        workshopAggregate.createEvent(cmd);
    }

    @Test
    public void defaultWorkshopTypeShouldBeNormalWorkshop() {
        AddWorkshopCommand command = AddWorkshopCommand.builder()
                .withWorkshopId(w1)
                .withAuthor(AuthorEnum.ADMIN)
                .withNumberOfSeats(10)
                .withWorkshopData(Optional.of(new WorkshopData(w1, "Wstitle", "A little description")))
                .create();
        assertThat(command.getWorkshopTypeEnum()).isEqualTo(WorkshopTypeEnum.NORMAL_WORKSHOP);
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event).isInstanceOf(WorkshopAddedByAdmin.class);
    }

    @Test
    public void aUniqueWorkshopShouldBeAdded() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddWorkshopCommand command = AddWorkshopCommand.builder()
                .withWorkshopId(w2)
                .withAuthor(AuthorEnum.SYSTEM)
                .withNumberOfSeats(10)
                .create();
        WorkshopAddedEvent event = workshopAggregate.createEvent(command);
        assertThat(event.getWorkshopId()).isEqualTo(w2);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void ReservationIsNotOkWhenWorkshopDoesNotExists() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w2)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        workshopAggregate.createEvent(cmd);
    }

    @Test
    public void ReservationIsOkWhenWorkshopExists() {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);
    }

    @Test
    public void shouldBeAbleToReserveMulitpleSpots() throws Exception {
        WorkshopData data = new WorkshopData(w1,"title","desc",null,null,Optional.empty(),WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0,null,null,data));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(2)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);

    }

    @Test
    public void multipleReservationsAreOk() throws Exception {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);
        eventstore.addEvent(reservationAddedByUser);
        workshopReservation = WorkshopReservation.builder()
                .setEmail("haha@email")
                .setFullname("Darth Vader")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd2 = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser2 =  workshopAggregate.createEvent(cmd2);
        assertThat(reservationAddedByUser2.getWorkshopId()).isEqualTo(w1);
    }

    @Test
    public void reReservationsAfterCancellingIsOk() throws Exception {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);

        eventstore.addEvent(reservationAddedByUser);

        CancelReservationCommand cancel = new CancelReservationCommand("bla@email",w1, AuthorEnum.USER);
        AbstractReservationCancelled rcbu = workshopAggregate.createEvent(cancel);

        assertThat(rcbu).isNotNull();

        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);
        eventstore.addEvent(rcbu);
        workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd2 = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser2 =  workshopAggregate.createEvent(cmd2);
        assertThat(reservationAddedByUser2.getWorkshopId()).isEqualTo(w1);
    }


    @Test
    public void sameEmailIsNotAllowedToReserveTwice() throws Exception {
        EmailSender emailSender = mock(EmailSender.class);
        workshopAggregate.setEmailSender(emailSender);
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(), 1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded event = workshopAggregate.createEvent(cmd);
        eventstore.addEvent(event);
        workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd2 = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        try {
            workshopAggregate.createEvent(cmd2);
            fail("Expected exception");
        } catch (ReservationCanNotBeAddedException e) {
            assertThat(e.getMessage().contains("bla@email"));
        }

        verify(emailSender).sendEmailConfirmation("bla@email",event.getReservationToken(),w1);
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

        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        workshopAggregate.createEvent(cmd);

    }



    @Test
    public void shouldBeAbleToCancelReservation() throws Exception {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 0));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(1)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        eventstore.addEvent(reservationAddedByUser);

        CancelReservationCommand cancel = new CancelReservationCommand("bla@email",w1, AuthorEnum.USER);
        AbstractReservationCancelled rcbu = workshopAggregate.createEvent(cancel);

        assertThat(rcbu).isNotNull();
    }

   

    @Test(expected = NoReservationFoundException.class)
    public void shouldNotBeAbleToCancelNonExsistingReservation() throws Exception {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(),1L, w1, 0));

        CancelReservationCommand cancel = new CancelReservationCommand("bla@email",w1, AuthorEnum.USER);
        workshopAggregate.createEvent(cancel);
    }

    @Test
    public void shouldBeAbleToPartlyCancel() throws Exception {
        WorkshopData data = new WorkshopData(w1,"title","desc",null,null,Optional.empty(),WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0,null,null,data));
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail("bla@email")
                .setFullname("Donnie Darko")
                .setWorkshopId(w1)
                .setGoogleUserEmail(Optional.empty())
                .setNumberOfSeatsReserved(2)
                .create();
        AddReservationCommand cmd = new AddReservationCommand(workshopReservation,AuthorEnum.USER);
        AbstractReservationAdded reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        eventstore.addEvent(reservationAddedByUser);

        ParitalCancellationCommand  cancellationCommand = new ParitalCancellationCommand("bla@email",w1,1);
        AbstractReservationCancelled event = workshopAggregate.createEvent(cancellationCommand);

        assertThat(event.getNumSpotsCancelled()).isEqualTo(1);


    }

    @Test
    public void shouldConfirmEmail() throws Exception {
        eventstore.addEvent(new WorkshopAddedBySystem(System.currentTimeMillis(), 1L, w1, 0));
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("bal@gmail.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId(w1)
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
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
