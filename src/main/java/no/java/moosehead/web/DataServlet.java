package no.java.moosehead.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.ParticipantReservation;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.commands.AuthorEnum;
import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.projections.Participant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.java.moosehead.web.Utils.readField;
import static no.java.moosehead.web.Utils.readJson;

@WebServlet(urlPatterns = {"/data/*"})
public class DataServlet extends HttpServlet {
    private ParticipantApi participantApi;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void init() throws ServletException {
        participantApi = SystemSetup.instance().workshopController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/workshopList".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printWorkshops(resp);
        } else if ("/myReservations".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printMyReservations(req,resp);
        } else if ("/teacherList".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printTeacherList(req, resp);
        } else if ("/userLogin".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            JsonNode node = (JsonNode) req.getSession().getAttribute("user");
            resp.getWriter().append(Optional.ofNullable(node).map(Object::toString).orElse("{}"));
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    private void printTeacherList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshop = req.getParameter("workshop");
        if (workshop == null || workshop.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Workshop not found");
            return;
        }

        Optional<WorkshopInfo> workshopInfoOptional = participantApi.workshops().stream()
                .filter(ws -> ws.getId().equals(workshop))
                .findAny();
        if (!workshopInfoOptional.isPresent()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Workshop not found");
            return;
        }
        WorkshopInfo workshopInfo = workshopInfoOptional.get();
        List<JSONObject> participants = workshopInfo.getParticipants().stream()
                .filter(Participant::isEmailConfirmed)
                .map(ParticipantApi::participantAsJson)
                .collect(Collectors.toList());

        try {
            JSONObject result = new JSONObject();
            result.put("title",workshopInfo.getTitle());
            result.put("participants",new JSONArray(participants));
            result.write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void printMyReservations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        List<ParticipantReservation> participantReservations = participantApi.myReservations(email);
        List<JSONObject> reservations = participantReservations.stream().map(res -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("workshopid", res.getWorkshopid());
                jsonObject.put("email", res.getEmail());
                jsonObject.put("status", res.getStatus());
                jsonObject.put("workshopname", res.getWorkshopname());
                jsonObject.put("numberOfSeatsReserved", res.getNumberOfSeatsReserved());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return jsonObject;
        }).collect(Collectors.toList());
        try {
            new JSONArray(reservations).write(resp.getWriter());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
                        int maxReservationSpaces = workshop.getWorkshopTypeEnum() == WorkshopTypeEnum.KIDSAKODER_WORKSHOP ? Configuration.maxNumberOfSeatsToReserve() : 1;
                        jsonObject.put("maxReservations", maxReservationSpaces);

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

    private Optional<ParticipantActionResult> doReservation(JSONObject jsonInput, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput, "email");
        String fullname = readField(jsonInput, "fullname");
        String capthca = readField(jsonInput,"captcha");
        String numReservationStr = readField(jsonInput,"numReservations");

        HttpSession session = req.getSession();
        Object captchaAnswer = session.getAttribute("captchaAnswer");
        if (capthca == null || !capthca.equals(captchaAnswer)) {
            return Optional.of(ParticipantActionResult.wrongCaptcha());
        }

        if (workshopid == null || email == null || fullname == null) {
            return Optional.of(ParticipantActionResult.error("Name and email must be present without spesial characters"));
        }
        if (numReservationStr == null) {
            return Optional.of(ParticipantActionResult.error("Invalid number of reservations"));
        }
        int numReservations;
        try {
            numReservations = Integer.parseInt(numReservationStr);
        } catch (NumberFormatException e) {
            return Optional.of(ParticipantActionResult.error("Invalid number of reservations"));
        }
        Optional<String> googleEmail = readGoogleMail(session);
        ParticipantActionResult reservation = participantApi.reservation(workshopid, email, fullname, AuthorEnum.USER, googleEmail, numReservations);

        return Optional.of(reservation);
    }

    private Optional<String> readGoogleMail(HttpSession session) {
        return Optional.ofNullable(session.getAttribute("user"))
                .map(ob -> (ObjectNode) ob)
                .map(on -> Optional.ofNullable(on.get("email")))
                .filter(Optional::isPresent)
                .map(ma -> ma.get().asText());
    }

    private Optional<ParticipantActionResult> doCancelation(JSONObject jsonInput,HttpServletResponse resp) throws IOException {
        String token = readField(jsonInput, "token");

        if (token == null ) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult cancel = participantApi.cancellation(token, AuthorEnum.USER);

        return Optional.of(cancel);
    }

    private Optional<ParticipantActionResult> doConfirmEmail(JSONObject jsonInput,HttpServletResponse resp) throws IOException {
        String token = readField(jsonInput, "token");

        if (token == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult cancel = participantApi.confirmEmail(token);

        return Optional.of(cancel);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonInput = readJson(req.getInputStream());
        if (jsonInput == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return;
        }
        Optional<ParticipantActionResult> apiResult;
        if ("/cancel".equals(req.getPathInfo())) {
            apiResult = doCancelation(jsonInput, resp);
        } else if ("/reserve".equals(req.getPathInfo())) {
            apiResult = doReservation(jsonInput, req, resp);
        } else if ("/confirmEmail".equals(req.getPathInfo())) {
            apiResult = doConfirmEmail(jsonInput, resp);
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

    public void setParticipantApi(ParticipantApi participantApi) {
        this.participantApi = participantApi;
    }

}
