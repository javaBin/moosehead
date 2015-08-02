package no.java.moosehead.repository;

import no.java.moosehead.commands.WorkshopTypeEnum;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class WorkshopData {
    private String id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Optional<Instant> registrationOpens = Optional.empty();
    private WorkshopTypeEnum workshopTypeEnum;

    public boolean hasStartAndEndTime() {
        return startTime != null && endTime != null;
    }

    public WorkshopData(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.workshopTypeEnum = WorkshopTypeEnum.NORMAL_WORKSHOP;
    }

    public WorkshopData(String id, String title, String description, Instant startTime, Instant endTime, Optional<Instant> registrationOpens, WorkshopTypeEnum workshopTypeEnum) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.registrationOpens = registrationOpens;
        this.workshopTypeEnum = workshopTypeEnum;
    }

    public WorkshopData() {

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

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Optional<Instant> getRegistrationOpens() {
        return registrationOpens;
    }

    public WorkshopTypeEnum getWorkshopTypeEnum() {
        return workshopTypeEnum;
    }

    public String infoText() {
        StringBuilder res = new StringBuilder();
        res.append(getTitle());
        res.append(" (Start time: ");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM-yyy HH:mm").withZone(ZoneId.from(ZonedDateTime.now()));
        String datestr = Optional.ofNullable(getStartTime())
                .map(st -> dateTimeFormatter.format(st))
                .orElse("Unknown");
        res.append(datestr);
        res.append(")");
        return res.toString();
    }
}
