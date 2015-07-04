package no.java.moosehead.commands;

public class AddWorkshopCommand {
    private String workshopId;
    private Author author;
    private int numberOfSeats;

    public AddWorkshopCommand(String workshopId, Author author, int numberOfSeats)
    {
        this.workshopId = workshopId;
        this.author = author;
        this.numberOfSeats= numberOfSeats;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String toString() {
        return "AddWorkshopCommand for workshop " + workshopId;
    }

    public Author getAuthor() {
        return author;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public enum Author {
        SYSTEM, ADMIN
    }
}
