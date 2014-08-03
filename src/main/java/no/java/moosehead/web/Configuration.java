package no.java.moosehead.web;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

public class Configuration {
    private static Map<String, String> confdata;

    private static synchronized Map<String, String> initConf() {
        String confFileName = System.getProperty("mooseheadConfFile");
        if (confFileName == null || confFileName.isEmpty()) {
            return null;
        }

        return null;
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
        return "http://test.2014.javazone.no/ems/server/events/9f40063a-5f20-4d7b-b1e8-ed0c6cc18a5f/sessions";
    }

    public static int placesPerWorkshop() {
        return Integer.parseInt(readConf("placesPerWorkshop", "30"));
    }

    public static OffsetDateTime openTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String defaultVal = LocalDateTime.now().atOffset(ZoneOffset.ofHours(2)).minusDays(2).format(format);
        String dateStr = readConf("openTime",defaultVal);

        ZoneOffset offset = ZoneOffset.ofHours(2);
        OffsetDateTime openstime = LocalDateTime.parse(dateStr, format).atOffset(offset);
        return openstime;
    }
}
