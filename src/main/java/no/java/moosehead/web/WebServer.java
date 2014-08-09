package no.java.moosehead.web;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.Collections;

public class WebServer {

    private final Integer port;
    private String warFile;

    public WebServer(Integer port, String warFile) {
        this.port = port;
        this.warFile = warFile;
    }

    public static void main(String[] args) throws Exception {
        String configFilename = null;
        String warFile = null;
        if (args.length > 0) {
            configFilename = args[0];
            System.setProperty("mooseheadConfFile",configFilename);
        } else {
            System.out.println("Running without config");
        }
        if (args.length > 1) {
            warFile = args[1];
        }
        new WebServer(getPort(8088),warFile).start();
    }

    private void start() throws Exception {
        Server server = new Server(port);

        WebAppContext webAppContext;
        if (warFile != null) {
            webAppContext = new WebAppContext();
            webAppContext.setContextPath("/");
            webAppContext.setWar(warFile);
        } else {
            webAppContext = new WebAppContext("src/main/webapp", "/");
            webAppContext.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            setupLogging();
        }

        /*if (Configuration.secureAdmin()) {
            setupSecurity(server, webAppContext);
        }*/
        server.setHandler(webAppContext);

        server.start();
        System.out.println(server.getURI());
    }

    private void setupLogging() {
        //LogManager.getRootLogger().setLevel(Level.INFO);
        LogManager.getLogger("org.eclipse.jetty").setLevel(Level.INFO);
    }

    private void setupSecurity(Server server, WebAppContext theContextToSecure) {
        LoginService loginService = new HashLoginService("MooseRealm",Configuration.loginConfigLocation());
        server.addBean(loginService);

        Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"adminrole"});

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/admin/*");
        mapping.setConstraint(constraint);

        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setRealmName("MooseRealm");
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);

        security.setHandler(theContextToSecure);

        server.setHandler(security);
    }

    private static int getPort(int defaultPort) {
        Integer serverPort = Configuration.serverPort();
        return serverPort != null ? serverPort : defaultPort;
    }
}