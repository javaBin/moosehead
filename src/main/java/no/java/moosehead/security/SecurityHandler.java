package no.java.moosehead.security;

import no.java.moosehead.web.Configuration;

import javax.servlet.http.HttpSession;

class SecurityHandler {
    public SecurityHandler() {
    }

    boolean login(String password, HttpSession session) {
        if (!Configuration.adminPassword().equals(password)) {
            return false;
        }
        session.setAttribute("logged","true");
        return true;
    }

    boolean isLoggedIn(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("logged"));
    }
}
