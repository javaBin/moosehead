package no.java.moosehead.eventstore;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.eventstore.utils.ClassSerializer;
import no.java.moosehead.repository.WorkshopData;
import org.junit.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class ClassSerializerTest {

    @Test
    public void shouldHandleOptionals() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(1L, 1L, "a@a.com", "Darth Vader", "xx", Optional.of("a@a.com"),1);
        String serialized = classSerializer.asString(reservationAddedByUser);
        ReservationAddedByUser copy = (ReservationAddedByUser) classSerializer.asObject(serialized);
        assertThat(copy.getGoogleUserEmail().get()).isEqualTo("a@a.com");
    }

    @Test
    public void shouldHandleEmptyOptionals() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(1L, 1L, "a@a.com", "Darth Vader", "xx", Optional.empty(),1);
        String serialized = classSerializer.asString(reservationAddedByUser);
        ReservationAddedByUser copy = (ReservationAddedByUser) classSerializer.asObject(serialized);
        assertThat(copy.getGoogleUserEmail().isPresent()).isFalse();
    }

    @Test
    public void shouldHandleEnums() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        WorkshopData workshopData = new WorkshopData("id", "title", "description", null, null, Optional.empty(), WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
        WorkshopAddedByAdmin workshopAddedByAdmin = new WorkshopAddedByAdmin(1L, 1L, "id", 30, null, null, workshopData);
        String asString = classSerializer.asString(workshopAddedByAdmin);
        System.out.println(asString);
        WorkshopAddedByAdmin copy = (WorkshopAddedByAdmin) classSerializer.asObject(asString);
        assertThat(copy.getWorkshopData().get().getWorkshopTypeEnum()).isEqualTo(WorkshopTypeEnum.KIDSAKODER_WORKSHOP);


    }
}
