package no.java.moosehead.api;

import de.svenjacobs.loremipsum.LoremIpsum;

import java.util.*;
import java.util.stream.Collectors;

public class MockApi implements  ParticipantApi {
    private static Map<String,Set<String>> participants = initMap();
    private static Map<String,WorkshopInfo> workshops = initWorkshops();

    public static Map<String,WorkshopInfo> initWorkshops() {
        LoremIpsum lips = new LoremIpsum();
        Map<String, WorkshopInfo> result = new HashMap<>();

        result.put("1",new WorkshopInfo("1","Workshop one",lips.getWords(),WorkshopStatus.FREE_SPOTS));
        result.put("2",new WorkshopInfo("2","Workshop two",lips.getWords(),WorkshopStatus.NOT_OPENED));
        result.put("3",new WorkshopInfo("3","Workshop three",lips.getWords(),WorkshopStatus.CLOSED));
        result.put("4",new WorkshopInfo("4","Workshop four",lips.getWords(),WorkshopStatus.FEW_SPOTS));
        return result;
    }

    private static Map<String, Set<String>> initMap() {
        Map<String, Set<String>> part = new HashMap<>();
        return part;
    }

    @Override
    public List<WorkshopInfo> workshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public ParticipantActionResult reservation(String workshopid, String email, String fullname) {
        if (!("1".equals(workshopid) || "4".equals(workshopid))) {
            return ParticipantActionResult.error("Workshop not open");
        }
        Set<String> myWorkshops = participants.get(email);
        if (myWorkshops == null) {
            myWorkshops = new HashSet<>();
            participants.put(email,myWorkshops);
        }
        myWorkshops.add(workshopid);
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult confirmEmail(String token) {
        return ParticipantActionResult.ok();
    }

    @Override
    public ParticipantActionResult cancellation(String workshopid, String email) {
        Set<String> myWorkshops = participants.get(email);
        if (myWorkshops == null) {
            return ParticipantActionResult.error("Not signed up");
        }
        myWorkshops.add(workshopid);
        return ParticipantActionResult.ok();
    }

    @Override
    public List<ParticipantReservation> myReservations(String email) {
        Set<String> myWorkshops = participants.get(email);
        if (myWorkshops == null) {
            return new ArrayList<>();
        }
        return myWorkshops.stream().map(wsid -> new ParticipantReservation(email,wsid)).collect(Collectors.toList());
    }
}
