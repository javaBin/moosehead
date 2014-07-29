package no.java.moosehead.web;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Configuration {
    public static void init(String filename) {

    }

    public static Integer serverPort() {
        return 8088;
    }

    public static String emsEventLocation() {
        return "http://test.2014.javazone.no/ems/server/events/9f40063a-5f20-4d7b-b1e8-ed0c6cc18a5f/sessions";
    }

    public static int placesPerWorkshop() {
        return 30;
    }

    public static OffsetDateTime openTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.atOffset(ZoneOffset.ofHours(2)).minusDays(2);
    }
}
