package no.java.moosehead.web;

import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.WorkshopInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataServlet extends HttpServlet {
    private ParticipantApi participantApi;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/json");
        List<WorkshopInfo> workshops = participantApi.workshops();
        List<JSONObject> jsons = workshops.stream().map(workshop -> {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", workshop.getId());
                        jsonObject.put("title", workshop.getTitle());
                        jsonObject.put("description", workshop.getDescription());
                        jsonObject.put("status", workshop.getStatus().name());

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return jsonObject;
                }
        ).collect(Collectors.toList());
        PrintWriter writer = resp.getWriter();
        try {
            new JSONArray(jsons).write(writer);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonInput = readJson(req.getInputStream(),resp);
        if (jsonInput == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return;
        }
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput,"email");
        String fullname = readField(jsonInput,"fullname");

        if (workshopid == null || email == null || fullname == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return;
        }

        resp.setContentType("text/json");
        ParticipantActionResult reservation = participantApi.reservation(workshopid, email, fullname);
        JSONObject result = new JSONObject();
        try {
            result.put("status",reservation.getStatus());
            result.write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String readField(JSONObject jsonInput, String name) {
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

    private JSONObject readJson(ServletInputStream inputStream, HttpServletResponse resp) throws IOException {
        try {
            return new JSONObject(toString(inputStream));
        } catch (JSONException e) {
            return null;
        }
    }

    public void setParticipantApi(ParticipantApi participantApi) {
        this.participantApi = participantApi;
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
