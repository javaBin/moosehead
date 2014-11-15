package no.java.moosehead.web;

import java.io.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    private static String toString(InputStream inputStream) throws IOException {
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

    public static String emsEventLocation() {
        return readConf("emsEventLocation","http://test.2014.javazone.no/ems/server/events/9f40063a-5f20-4d7b-b1e8-ed0c6cc18a5f/sessions");
    }

    public static String loginConfigLocation() {
        return readConf("loginConfigLocation","realm.properties");
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

    public static OffsetDateTime openTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String defaultVal = LocalDateTime.now().atOffset(ZoneOffset.ofHours(2)).minusDays(2).format(format);
        String dateStr = readConf("openTime",defaultVal);

        ZoneOffset offset = ZoneOffset.ofHours(2);
        OffsetDateTime openstime = LocalDateTime.parse(dateStr, format).atOffset(offset);
        return openstime;
    }

    public static String smtpServer() {
        return readConf("smtpServer",null);
    }

    public static int smtpPort() {
        return Integer.parseInt(readConf("smtpPort","25"));
    }

    public static String bccTo() {
        return readConf("mailbcc",null);
    }

    public static String mooseheadLocation() {
        return readConf("mooseheadLocation","http://localhost:8088");
    }

    public static String eventstoreFilename() {
        return readConf("eventstoreFilename",null);
    }

    public static boolean isProdEnviroment() {
        return "false".equals(readConf("testenv","true"));
    }

    public static boolean secureAdmin() {
        return "true".equals(readConf("secureAdmin","true"));
    }

    public static String adminPassword() {
        return readConf("adminPassword","password");
    }

    public static int veryFullNumber() {
        return Integer.parseInt(readConf("veryFullNumber","20"));
    }

    public static String logonRedirectUrl() {
        return readConf("logonRedirectUrl","http://localhost:9997/sso/login?redirectURI=http://localhost:8088/admin/");
    }

    public static String tokenServiceUrl() {
        return readConf("tokenServiceUrl","http://localhost:9998/tokenservice");
    }

    public static String applicationId() {
        return readConf("applicationId","99");
    }

    public static String applicationSecret() { return readConf("applicationSecret","33879936R6Jr47D4Hj5R6p9qT");}

}
