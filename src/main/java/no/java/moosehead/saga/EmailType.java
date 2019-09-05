package no.java.moosehead.saga;

import no.java.moosehead.commands.WorkshopTypeEnum;

import java.util.HashMap;
import java.util.Map;

public enum EmailType {

    CONFIRM_EMAIL("confirmEmail.txt","Javazone Workshop: Email confirmation needed"),
    RESERVATION_CONFIRMED("confirmReservation.txt","Welcome to Javazone Workshop",new SpecialHandling(WorkshopTypeEnum.NORMAL_WORKSHOP,"confirmLate.html")),
    RESERVATION_CANCELLED("confirmationCancelled.txt","Javazone Workshop: Cancellation confirmed"),
    WAITING_LIST("waitingList.txt","Javazone Workshop: You are on the waiting list"),
    WELCOME("welcome.txt","Javazone Workshop: Welcome");


    public static class SpecialHandling {
        public final WorkshopTypeEnum workshopTypeEnum;
        public final String template;

        SpecialHandling(WorkshopTypeEnum workshopTypeEnum,String template) {
            this.workshopTypeEnum = workshopTypeEnum;
            this.template = template;
        }


    }

    private final String template;
    private final String subject;
    private final Map<WorkshopTypeEnum,String> specialHandling;

    private EmailType(String template,String subject,SpecialHandling... specialHandlings) {
        this.template = template;
        this.subject = subject;
        this.specialHandling = new HashMap<>();
        for (SpecialHandling specialHandlingSetup : specialHandlings) {
            this.specialHandling.put(specialHandlingSetup.workshopTypeEnum,specialHandlingSetup.template);
        }
    }

    public String getTemplate() {
        return template;
    }

    public String getSubject() {
        return subject;
    }

    public Map<WorkshopTypeEnum, String> getSpecialHandling() {
        return new HashMap<>(specialHandling);
    }
}
