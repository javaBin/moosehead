package no.java.moosehead.web;


import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletInputStream;
import java.io.*;

public class Utils {

    public static String readField(JSONObject jsonInput, String name) {
        String value;
        try {
            value = jsonInput.getString(name);
        } catch (JSONException e) {
            return null;
        }
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c) || "-_ @.".indexOf(c) != -1) {
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
}
