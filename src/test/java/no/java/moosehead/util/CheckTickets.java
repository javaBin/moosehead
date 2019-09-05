package no.java.moosehead.util;

import no.java.moosehead.web.Configuration;
import org.jsonbuddy.JsonObject;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckTickets {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage participantFile ticketfile");
            return;
        }
        JsonObject regobj = CheckDoubleBookings.readRegistrations(args[0]);
        Set<String> allTickets = readTicketFile(args[1]);
        printNoTickets(regobj,allTickets);
    }

    private static void printNoTickets(JsonObject regobj, Set<String> allTickets) {
        for (String email : regobj.keys()) {
            if (!allTickets.contains(email.toLowerCase())) {
                System.out.println(email);
            }
        }
    }

    private static Set<String> readTicketFile(String filename) throws Exception {
        String content = Configuration.toString(new FileInputStream(filename));
        return Arrays.stream(content.split("\n")).map(String::toLowerCase).collect(Collectors.toSet());
    }
}
