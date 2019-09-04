package no.java.moosehead.util;

import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CheckDoubleBookings {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage reportfile");
            return;
        }
        JsonObject conflicts = readConflicts();
        JsonObject registration = readRegistrations(args[0]);
        printConflicts(conflicts,registration);

    }

    private static JsonObject readRegistrations(String filname) throws Exception {
        JsonObject result;
        try (InputStream is = new FileInputStream(filname)) {
            result = JsonParser.parseToObject(is);
        }
        return result;
    }

    private static void printConflicts(JsonObject conflicts,JsonObject registrations) {
        for (String email : registrations.keys()) {
            JsonArray regOnWs = registrations.requiredArray(email);
            for (int i=0;i<regOnWs.size()-1;i++) {
                String wsa = regOnWs.requiredString(i);
                List<String> possibleConflicts = conflicts.requiredArray(wsa).strings();
                for (int j=i+1;j<regOnWs.size();j++) {
                    String wsb = regOnWs.requiredString(j);
                    if (possibleConflicts.contains(wsb)) {
                        System.out.println(email+ "->" + wsa + "," + wsb);
                    }
                }
            }
        }
    }

    private static class WSInfo {
        public final String id;
        public final LocalTime start;
        public final LocalTime end;


        private WSInfo(String id, LocalTime start, LocalTime end) {
            this.id = id;
            this.start = start;
            this.end = end;
        }

        public boolean hasConflict(WSInfo other) {
            if (other.end.isBefore(this.start)) {
                return false;
            }
            if (this.end.isBefore(other.start)) {
                return false;
            }
            return true;
        }
    }

    private static JsonObject readConflicts()  throws Exception {
        JsonArray all;
        try (InputStream is = new URL("https://sleepingpill.javazone.no/public/conference/99f71831-fdd3-41e3-962e-f25af5e091b9/session").openConnection().getInputStream()) {
            all = JsonParser.parseToObject(is).requiredArray("sessions");
        }
        List<WSInfo> allWs = new ArrayList<>();
        all.objectStream().forEach(jsonobj -> {
            if (!"workshop".equals(jsonobj.stringValue("format").orElse(""))) {
                return;
            }
            String registerLoc = jsonobj.requiredString("registerLoc");
            int idind = registerLoc.lastIndexOf("/");
            String id;
            try {
                id = URLDecoder.decode(registerLoc.substring(idind+1),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            LocalTime start = LocalDateTime.parse(jsonobj.requiredString("startTime")).toLocalTime();
            LocalTime end = LocalDateTime.parse(jsonobj.requiredString("endTime")).toLocalTime();
            allWs.add(new WSInfo(id,start,end));
        });
        JsonObject result = new JsonObject();
        for (int i=0;i<allWs.size();i++) {
            JsonArray conflictsForThis = new JsonArray();
            WSInfo wsa = allWs.get(i);
            for (int j=0;j<allWs.size();j++) {
                if (i == j) {
                    continue;
                }
                WSInfo wsb = allWs.get(j);
                if (wsa.hasConflict(wsb)) {
                    conflictsForThis.add(wsb.id);
                }
            }
            result.put(wsa.id,conflictsForThis);
        }
        return result;
    }
}
