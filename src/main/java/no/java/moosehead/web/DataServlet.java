package no.java.moosehead.web;

import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.ParticipantReservation;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.commands.AuthorEnum;
import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.projections.Participant;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonBoolean;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParseException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
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
        resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        resp.addHeader("Pragma", "no-cache"); // HTTP 1.0.
        resp.addDateHeader("Expires", 0); // Proxies.
        if ("/workshopList".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            resp.addHeader("Access-Control-Allow-Origin", "*");
            printWorkshops(resp);
        } else if ("/myReservations".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printMyReservations(req,resp);
        } else if ("/teacherList".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printTeacherList(req, resp);
        } else if ("/userLogin".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            JsonObject node = (JsonObject) req.getSession().getAttribute("user");
            resp.getWriter().append(Optional.ofNullable(node).map(Object::toString).orElse("{}"));
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    private void printTeacherList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject userAccess = (JsonObject) req.getSession().getAttribute("user");
        if (Configuration.secureAdmin() && (userAccess == null || !userAccess.value("admin").map(jn -> ((JsonBoolean) jn).booleanValue()).orElse(false))) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

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
        List<JsonObject> participants = workshopInfo.getParticipants().stream()
                .filter(Participant::isEmailConfirmed)
                .map(ParticipantApi::participantAsJson)
                .collect(Collectors.toList());

        JsonObject result = JsonFactory.jsonObject();
        result.put("title", workshopInfo.getTitle());
        result.put("participants", JsonArray.fromNodeList(participants));
        result.toJson(resp.getWriter());

    }

    private void printMyReservations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        List<ParticipantReservation> participantReservations = participantApi.myReservations(email);
        List<JsonObject> reservations = participantReservations.stream().map(res -> {
            JsonObject jsonObject = JsonFactory.jsonObject();
            jsonObject.put("workshopid", res.getWorkshopid());
            jsonObject.put("email", res.getEmail());
            jsonObject.put("status", res.getStatus());
            jsonObject.put("workshopname", res.getWorkshopname());
            jsonObject.put("numberOfSeatsReserved", res.getNumberOfSeatsReserved());
            Optional<Integer> waitingListNumber = res.getWaitingListNumber();
            if (waitingListNumber.isPresent()) {
                jsonObject.put("waitingListNumber", waitingListNumber.get());
            }
            return jsonObject;
        }).collect(Collectors.toList());
        JsonArray.fromNodeList(reservations).toJson(resp.getWriter());
    }

    private void printWorkshops(HttpServletResponse resp) throws IOException {
        List<WorkshopInfo> workshops = participantApi.workshops();

        List<JsonObject> jsons = workshops.stream().map(workshop -> {

                    JsonObject jsonObject = JsonFactory.jsonObject();
                    jsonObject.put("id", workshop.getId());
                    jsonObject.put("title", workshop.getTitle());
                    jsonObject.put("description", workshop.getDescription());
                    jsonObject.put("status", workshop.getStatus().name());
                    int maxReservationSpaces = workshop.getWorkshopTypeEnum() == WorkshopTypeEnum.KIDSAKODER_WORKSHOP ? Configuration.maxNumberOfSeatsToReserve() : 1;
                    jsonObject.put("maxReservations", maxReservationSpaces);
                    jsonObject.put("start",workshop.workshopStartDate());
                    jsonObject.put("duration",workshop.workshopStartDate());
                    jsonObject.put("onWaitingList",workshop.getNumberOnWaitingList());

                    Optional<Instant> instant = workshop.registrationOpensAt();
                    instant.ifPresent(opens -> jsonObject.put("opensAt",Utils.formatInstant(opens)));

                    return jsonObject;
            }
        ).collect(Collectors.toList());
        PrintWriter writer = resp.getWriter();
        JsonArray.fromNodeList(jsons).toJson(writer);
    }

    private Optional<ParticipantActionResult> doReservation(JsonObject jsonInput, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput, "email");
        String fullname = readField(jsonInput, "fullname");
        String capthca = readField(jsonInput,"captcha");
        String numReservationStr = readField(jsonInput,"numReservations");
        Optional<JsonObject> additionalInfo = jsonInput.objectValue("additionalInfo");

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
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setWorkshopId(workshopid)
                .setEmail(email)
                .setFullname(fullname)
                .setGoogleUserEmail(googleEmail)
                .setNumberOfSeatsReserved(numReservations)
                .setAdditionalInfo(additionalInfo.orElse(null))
                .create();
        ParticipantActionResult reservation = participantApi.reservation(workshopReservation,AuthorEnum.USER);

        return Optional.of(reservation);
    }

    private Optional<String> readGoogleMail(HttpSession session) {
        return Optional.ofNullable(session.getAttribute("user"))
                .map(ob -> (JsonObject) ob)
                .map(on -> on.stringValue("email"))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<ParticipantActionResult> doCancelation(JsonObject jsonInput,HttpServletResponse resp) throws IOException {
        String token = Utils.sanitize(jsonInput.requiredString("token"));

        ParticipantActionResult cancel = participantApi.cancellation(token, AuthorEnum.USER);

        return Optional.of(cancel);
    }

    private Optional<ParticipantActionResult> doConfirmEmail(JsonObject jsonInput,HttpServletResponse resp) throws IOException {
        String token = readField(jsonInput, "token");

        if (token == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return Optional.empty();
        }
        ParticipantActionResult cancel = participantApi.confirmEmail(token);

        return Optional.of(cancel);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (JsonParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");
            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject jsonInput = readJson(req.getInputStream());
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
        JsonObject result = JsonFactory.jsonObject();
        result.put("status", apiResult.get().getStatus());
        String errormessage = apiResult.get().getErrormessage();
        if (errormessage != null) {
            result.put("message",errormessage);
        }
        result.toJson(resp.getWriter());
    }

    public void setParticipantApi(ParticipantApi participantApi) {
        this.participantApi = participantApi;
    }

}
