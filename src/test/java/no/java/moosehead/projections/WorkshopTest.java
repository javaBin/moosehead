package no.java.moosehead.projections;

import no.java.moosehead.eventstore.ReservationAddedByAdmin;
import no.java.moosehead.repository.WorkshopData;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class WorkshopTest {

    @Test
    public void testWaitingListNumber() throws Exception {
        WorkshopData wsd = new WorkshopData("id","tittel","beskrivelse");
        Workshop ws = new Workshop(wsd, 10, 1L);

        final Participant onList_0 = Participant.confirmedParticipant(new ReservationAddedByAdmin(0L, 0L, "email0@em.ail", "0", ws.getWorkshopData().getId(), 5), ws);
        ws.addParticipant(onList_0);
        assertThat(onList_0.waitingListNumber()).isEqualTo(0);

        final Participant onList_1 = Participant.confirmedParticipant(new ReservationAddedByAdmin(1L, 1L, "email1@em.ail", "1", ws.getWorkshopData().getId(), 4), ws);
        ws.addParticipant(onList_1);
        assertThat(onList_1.waitingListNumber()).isEqualTo(0);

        final Participant onWaitingList_1 = Participant.confirmedParticipant(new ReservationAddedByAdmin(2L, 2L, "email2@em.ail", "2", ws.getWorkshopData().getId(), 2), ws);
        ws.addParticipant(onWaitingList_1);
        assertThat(onWaitingList_1.waitingListNumber()).isEqualTo(1);

        final Participant onWaitingList_2 = Participant.confirmedParticipant(new ReservationAddedByAdmin(3L, 3L, "email3@em.ail", "3", ws.getWorkshopData().getId(), 1), ws);
        ws.addParticipant(onWaitingList_2);
        assertThat(onWaitingList_2.waitingListNumber()).isEqualTo(2);
    }

    @Test
    public void testNoWaitingListNumber() throws Exception {
        WorkshopData wsd = new WorkshopData("id","tittel","beskrivelse");
        Workshop ws = new Workshop(wsd, 10, 1L);

        final Participant onList_0 = Participant.confirmedParticipant(new ReservationAddedByAdmin(0L, 0L, "email0@em.ail", "0", ws.getWorkshopData().getId(), 5), ws);
        ws.addParticipant(onList_0);
        assertThat(onList_0.waitingListNumber()).isEqualTo(0);

        final Participant onList_1 = Participant.confirmedParticipant(new ReservationAddedByAdmin(1L, 1L, "email1@em.ail", "1", ws.getWorkshopData().getId(), 5), ws);
        ws.addParticipant(onList_1);
        assertThat(onList_1.waitingListNumber()).isEqualTo(0);
    }
}