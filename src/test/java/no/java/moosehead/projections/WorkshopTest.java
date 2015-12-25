package no.java.moosehead.projections;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.eventstore.AbstractReservationAdded;
import no.java.moosehead.eventstore.ReservationAddedByAdmin;
import no.java.moosehead.repository.WorkshopData;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class WorkshopTest {

    @Test
    public void testWaitingListNumber() throws Exception {
        WorkshopData wsd = new WorkshopData("id","tittel","beskrivelse");
        Workshop ws = new Workshop(wsd, 10);

        final Participant onList_0 = Participant.confirmedParticipant(new ReservationAddedByAdmin(AbstractReservationAdded.builder()
                        .setSystemTimeInMillis(0L)
                        .setRevisionId(0L)
                        .setEmail("email0@em.ail")
                        .setFullname("0")
                        .setWorkshopId(ws.getWorkshopData().getId())
                        .setNumberOfSeatsReserved(5)
                ), ws);
        ws.addParticipant(onList_0);
        assertThat(onList_0.waitingListNumber()).isEqualTo(0);

        final Participant onList_1 = Participant.confirmedParticipant(new ReservationAddedByAdmin(AbstractReservationAdded.builder()
                        .setSystemTimeInMillis(1L)
                        .setRevisionId(1L)
                        .setEmail("email1@em.ail")
                        .setFullname("1")
                        .setWorkshopId(ws.getWorkshopData().getId())
                        .setNumberOfSeatsReserved(4)
                ), ws);
        ws.addParticipant(onList_1);
        assertThat(onList_1.waitingListNumber()).isEqualTo(0);

        final Participant onWaitingList_1 = Participant.confirmedParticipant(new ReservationAddedByAdmin(AbstractReservationAdded.builder()
                        .setSystemTimeInMillis(2L)
                        .setRevisionId(2L)
                        .setEmail("email2@em.ail")
                        .setFullname("2")
                        .setWorkshopId(ws.getWorkshopData().getId())
                        .setNumberOfSeatsReserved(2)
                ), ws);
        ws.addParticipant(onWaitingList_1);
        assertThat(onWaitingList_1.waitingListNumber()).isEqualTo(1);

        final Participant onWaitingList_2 = Participant.confirmedParticipant(new ReservationAddedByAdmin(AbstractReservationAdded.builder()
                        .setSystemTimeInMillis(3L)
                        .setRevisionId(3L)
                        .setEmail("email3@em.ail")
                        .setFullname("3")
                        .setWorkshopId(ws.getWorkshopData().getId())
                        .setNumberOfSeatsReserved(1)
                ), ws);
        ws.addParticipant(onWaitingList_2);
        assertThat(onWaitingList_2.waitingListNumber()).isEqualTo(2);
    }

    @Test
    public void testNoWaitingListNumber() throws Exception {
        WorkshopData wsd = new WorkshopData("id","tittel","beskrivelse");
        Workshop ws = new Workshop(wsd, 10);

        final Participant onList_0 = Participant.confirmedParticipant(new ReservationAddedByAdmin(AbstractReservationAdded.builder()
                        .setSystemTimeInMillis(0L)
                        .setRevisionId(0L)
                        .setEmail("email0@em.ail")
                        .setFullname("0")
                        .setWorkshopId(ws.getWorkshopData().getId())
                        .setNumberOfSeatsReserved(5)
                ), ws);
        ws.addParticipant(onList_0);
        assertThat(onList_0.waitingListNumber()).isEqualTo(0);

        final Participant onList_1 = Participant.confirmedParticipant(new ReservationAddedByAdmin(AbstractReservationAdded.builder()
                        .setSystemTimeInMillis(1L)
                        .setRevisionId(1L)
                        .setEmail("email1@em.ail")
                        .setFullname("1")
                        .setWorkshopId(ws.getWorkshopData().getId())
                        .setNumberOfSeatsReserved(5)
                ), ws);
        ws.addParticipant(onList_1);
        assertThat(onList_1.waitingListNumber()).isEqualTo(0);
    }

    @Test
    public void testWorkshopInfoText() throws Exception {
        Instant start = LocalDateTime.of(2018, 4, 20, 14, 0, 0).atOffset(ZoneOffset.ofHours(2)).toInstant();
        Instant end = LocalDateTime.of(2018, 4, 20, 15, 0, 0).atOffset(ZoneOffset.ofHours(2)).toInstant();
        WorkshopData workshopData = new WorkshopData("xx", "Juggling workshop", "Learn to juggle", start, end, Optional.empty(), WorkshopTypeEnum.NORMAL_WORKSHOP);
        assertThat(workshopData.infoText()).isEqualTo("Juggling workshop (Start time: 20/04-2018 14:00)");


    }
}