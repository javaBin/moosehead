package no.java.moosehead.web;

import no.java.moosehead.database.Postgres;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration.ClassList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;

public class WebServer {

    private final Integer port;

    private WebServer(Integer port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        String configFilename = null;
        if (args.length > 0) {
            configFilename = args[0];
            System.setProperty("mooseheadConfFile",configFilename);
        } else {
            System.out.println("Running without config");
        }
        new WebServer(getPort(8088)).start();
    }

    private void start() throws Exception {
        Server server = new Server(port);

        initDb();

        ClassList classlist = ClassList.setServerDefault(server);
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration","org.eclipse.jetty.annotations.AnnotationConfiguration");

        WebAppContext webAppContext;
        webAppContext = new WebAppContext();
        webAppContext.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webAppContext.setContextPath("/");

        // AnnotationConfiguration scanner BARE WebInfClasses/libs og container jars, så vi må simulere dette ved å legge til URL til denne klassens.
        final URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        webAppContext.getMetaData().setWebInfClassesDirs(Arrays.asList(Resource.newResource(location)));

        setupLogging();

        if (Configuration.isDevEnviroment()) {
            // Development ie running in ide
            System.out.println("Warning: You are running in your IDE!!!");
            webAppContext.setResourceBase("src/main/resources/webapp");

        } else {
            // Prod ie running from jar
            webAppContext.setBaseResource(Resource.newClassPathResource("webapp", true, false));
        }

         Handler serverHandler = webAppContext;

        /*
        if (Configuration.secureAdmin()) {
            serverHandler = setupSecurity(server, webAppContext);
        } else {
            serverHandler = webAppContext;
        }*/

        server.setHandler(serverHandler);

        server.start();

        System.out.println("Starting at " + LocalDateTime.now());
    }

    private void initDb() {
        if (Configuration.dbName() == null) {
            return;
        }
        Flyway flyway = Flyway.configure().dataSource(Postgres.source()).load();
        if (Configuration.cleanDb()) {
            System.out.println("Cleaning db");
            flyway.clean();
        }
        flyway.migrate();

    }


    private void setupLogging() {
        //LogManager.getRootLogger().setLevel(Level.INFO);
        //LogManager.getLogger("org.eclipse.jetty").setLevel(Level.INFO);
        //LogManager.getLogger("org.eclipse.jetty.security").setLevel(Level.TRACE);
    }


    private static int getPort(int defaultPort) {
        Integer serverPort = Configuration.serverPort();
        return serverPort != null ? serverPort : defaultPort;
    }
}