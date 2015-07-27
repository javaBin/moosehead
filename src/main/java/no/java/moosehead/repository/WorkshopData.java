package no.java.moosehead.repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

public class WorkshopData {
    private String id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Optional<Instant> registrationOpens;

    public boolean hasStartAndEndTime() {
        return startTime != null && endTime != null;
    }

    public WorkshopData(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public WorkshopData(String id, String title, String description, Instant startTime, Instant endTime, Optional<Instant> registrationOpens) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.registrationOpens = registrationOpens;
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
}
