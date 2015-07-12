package no.java.moosehead.commands;

import java.time.Instant;

public class AddWorkshopCommand {
    private String workshopId;
    private Author author;
    private int numberOfSeats;
    private Instant startTime;
    private Instant endTime;

    public boolean hasStartAndEndTime() {
        return startTime != null && endTime != null;
    }

    public AddWorkshopCommand(String workshopId, Author author, int numberOfSeats)
    {
        this.workshopId = workshopId;
        this.author = author;
        this.numberOfSeats= numberOfSeats;
    }

    public AddWorkshopCommand(String workshopId, Author author, int numberOfSeats, Instant startTime, Instant endTime)
    {
        this.workshopId = workshopId;
        this.author = author;
        this.numberOfSeats= numberOfSeats;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }


}
