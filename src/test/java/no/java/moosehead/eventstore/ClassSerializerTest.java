package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.utils.ClassSerializer;
import org.junit.*;
import static org.fest.assertions.Assertions.assertThat;

public class ClassSerializerTest {

    @Test
    public void testSerializing() {
        ClassSerializer classSerializer = new ClassSerializer();
        final WorkshopAddedByAdmin addedByAdmin = new WorkshopAddedByAdmin(99999, 1L, "W1", 0);
        assertThat(classSerializer.asString(addedByAdmin)).isEqualTo("<no.java.moosehead.eventstore.WorkshopAddedByAdmin;workshopId=W1;numberOfSeats=0;systemTimeInMillis=99999;revisionId=1>");
    }

    @Test
    public void testDeserializing() {
        ClassSerializer classSerializer = new ClassSerializer();
        WorkshopAddedByAdmin event = (WorkshopAddedByAdmin) classSerializer.asObject("<no.java.moosehead.eventstore.WorkshopAddedByAdmin;workshopId=W1;numberOfSeats=0;systemTimeInMillis=99999;revisionId=1>");
        assertThat(event.getWorkshopId()).isEqualTo("W1");
    }

}
