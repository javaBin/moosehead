package no.java.moosehead.api;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.saga.EmailSender;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class WorkshopInfo {
    private String id;
    private String title;
    private String description;
    private List<Participant> participants;
    private WorkshopStatus status;
    private WorkshopTypeEnum workshopTypeEnum;
    private int numberOfSeats;
    private WorkshopData workshopData;
    private int numberOnWaitingList;

    public WorkshopInfo(WorkshopData workshopData, List<Participant> participants, WorkshopStatus status, WorkshopTypeEnum workshopTypeEnum, int numberOfSeats, int numberOnWaitingList) {
        this.id = workshopData.getId();
        this.title = workshopData.getTitle();
        this.description = workshopData.getDescription();
        this.participants = participants;
        this.status = status;
        this.workshopTypeEnum = workshopTypeEnum;
        this.numberOfSeats = numberOfSeats;
        this.workshopData = workshopData;
        this.numberOnWaitingList = numberOnWaitingList;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public WorkshopStatus getStatus() {
        return status;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public WorkshopTypeEnum getWorkshopTypeEnum() {
        return workshopTypeEnum;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getNumberOnWaitingList() {
        return numberOnWaitingList;
    }

    public int computeShownUps() {
        return participants.stream()
                .filter(Participant::isHasShownUp)
                .mapToInt(Participant::getNumberOfSeatsReserved)
                .sum();
    }

    public String workshopStartDate() {
        return Optional.ofNullable(workshopData.getStartTime()).map(EmailSender::formatInstant).orElse("Not set");
    }

    public String duration() {
        long minutes = Duration.between(workshopData.getStartTime(),workshopData.getEndTime()).getSeconds()/60L;
        return minutes + "minutes";
    }

    public int numberOfParticipants() {
        return participants.stream()
                .mapToInt(Participant::getNumberOfSeatsReserved)
                .sum();
    }

    public Optional<Instant> registrationOpensAt() {
        Optional<Instant> registrationOpens = workshopData.getRegistrationOpens();
        if (registrationOpens == null) {
            return Optional.empty();
        }
        return registrationOpens;
    }

}
