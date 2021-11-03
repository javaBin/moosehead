package no.java.moosehead.web;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Configuration {
    private static Map<String, String> confdata;

    private static synchronized Map<String, String> initConf() {
        if (confdata != null) {
            return confdata;
        }
        String confFileName = System.getProperty("mooseheadConfFile");
        if (confFileName == null || confFileName.isEmpty()) {
            return null;
        }
        String confFileContent;
        try (InputStream is = new FileInputStream(new File(confFileName))) {
             confFileContent = toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String,String> result = readConfigData(confFileContent);


        return result;
    }

    private static Map<String, String> readConfigData(String confFileContent) {
        Map<String,String> result = new HashMap<>();
        String enviroment = null;
        boolean skip = false;
        for (String line : confFileContent.split("\n")) {
            line=line.trim();
            if (line.startsWith("#") || line.trim().isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                skip = !(line.equals("[default]") || line.equals(enviroment));
                continue;
            }
            if (skip) {
                continue;
            }

            int pos = line.indexOf(("="));
            String key = line.substring(0, pos);
            String value = line.substring(pos + 1);
            if ("configenviroment".equals(key)) {
                enviroment = value;
            }
            result.put(key, value);
        }
        return result;
    }


    public static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }


    private Configuration() {
    }


    private static String readConf(String key,String defVal) {
        if (confdata == null) {
            confdata = initConf();
            if (confdata == null) {
                return defVal;
            }
        }
        String res = confdata.get(key);
        if (res == null) {
            return defVal;
        }
        return res;
    }

    public static void initData(Map<String,String> givenData) {
        confdata = givenData;
    }

    public static Integer serverPort() {
        return Integer.parseInt(readConf("serverPort","8088"));
    }

    public static Optional<String> emsEventLocation() {
        String emsEventLocation = readConf("emsEventLocation", "null");
        if ("null".equals(emsEventLocation)) {
            return Optional.empty();
        }
        return Optional.of(emsEventLocation);
    }


    public static int placesPerWorkshop() {
        return Integer.parseInt(readConf("placesPerWorkshop", "30"));
    }

    public static List<String> closedWorkshops() {
        String closedWorkshops = readConf("closedWorkshops",null);
        if (closedWorkshops == null || closedWorkshops.isEmpty()) {
            return new ArrayList<String>();
        }
        String[] split = closedWorkshops.split(",");
        List<String> res = new ArrayList<>();
        for (String s : split) {
            res.add(s);
        }
        return res;
    }

    public static ZonedDateTime openTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String defaultVal = LocalDateTime.now().atZone(ZoneId.of("Europe/Oslo")).minusDays(2).format(format);
        String dateStr = readConf("openTime",defaultVal);

        ZonedDateTime openstime = LocalDateTime.parse(dateStr, format).atZone(ZoneId.of("Europe/Oslo"));
        return openstime;
    }

    public static String smtpServer() {
        return readConf("smtpServer", null);
    }

    public static boolean useMailSSL() {
        return "true".equals(readConf("mailSsl","false"));
    }

    public static String mailUser() {
        return readConf("mailUser",null);
    }

    public static String mailPassword() {
        return readConf("mailPassword",null);
    }


    public static int smtpPort() {
        return Integer.parseInt(readConf("smtpPort", "25"));
    }

    public static String bccTo() {
        return readConf("mailbcc", null);
    }

    public static String mooseheadLocation() {
        return readConf("mooseheadLocation","http://localhost:8088");
    }

    public static String eventstoreFilename() {
        return readConf("eventstoreFilename", null);
    }

    public static String dbServer() {
        return readConf("dbServer", "localhost");
    }

    public static String dbName() {
        return readConf("dbName", null);
    }


    public static String dbUser() {
        return readConf("dbUser", null);
    }

    public static String dbPassword() {
        return readConf("dbPassword", null);
    }

    public static int dbPort() {
        return Integer.parseInt(readConf("dbPort","5432"));
    }

    public static int maxDbConnections() {
        return Integer.parseInt(readConf("maxDbConnections","10"));
    }

    public static boolean cleanDb() {
        return "true".equals(readConf("cleanDb","false"));
    }




    public static boolean isProdEnviroment() {
        return "false".equals(readConf("testenv", "true"));
    }

    public static int veryFullNumber() {
        return Integer.parseInt(readConf("veryFullNumber", "20"));
    }

    public static int fewSpotsNumber() {
        return Integer.parseInt(readConf("fewSpotsNumber", "5"));
    }

    public static String googleClientId() {
        return readConf("googleClientId", "xxx");
    }

    public static String googleClientSecret() {
        return readConf("googleClientSecret", "xxx");
    }

    public static String adminGoogleIds() {
        return readConf("adminGoogleIds","");
    }

    public static boolean secureAdmin() {
        return "true".equals(readConf("secureAdmin", "true"));
    }

    public static String emsEventsFile() {
        return readConf("emsEventsFile", null);
    }

    public static int maxNumberOfSeatsToReserve() {
        return Integer.parseInt(readConf("maxNumberOfSeatsToReserve", "3"));
    }

    public static void setConfdata(Map<String, String> confdata) {
        Configuration.confdata = confdata;
    }

    public static long emailSleepTime() {
        return Long.parseLong(readConf("emailSleepTime","5000"));
    }

    public static boolean isDevEnviroment() {
        return "true".equals(readConf("devEnviroment","true"));
    }

    public static String sendGridKey() {
        return readConf("sendGridKey",null);
    }
}
