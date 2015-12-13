package no.java.moosehead.web;

import org.jsonbuddy.JsonNode;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Optional;

@WebServlet(urlPatterns = {"/oauth2callback/*"})
public class GoogleLoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (Optional.ofNullable(req.getPathInfo()).orElse("unknown")) {
            case "/login":
                handleLogin(req,resp);
                break;
            default:
                handleAuthorization(req, resp);
        }
    }



    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        String sendMeTo = req.getParameter("sendMeTo");
        if (sendMeTo != null) {
            session.setAttribute("sendMeTo",sendMeTo);
        }
        String redir = Configuration.mooseheadLocation() + "/oauth2callback";
        session.setAttribute("redir", redir);
        String sessionid = session.getId();
        // redirect to google for authorization
        StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
                .append("?client_id=").append(Configuration.googleClientId()) // the client id from the api console registration
                .append("&response_type=code")
                .append("&scope=openid%20email") // scope is the api permissions we are requesting
                .append("&redirect_uri=").append(redir) // the servlet that google redirects to after authorization
                .append("&state=" + sessionid)
                .append("&access_type=online")
                .append("&approval_prompt=auto") // here we are asking to access to user's data while they are not signed in
                ;

        resp.sendRedirect(oauthUrl.toString());
    }

    private void handleAuthorization(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        if (req.getParameter("error") != null) {
            writer.append(req.getParameter("error"));
            return;
        }

        String code = req.getParameter("code");

        String redir = (String) req.getSession().getAttribute("redir");
        req.getSession().setAttribute("redir", null);

        if (code == null || redir == null) {
            resp.sendRedirect("/");
            return;
        }

        StringBuilder postParameters = new StringBuilder();
        postParameters.append(para("code", code)).append("&");
        postParameters.append(para("client_id", Configuration.googleClientId())).append("&");
        postParameters.append(para("client_secret", Configuration.googleClientSecret())).append("&");
        postParameters.append(para("redirect_uri", redir)).append("&");
        postParameters.append(para("grant_type", "authorization_code"));
        URL url = new URL("https://accounts.google.com/o/oauth2/token");
        URLConnection urlConnection = url.openConnection();


        ((HttpURLConnection) urlConnection).setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", "" + postParameters.toString().length());

        // Create I/O streams
        DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());
        // Send request
        outStream.writeBytes(postParameters.toString());
        outStream.flush();
        outStream.close();


        String googleJson = toString(urlConnection.getInputStream());

        JsonObject jsonObject = (JsonObject) JsonParser.parse(googleJson);
        String accessToken = jsonObject.requiredString("access_token");

        // get some info about the user with the access token
        String getStr = "https://www.googleapis.com/oauth2/v1/userinfo?" + para("access_token", accessToken);
        URLConnection inconn = new URL(getStr).openConnection();
        String gsstr;
        try (InputStream is = inconn.getInputStream()) {
            gsstr = toString(is);
        }

        updateUserLogin(req, gsstr);
        redirToLandingPage(req,resp);
    }

    private void redirToLandingPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String redir = "/";
        HttpSession session = req.getSession();
        String sendMeTo = (String) session.getAttribute("sendMeTo");
        if (sendMeTo != null) {
            redir = sendMeTo;
            session.removeAttribute("sendMeTo");
        }
        resp.sendRedirect(redir);
    }

    private void updateUserLogin(HttpServletRequest req, String gsstr) throws IOException {
        JsonNode googleAuth = JsonParser.parse(gsstr);
        JsonObject objnode = (JsonObject) googleAuth;

        String googleId = objnode.requiredString("id");
        boolean isAdmin = Configuration.adminGoogleIds().contains(googleId);
        objnode.put("admin",isAdmin);

        System.out.println("Setting user logged in " + objnode);
        req.getSession().setAttribute("user", objnode);
    }

    private static String para(String name, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(name, "utf-8") + "=" + URLEncoder.encode(value, "UTF-8");
    }

    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
            return result.toString();
        }
    }
}
