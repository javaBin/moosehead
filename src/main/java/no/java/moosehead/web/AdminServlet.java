package no.java.moosehead.web;

import com.fasterxml.jackson.databind.JsonNode;
import no.java.moosehead.api.AdminApi;
import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.commands.AuthorEnum;
import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.repository.WorkshopData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.java.moosehead.web.Utils.*;

@WebServlet(urlPatterns = {"/admin/data/*"})
public class AdminServlet  extends HttpServlet {
    private ParticipantApi participantApi;
    private AdminApi adminApi;

    @Override
    public void init() throws ServletException {
        participantApi = SystemSetup.instance().workshopController();
        adminApi = SystemSetup.instance().workshopController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonNode userAccess = (JsonNode) req.getSession().getAttribute("user");
        if ("/userLogin".equals(req.getPathInfo())) {
            resp.setContentType("text/json");

            PrintWriter writer = resp.getWriter();
            if (!Configuration.secureAdmin()) {
                writer.append("{\"id\":54435,\"admin\":true}");
                return;
            }
            writer.append(Optional.ofNullable(userAccess).map(Object::toString).orElse("{}"));
            return;
        }
        if (Configuration.secureAdmin() && (userAccess == null || !Optional.ofNullable(userAccess.get("admin")).map(JsonNode::asBoolean).orElse(false))) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if ("/workshopList".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printWorkshops(resp);
        } else if ("/workshop".equals(req.getPathInfo())) {
            printWorkshopDetails(req, resp);
        } else if ("/alldata".equals(req.getPathInfo())) {
            printAllInfo(resp);
        } else if ("/duplreservations".equals(req.getPathInfo())) {
            printDuplicate(resp);
        } else  {
            resp.getWriter().print("" +
                    "<html>Protected Admin API:<ul>" +
                    "   <li>/workshopList</li>" +
                    "   <li>/workshop?workshopid=[workshopid]</li>" +
                    "</html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonNode userAccess = (JsonNode) req.getSession().getAttribute("user");
        if (Configuration.secureAdmin() && (userAccess == null || !Optional.ofNullable(userAccess.get("admin")).map(JsonNode::asBoolean).orElse(false))) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JSONObject jsonInput = readJson(req.getInputStream());
        if (jsonInput == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return;
        }

        Optional<ParticipantActionResult> apiResult;
        String pathInfo = req.getPathInfo();
        if ("/cancel".equals(pathInfo)) {
            apiResult = doCancelation(jsonInput, resp);
        } else if ("/reserve".equals(pathInfo)) {
            apiResult = doReservation(jsonInput, req, resp);
        } else if ("/addWorkshop".equals(pathInfo)) {
            apiResult = addWorkshop(jsonInput);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal path");
            return;
        }
        if (!apiResult.isPresent()) {
            return;
        }
        resp.setContentType("text/json");
        JSONObject result = new JSONObject();
        try {
            result.put("status", apiResult.get().getStatus());
            String errormessage = apiResult.get().getErrormessage();
            if (errormessage != null) {
                result.put("message",errormessage);
            }
            result.write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<ParticipantActionResult> addWorkshop(JSONObject jsonInput) {
        Optional<String> errormessage = validateRequiredAddWorkshopFields(jsonInput);
        if (errormessage.isPresent()) {
            return Optional.of(ParticipantActionResult.error(errormessage.get()));
        }
        WorkshopData workshopData = new WorkshopData(
                readField(jsonInput, "slug"),
                readField(jsonInput, "title"),
                readField(jsonInput, "description"),
                readInstantField(jsonInput, "startTime"),
                readInstantField(jsonInput, "endTime"),
                Optional.of(readInstantField(jsonInput, "openTime")),
                readWorkshopTypeEnum(jsonInput, "workshopType")
        );
        ParticipantActionResult result = adminApi.createWorkshop(
                workshopData,
                readInstantField(jsonInput, "startTime"),
                readInstantField(jsonInput, "endTime"),
                readInstantField(jsonInput, "openTime"),
                Integer.parseInt(readField(jsonInput, "maxParticipants"))
        );

        return Optional.of(result);
    }

    private Optional<String> validateRequiredAddWorkshopFields(JSONObject jsonInput) {
        List<String> requiredItems = Arrays.asList("slug",
                "title",
                "description",
                "startTime",
                "endTime",
                "openTime",
                "maxParticipants",
                "workshopType");
        for (String required : requiredItems) {
            String value = readField(jsonInput,required);
            if (value == null || value.trim().isEmpty()) {
                return Optional.of(String.format("Field %s is required", required));
            }
        }
        List<String> dateFields = Arrays.asList(
                "startTime",
                "endTime",
                "openTime");
        for (String dateField : dateFields) {
            if (!Utils.toInstant(readField(jsonInput,dateField)).isPresent()) {
                return Optional.of(String.format("Field %s must have date format dd/MM-yyyy HH:mm", dateField));
            }

        }
        if (!readInstantField(jsonInput,"openTime").isBefore(readInstantField(jsonInput,"startTime"))) {
            return Optional.of("Open time must be before start time");
        }
        if (!readInstantField(jsonInput,"startTime").isBefore(readInstantField(jsonInput,"endTime"))) {
            return Optional.of("Start time must be before end time");
        }
        try {
            Integer.parseInt(readField(jsonInput,"maxParticipants"));
        } catch (NumberFormatException e) {
            return Optional.of("Max participants must be numeric");
        }
        try {
            WorkshopTypeEnum.valueOf(readField(jsonInput,"workshopType"));
        } catch (IllegalArgumentException e) {
            return Optional.of("Illegal value for workshop type");
        }
        return Optional.empty();
    }

    private static Instant readInstantField(JSONObject jsonInput, String name) {
        return Utils.toInstant(readField(jsonInput,name)).get();
    }

    private Optional<ParticipantActionResult> doReservation(JSONObject jsonInput, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput,"email");
        String fullname = readField(jsonInput, "fullname");

        if (workshopid == null || email == null || fullname == null) {
            return Optional.of(ParticipantActionResult.error("Name and email must be present without spesial characters"));
        }
        ParticipantActionResult reservation = participantApi.reservation(workshopid, email, fullname, AuthorEnum.ADMIN, Optional.empty(), 1);

        return Optional.of(reservation);
    }

    private Optional<ParticipantActionResult> doCancelation(JSONObject jsonInput,HttpServletResponse resp) throws IOException {
        String token = readField(jsonInput, "token");

        if (token == null ) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult cancel = participantApi.cancellation(token, AuthorEnum.ADMIN);

        return Optional.of(cancel);
    }

    private void printDuplicate(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/json");
        List<JSONObject> report = new ArrayList<>();
        List<WorkshopInfo> workshops = participantApi.workshops();
        for (int i=0;i<workshops.size()-1;i++) {
            WorkshopInfo a = workshops.get(i);
            for (int j=i+1;j<workshops.size();j++) {
                WorkshopInfo b = workshops.get(j);
                List<String> duplicates = a.getParticipants().stream()
                        .filter(pa -> (pa.isEmailConfirmed() && !pa.isWaiting()))
                        .filter(ap -> {
                            return b.getParticipants().stream()
                                    .filter(pa -> (pa.isEmailConfirmed() && !pa.isWaiting()))
                                    .filter(bp -> ap.getEmail().equals(bp.getEmail()))
                                    .findAny().isPresent();
                        })
                        .map(pa -> pa.getEmail())
                        .collect(Collectors.toList());
                JSONObject duplReport = new JSONObject();
                try {
                    duplReport.put("wsa",a.getId());
                    duplReport.put("wsb",b.getId());
                    duplReport.put("duplicates",new JSONArray(duplicates));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                report.add(duplReport);

            }
        }
        try {
            new JSONArray(report).write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void printAllInfo(HttpServletResponse resp) throws IOException {
        List<WorkshopInfo> workshops = participantApi.workshops();
        List<JSONObject> allInfo = workshops.stream()
                .sequential()
                .map(ParticipantApi::asAdminJson)
                .collect(Collectors.toList());
        JSONArray jsonArray = new JSONArray(allInfo);
        resp.setContentType("text/json");
        resp.getWriter().append(jsonArray.toString());
    }

    private void printWorkshops(HttpServletResponse resp) throws IOException {
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

    private void printWorkshopDetails(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshopId = req.getParameter("workshopid");
        WorkshopInfo workshop = participantApi.getWorkshop(workshopId);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", workshop.getId());
            jsonObject.put("title", workshop.getTitle());
            jsonObject.put("description", workshop.getDescription());
            jsonObject.put("status", workshop.getStatus().name());
            jsonObject.put("participants", new JSONArray(workshop.getParticipants().stream().
                        map(pa -> {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("name", pa.getName());
                                json.put("email", pa.getEmail());
                                json.put("numberOfSeats", pa.getNumberOfSeatsReserved());
                                json.put("isEmailConfirmed",pa.isEmailConfirmed());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return json;
                        }).collect(Collectors.toList())));


            PrintWriter writer = resp.getWriter();

            jsonObject.write(writer);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
