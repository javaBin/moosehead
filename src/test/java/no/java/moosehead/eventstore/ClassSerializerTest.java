package no.java.moosehead.eventstore;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.utils.ClassSerializer;
import no.java.moosehead.repository.WorkshopData;
import org.jsonbuddy.JsonFactory;
import org.junit.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class ClassSerializerTest {

    @Test
    public void shouldHandleOptionals() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(1L)
                        .setRevisionId(1L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("xx")
                        .setGoogleUserEmail(Optional.of("a@a.com"))
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        String serialized = classSerializer.asString(reservationAddedByUser);
        ReservationAddedByUser copy = (ReservationAddedByUser) classSerializer.asObject(serialized);
        assertThat(copy.getGoogleUserEmail().get()).isEqualTo("a@a.com");
    }

    @Test
    public void shouldHandleEmptyOptionals() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(1L)
                        .setRevisionId(1L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("xx")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
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
        WorkshopAddedByAdmin copy = (WorkshopAddedByAdmin) classSerializer.asObject(asString);
        assertThat(copy.getWorkshopData().get().getWorkshopTypeEnum()).isEqualTo(WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
    }

    @Test
    public void shouldHandleJsonContent() throws Exception {
        ClassSerializer classSerializer = new ClassSerializer();
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setWorkshopId("3")
                .setAdditionalInfo(JsonFactory.jsonObject().put("shirts", JsonFactory.jsonArray().add(JsonFactory.jsonObject().put("size", "small"))))
                .create();
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(workshopReservation);

        String asString = classSerializer.asString(reservationAddedByUser);

        ReservationAddedByUser copy = (ReservationAddedByUser) classSerializer.asObject(asString);

        assertThat(copy.getAdditionalInfo().requiredArray("shirts").objectStream().findAny().get().requiredString("size")).isEqualTo("small");

    }
}
