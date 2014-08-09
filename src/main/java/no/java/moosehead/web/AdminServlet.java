package no.java.moosehead.web;

import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.controller.SystemSetup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminServlet  extends HttpServlet {

    private ParticipantApi participantApi;

    @Override
    public void init() throws ServletException {
        participantApi = SystemSetup.instance().workshopController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/workshopList".equals(req.getPathInfo())) {
            resp.setContentType("text/json");
            printWorkshops(resp);
        } else if ("/workshop".equals(req.getPathInfo())) {
            printWorkshopDetails(req, resp);
        } else if ("/alldata".equals(req.getPathInfo())) {
            printAllInfo(resp);
        } else {
            resp.getWriter().print("" +
                    "<html>Protected Admin API:<ul>" +
                    "   <li>/workshopList</li>" +
                    "   <li>/workshop?workshopid=[workshopid]</li>" +
                    "</html>");
        }

    }

    private void printAllInfo(HttpServletResponse resp) throws IOException {
        List<WorkshopInfo> workshops = participantApi.workshops();
        List<JSONObject> allInfo = workshops.stream()
                .sequential()
                .map(workshop -> {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("id", workshop.getId());
                        jsonObject.put("title", workshop.getTitle());
                        jsonObject.put("description", workshop.getDescription());
                        jsonObject.put("status", workshop.getStatus().name());

                        List<JSONObject> partList = workshop.getParticipants().stream().sequential()
                                .map(pa -> {
                                    JSONObject partObj = new JSONObject();

                                    try {
                                        partObj.put("email", pa.getEmail());
                                        partObj.put("name", pa.getName());
                                        partObj.put("isEmailConfirmed", pa.isEmailConfirmed());
                                        partObj.put("confirmedAt", pa.getConfirmedAt().map(ca -> ca.toString()).orElse("-"));
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return partObj;
                                })
                                .collect(Collectors.toList());
                        jsonObject.put("participants",new JSONArray(partList));

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return jsonObject;
                })
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
