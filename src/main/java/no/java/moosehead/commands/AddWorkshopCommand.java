package no.java.moosehead.commands;

import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;
import java.util.Optional;

public class AddWorkshopCommand {
    private final String workshopId;
    private final Author author;
    private final int numberOfSeats;
    private final Instant startTime;
    private final Instant endTime;
    private final Optional<WorkshopData> workshopData;
    private final WorkshopType workshopType;


    public boolean hasStartAndEndTime() {
        return startTime != null && endTime != null;
    }

    public WorkshopType getWorkshopType() {
        return workshopType;
    }

    public static class Builder {
        private String workshopId;
        private Author author;
        private int numberOfSeats;
        private Instant startTime;
        private Instant endTime;
        private Optional<WorkshopData> workshopData = Optional.empty();
        private WorkshopType workshopType = WorkshopType.NORMAL_WORKSHOP;

        public Builder withWorkshopType(WorkshopType workshopType) {
            this.workshopType = workshopType;
            return this;
        }

        public Builder withWorkshopId(String workshopId) {
            this.workshopId = workshopId;
            return this;
        }

        public Builder withAuthor(Author author) {
            this.author = author;
            return this;
        }

        public Builder withNumberOfSeats(int numberOfSeats) {
            this.numberOfSeats = numberOfSeats;
            return this;
        }

        public Builder withStartTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder withEndTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder withWorkshopData(Optional<WorkshopData> workshopData) {
            this.workshopData = workshopData;
            return this;
        }

        public AddWorkshopCommand create() {
            return new AddWorkshopCommand(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private AddWorkshopCommand(Builder builder) {
        this.workshopId = builder.workshopId;
        this.author = builder.author;
        this.numberOfSeats = builder.numberOfSeats;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.workshopData = builder.workshopData;
        this.workshopType = builder.workshopType;
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

    public Optional<WorkshopData> getWorkshopData() {
        return workshopData;
    }

    public enum WorkshopType {
        KIDSAKODER_WORKSHOP,
        NORMAL_WORKSHOP
    }
}
