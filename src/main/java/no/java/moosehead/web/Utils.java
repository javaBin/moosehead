package no.java.moosehead.web;


import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletInputStream;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class Utils {

    public static String readField(JSONObject jsonInput, String name) {
        String value;
        try {
            value = jsonInput.getString(name);
        } catch (JSONException e) {
            return null;
        }
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c) || "-_ @./:".indexOf(c) != -1) {
                continue;
            }
            return null;
        }
        return value;
    }



    public static JSONObject readJson(ServletInputStream inputStream) throws IOException {
        try {
            return new JSONObject(toString(inputStream));
        } catch (JSONException e) {
            return null;
        }
    }

    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
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
