package no.java.moosehead.commands;

public class AddWorkshopCommand {
    private String workshopId;

    public AddWorkshopCommand(String workshopId) {
        this.workshopId = workshopId;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String toString() {
        return "AddWorkshopCommand for workshop " + workshopId;
    }
}
