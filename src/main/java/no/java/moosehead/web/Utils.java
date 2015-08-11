package no.java.moosehead.web;


import no.java.moosehead.commands.WorkshopTypeEnum;
import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParseException;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class Utils {

    public static WorkshopTypeEnum readWorkshopTypeEnum(JsonObject jsonInput, String name) {
        String value;
        value = readField(jsonInput,name);
        if (value == null) {
            return WorkshopTypeEnum.NORMAL_WORKSHOP;
        } else {
            return WorkshopTypeEnum.valueOf(value);
        }
    }

    public static String readField(JsonObject jsonInput, String name) {
        return sanitize(jsonInput.requiredString(name));
    }

    static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c) || " -_@./:".indexOf(c) != -1) {
                continue;
            }
            return null;
        }
        return value;
    }


    public static JsonObject readJson(ServletInputStream inputStream) throws IOException {
        try {
            return (JsonObject) JsonParser.parse(inputStream);
        } catch (ClassCastException e) {
            return null;
        }
    }


    public static Optional<Instant> toInstant(String datestring) {
        if (datestring == null) {
            return null;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM-yyyy HH:mm");
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(datestring, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
        Instant instant = dateTime.toInstant(ZoneOffset.ofHours(2));
        return Optional.of(instant);
    }
}
