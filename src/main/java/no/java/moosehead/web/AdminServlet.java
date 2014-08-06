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

/**
 * Created by Tor Egil Refsahl on 06.08.2014.
 */
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
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
}
