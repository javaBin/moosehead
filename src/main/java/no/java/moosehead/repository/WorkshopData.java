package no.java.moosehead.repository;

import no.java.moosehead.api.WorkshopStatus;

public class WorkshopData {
    private String id;
    private String title;
    private String description;

    public WorkshopData(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
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

}
