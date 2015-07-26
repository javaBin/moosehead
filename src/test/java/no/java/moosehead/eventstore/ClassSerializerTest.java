package no.java.moosehead.eventstore;

import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.Author;
import no.java.moosehead.eventstore.utils.ClassSerializer;
import org.junit.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class ClassSerializerTest {

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
