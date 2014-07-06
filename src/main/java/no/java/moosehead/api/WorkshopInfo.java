package no.java.moosehead.api;

public class WorkshopInfo {
    private String id;
    private String title;
    private String description;
    private WorkshopStatus status;

    public WorkshopInfo(String id, String title, String description, WorkshopStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
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
}
