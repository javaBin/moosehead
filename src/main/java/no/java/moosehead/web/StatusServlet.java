package no.java.moosehead.web;

import org.jsonbuddy.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = {"/status"})
public class StatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        new JsonObject()
                .put("status", "ok")
                .put("localtime",LocalDateTime.now().toString())
                .put("instantnow", Instant.now().toString())
                .put("opentime", Configuration.openTime().toString())
                .toJson(resp.getWriter());
    }
}
