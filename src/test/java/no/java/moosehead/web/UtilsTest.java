package no.java.moosehead.web;


import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class UtilsTest {
    @Test
    public void shouldConvertFromStringToInstant() throws Exception {
        Optional<Instant> instant = Utils.toInstant("28/07-2015 15:45");
        assertThat(instant).isPresent();
        assertThat(instant.get().toString()).isEqualTo("2015-07-28T13:45:00Z");
    }

    @Test
    public void shouldHandleInvalidDate() throws Exception {
        assertThat(Utils.toInstant("sdf").isPresent()).isFalse();

    }
}