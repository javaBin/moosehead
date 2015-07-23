package no.java.moosehead.eventstore;

import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.Author;
import no.java.moosehead.eventstore.utils.ClassSerializer;
import org.junit.*;

import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;

public class ClassSerializerTest {

    @Test
    public void testSerializing() {
        ClassSerializer classSerializer = new ClassSerializer();
        final WorkshopAddedByAdmin addedByAdmin = new WorkshopAddedByAdmin(99999, 1L, "W1", 0);
        assertThat(classSerializer.asString(addedByAdmin)).isEqualTo("<no.java.moosehead.eventstore.WorkshopAddedByAdmin;workshopId=W1;numberOfSeats=0;startTime=<null>;endTime=<null>;systemTimeInMillis=99999;revisionId=1>");
    }

    @Test
    public void testDeserializing() {
        ClassSerializer classSerializer = new ClassSerializer();
        WorkshopAddedByAdmin event = (WorkshopAddedByAdmin) classSerializer.asObject("<no.java.moosehead.eventstore.WorkshopAddedByAdmin;workshopId=W1;numberOfSeats=0;systemTimeInMillis=99999;revisionId=1>");
        assertThat(event.getWorkshopId()).isEqualTo("W1");
    }

    @Test
    public void shouldHandleOptionals() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(1L, 1L, "a@a.com", "Darth Vader", "xx", Optional.of("a@a.com"));
        String serialized = classSerializer.asString(reservationAddedByUser);
        ReservationAddedByUser copy = (ReservationAddedByUser) classSerializer.asObject(serialized);
        assertThat(copy.getGoogleUserEmail().get()).isEqualTo("a@a.com");
    }

    @Test
    public void shouldHandleEmptyOptionals() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(1L, 1L, "a@a.com", "Darth Vader", "xx", Optional.empty());
        String serialized = classSerializer.asString(reservationAddedByUser);
        ReservationAddedByUser copy = (ReservationAddedByUser) classSerializer.asObject(serialized);
        assertThat(copy.getGoogleUserEmail().isPresent()).isFalse();
    }

}
