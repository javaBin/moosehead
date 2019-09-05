package no.java.moosehead.web;

import no.java.moosehead.api.AdminApi;
import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.commands.AuthorEnum;
import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.saga.EmailSender;
import org.jsonbuddy.*;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static no.java.moosehead.web.Utils.*;

@WebServlet(urlPatterns = {"/admin/data/*"})
public class AdminServlet  extends HttpServlet {
    private ParticipantApi participantApi;
    private AdminApi adminApi;
    private EmailSender emailSender;

    @Override
    public void init() throws ServletException {
        participantApi = SystemSetup.instance().workshopController();
        adminApi = SystemSetup.instance().workshopController();
        emailSender = SystemSetup.instance().emailSender();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject userAccess = (JsonObject) req.getSession().getAttribute("user");
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
        if (Configuration.secureAdmin() && (userAccess == null || !userAccess.value("admin").map(jn -> ((JsonBoolean) jn).booleanValue()).orElse(false))) {
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
        } else if ("/status".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            JsonFactory.jsonObject().put("emailSender",SystemSetup.instance().emailSender().getClass().toString()).toJson(resp.getWriter());
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
        JsonObject userAccess = (JsonObject) req.getSession().getAttribute("user");
        if (Configuration.secureAdmin() && (userAccess == null || !userAccess.value("admin").map(jn -> ((JsonBoolean) jn).booleanValue()).orElse(false))) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JsonObject jsonInput = readJson(req.getInputStream());
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
        } else if ("/partialCancel".equals(pathInfo)) {
            apiResult = partialCancel(jsonInput);
        } else if ("/resendReservationConfirmEmail".equals(pathInfo)) {
            apiResult = resendAllReservationCondirmations(jsonInput);
        } else if ("/shownUp".equals(pathInfo)) {
            apiResult = registerShowUp(jsonInput);
        } else if ("/resendConfirmation".equals(pathInfo)) {
            apiResult = resendConfirmation(jsonInput);
        } else if ("/updateWorkshopSize".equals(pathInfo)) {
            apiResult = updateWorkshopSize(jsonInput);
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



    private Optional<ParticipantActionResult> updateWorkshopSize(JsonObject jsonInput) {
        Optional<String> workshopid = jsonInput.stringValue("workshopid");
        Optional<Integer> numSpots = jsonInput.stringValue("numspots").map(Integer::parseInt);
        if (!workshopid.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value workshopid"));
        }
        if (!numSpots.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value numspots"));
        }
        ParticipantActionResult participantActionResult = adminApi.changeWorkshopSize(workshopid.get(), numSpots.get());
        return Optional.of(participantActionResult);
    }

    private Optional<ParticipantActionResult> resendConfirmation(JsonObject jsonInput) {
        Optional<String> reservationToken = jsonInput.stringValue("reservationToken");
        if (!reservationToken.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value reservationToken"));
        }
        Optional<String> workshopid = jsonInput.stringValue("workshopid");
        if (!workshopid.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value workshopid"));
        }
        List<WorkshopInfo> workshops = participantApi.workshops();
        Optional<WorkshopInfo> optionalWorkshopInfo = workshops.stream().filter(ws -> ws.getId().equals(workshopid.get())).findAny();
        if (!optionalWorkshopInfo.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Unknown workshop id " + workshopid.get()));
        }
        List<Participant> participants = optionalWorkshopInfo.get().getParticipants();
        Optional<Participant> optionalParticipant = participants.stream()
                .filter(participant -> participant.getWorkshopReservation().getReservationToken().equals(reservationToken.get()))
                .findAny();
        if (!optionalParticipant.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Unknown reservation token " + reservationToken.get()));
        }
        Participant participant = optionalParticipant.get();
        WorkshopReservation reservation = participant.getWorkshopReservation();
        emailSender.sendEmailConfirmation(reservation.getEmail(),reservation.getReservationToken(),workshopid.get());
        return Optional.of(ParticipantActionResult.ok());
    }

    private Optional<ParticipantActionResult> resendAllReservationCondirmations(JsonObject jsonInput) {
        Optional<String> workshopidOpt = jsonInput.stringValue("workshopid");
        if (!workshopidOpt.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value workshopid"));
        }
        String workshopid = workshopidOpt.get();
        List<WorkshopInfo> workshops = participantApi.workshops();
        Optional<WorkshopInfo> optionalWorkshopInfo = workshops.stream().filter(ws -> ws.getId().equals(workshopid)).findAny();
        if (!optionalWorkshopInfo.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Unknown workshop id " + workshopid));
        }
        WorkshopInfo workshopInfo = optionalWorkshopInfo.get();
        List<Participant> participants = workshopInfo.getParticipants();
        int spacesLeft = workshopInfo.getNumberOfSeats();
        for (Participant participant : participants) {
            spacesLeft-=participant.getNumberOfSeatsReserved();
            if (spacesLeft < 0) {
                break;
            }
            WorkshopReservation reservation = participant.getWorkshopReservation();
            emailSender.sendReservationConfirmation(reservation.getEmail(),workshopid,reservation.getReservationToken());
        }
        return Optional.of(ParticipantActionResult.ok());
    }

    private Optional<ParticipantActionResult> registerShowUp(JsonObject jsonInput) {
        Optional<String> reservationToken = jsonInput.stringValue("reservationToken");
        if (!reservationToken.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value reservationToken"));
        }
        Optional<Boolean> shownUp = jsonInput.booleanValue("hasShownUp");
        if (!shownUp.isPresent()) {
            return Optional.of(ParticipantActionResult.error("Required value shownUp"));
        }
        ParticipantActionResult participantActionResult = adminApi.registerShowUp(reservationToken.get(), shownUp.get());
        return Optional.of(participantActionResult);

    }

    private Optional<ParticipantActionResult> partialCancel(JsonObject jsonInput) {
        String email;
        String workshopid;
        int numSpotCanceled;
        try {
            email = jsonInput.requiredString("email");
            workshopid = jsonInput.requiredString("workshopid");
            numSpotCanceled = (int) jsonInput.requiredLong("numSpotCanceled");
        } catch (JsonValueNotPresentException e) {
            return Optional.of(ParticipantActionResult.error("Need email, workshopid and num canceled"));
        }
        return Optional.of(adminApi.partialCancel(email,workshopid,numSpotCanceled));
    }

    private Optional<ParticipantActionResult> addWorkshop(JsonObject jsonInput) {
        Optional<String> errormessage = validateRequiredAddWorkshopFields(jsonInput);
        if (errormessage.isPresent()) {
            return Optional.of(ParticipantActionResult.error(errormessage.get()));
        }
        Optional<String> sleepingpillurl = jsonInput.stringValue("sleepingpillurl").filter(s -> !s.trim().isEmpty());
        if (sleepingpillurl.isPresent()) {
            return createFromSleepingpull(sleepingpillurl.get(),readInstantField(jsonInput, "openTime"),Integer.parseInt(readField(jsonInput, "maxParticipants")));
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

    private static class WorkshopToCreate {
        private String slug;
        private String title;
        private String description;
        private Instant startTime;
        private Instant endTime;

        private static WorkshopToCreate fromJson(JsonObject sessionobject) {
            WorkshopToCreate result = new WorkshopToCreate();
            result.title = sessionobject.requiredString("title");
            result.slug = createSlug(result.title);
            result.description = sessionobject.requiredString("abstract");
            result.startTime = instantFromSleepingpillDate(sessionobject.requiredString("startTime"));
            result.endTime = instantFromSleepingpillDate(sessionobject.requiredString("endTime"));
            return result;
        }
    }

    private static Instant instantFromSleepingpillDate(String datestr) {
        return LocalDateTime.parse(datestr).toInstant(ZoneOffset.ofHours(2));
    }

    private static String createSlug(String title) {
        StringBuilder result = new StringBuilder();
        for (Character c : title.toLowerCase().toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
            }
            if (Character.isWhitespace(c)) {
                result.append("_");
            }
        }
        if (result.toString().isEmpty()) {
            result.append("xxxx");
        }
        return result.toString();
    }

    private Optional<ParticipantActionResult> createFromSleepingpull(String sleepingPillUrl, Instant openTime, int maxParticipants) {
        JsonObject sleepingpillobj;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(sleepingPillUrl).openConnection();
            try (InputStream is = urlConnection.getInputStream()) {
                sleepingpillobj = JsonParser.parseToObject(is);
            }
        } catch (IOException e) {
            return Optional.of(ParticipantActionResult.error("Error reading from sleepingpill"));
        }
        List<WorkshopToCreate> collect = sleepingpillobj.requiredArray("sessions").objectStream()
                .filter(sessobj -> Optional.of("workshop").equals(sessobj.stringValue("format")))
                .map(WorkshopToCreate::fromJson)
                .collect(Collectors.toList());
        for (WorkshopToCreate workshopToCreate : collect) {
            WorkshopData workshopData = new WorkshopData(
                    workshopToCreate.slug,
                    workshopToCreate.title,
                    workshopToCreate.description,
                    workshopToCreate.startTime,
                    workshopToCreate.endTime,
                    Optional.of(openTime),
                    WorkshopTypeEnum.NORMAL_WORKSHOP
            );
            ParticipantActionResult result = adminApi.createWorkshop(
                    workshopData,
                    workshopToCreate.startTime,
                    workshopToCreate.endTime,
                    openTime,
                    maxParticipants
            );

        }
        return Optional.of(ParticipantActionResult.ok());
    }

    private Optional<String> validateRequiredAddWorkshopFields(JsonObject jsonInput) {
        Optional<String> sleepingpillurl = jsonInput.stringValue("sleepingpillurl").filter(s -> !s.trim().isEmpty());

        List<String> requiredItems;
        if (sleepingpillurl.isPresent()) {
            requiredItems = Arrays.asList(
                    "openTime",
                    "maxParticipants");
        } else {
            requiredItems = Arrays.asList("slug",
                    "title",
                    "description",
                    "startTime",
                    "endTime",
                    "openTime",
                    "maxParticipants",
                    "workshopType");
        }
        for (String required : requiredItems) {
            String value = readField(jsonInput,required);
            if (value == null || value.trim().isEmpty()) {
                return Optional.of(String.format("Field %s is required", required));
            }
        }
        List<String> dateFields;
        if (sleepingpillurl.isPresent()) {
            dateFields = Collections.singletonList("openTime");
        } else {
            dateFields = Arrays.asList(
                    "startTime",
                    "endTime",
                    "openTime");
        }
        for (String dateField : dateFields) {
            if (!Utils.toInstant(readField(jsonInput,dateField)).isPresent()) {
                return Optional.of(String.format("Field %s must have date format dd/MM-yyyy HH:mm", dateField));
            }
        }
        try {
            Integer.parseInt(readField(jsonInput,"maxParticipants"));
        } catch (NumberFormatException e) {
            return Optional.of("Max participants must be numeric");
        }
        if (sleepingpillurl.isPresent()) {
            return Optional.empty();
        }

        if (!readInstantField(jsonInput,"openTime").isBefore(readInstantField(jsonInput,"startTime"))) {
            return Optional.of("Open time must be before start time");
        }
        if (!readInstantField(jsonInput,"startTime").isBefore(readInstantField(jsonInput,"endTime"))) {
            return Optional.of("Start time must be before end time");
        }
        try {
            WorkshopTypeEnum.valueOf(readField(jsonInput,"workshopType"));
        } catch (IllegalArgumentException e) {
            return Optional.of("Illegal value for workshop type");
        }
        return Optional.empty();
    }

    private static Instant readInstantField(JsonObject jsonInput, String name) {
        return Utils.toInstant(readField(jsonInput,name)).get();
    }

    private Optional<ParticipantActionResult> doReservation(JsonObject jsonInput, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshopid = readField(jsonInput, "workshopid");
        String email = readField(jsonInput,"email");
        String fullname = readField(jsonInput, "fullname");

        if (workshopid == null || email == null || fullname == null) {
            return Optional.of(ParticipantActionResult.error("Name and email must be present without spesial characters"));
        }
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setWorkshopId(workshopid)
                .setEmail(email)
                .setFullname(fullname)
                .create();
        ParticipantActionResult reservation = participantApi.reservation(workshopReservation, AuthorEnum.ADMIN);

        return Optional.of(reservation);
    }

    private Optional<ParticipantActionResult> doCancelation(JsonObject jsonInput,HttpServletResponse resp) throws IOException {
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
        List<JsonObject> report = new ArrayList<>();
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
                                    .filter(bp -> ap.getWorkshopReservation().getEmail().equals(bp.getWorkshopReservation().getEmail()))
                                    .findAny().isPresent();
                        })
                        .map(pa -> pa.getWorkshopReservation().getEmail())
                        .collect(Collectors.toList());
                JsonObject duplReport = JsonFactory.jsonObject();
                duplReport.put("wsa", a.getId());
                duplReport.put("wsb", b.getId());
                duplReport.put("duplicates", JsonArray.fromStringList(duplicates));
                report.add(duplReport);

            }
        }
        JsonArray.fromNodeList(report).toJson(resp.getWriter());
    }


    private void printAllInfo(HttpServletResponse resp) throws IOException {
        List<WorkshopInfo> workshops = participantApi.workshops();
        List<JsonObject> allInfo = workshops.stream()
                .sequential()
                .map(ParticipantApi::asAdminJson)
                .collect(Collectors.toList());
        JsonArray jsonArray = JsonArray.fromNodeList(allInfo);
        resp.setContentType("text/json");
        resp.getWriter().append(jsonArray.toString());
    }

    private void printWorkshops(HttpServletResponse resp) throws IOException {
        List<WorkshopInfo> workshops = participantApi.workshops();
        List<JsonObject> jsons = workshops.stream().map(workshop -> {
                    JsonObject jsonObject = JsonFactory.jsonObject();
                    jsonObject.put("id", workshop.getId());
                    jsonObject.put("title", workshop.getTitle());
                    jsonObject.put("description", workshop.getDescription());
                    jsonObject.put("status", workshop.getStatus().name());
                    return jsonObject;
                }
        ).collect(Collectors.toList());
        PrintWriter writer = resp.getWriter();
        JsonArray.fromNodeList(jsons).toJson(writer);
    }

    private void printWorkshopDetails(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String workshopId = req.getParameter("workshopid");
        WorkshopInfo workshop = participantApi.getWorkshop(workshopId);

        JsonObject jsonObject = JsonFactory.jsonObject();
        jsonObject.put("id", workshop.getId());
        jsonObject.put("title", workshop.getTitle());
        jsonObject.put("description", workshop.getDescription());
        jsonObject.put("status", workshop.getStatus().name());
        jsonObject.put("participants", JsonArray.fromNodeList(workshop.getParticipants().stream().
                map(pa -> {
                    JsonObject json = JsonFactory.jsonObject();
                    json.put("name", pa.getWorkshopReservation().getFullname());
                    json.put("email", pa.getWorkshopReservation().getEmail());
                    json.put("numberOfSeats", pa.getNumberOfSeatsReserved());
                    json.put("isEmailConfirmed", pa.isEmailConfirmed());
                    return json;
                }).collect(Collectors.toList())));


        PrintWriter writer = resp.getWriter();

        jsonObject.toJson(writer);
    }
}
