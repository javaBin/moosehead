package no.java.moosehead.commands;

import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;
import java.util.Optional;

public class AddWorkshopCommand {
    private final String workshopId;
    private final AuthorEnum authorEnum;
    private final int numberOfSeats;
    private final Instant startTime;
    private final Instant endTime;
    private final Optional<WorkshopData> workshopData;
    private final WorkshopTypeEnum workshopTypeEnum;


    public boolean hasStartAndEndTime() {
        return startTime != null && endTime != null;
    }

    public WorkshopTypeEnum getWorkshopTypeEnum() {
        return workshopTypeEnum;
    }

    public static class Builder {
        private String workshopId;
        private AuthorEnum authorEnum;
        private int numberOfSeats;
        private Instant startTime;
        private Instant endTime;
        private Optional<WorkshopData> workshopData = Optional.empty();
        private WorkshopTypeEnum workshopTypeEnum = WorkshopTypeEnum.NORMAL_WORKSHOP;

        public Builder withWorkshopType(WorkshopTypeEnum workshopTypeEnum) {
            this.workshopTypeEnum = workshopTypeEnum;
            return this;
        }

        public Builder withWorkshopId(String workshopId) {
            this.workshopId = workshopId;
            return this;
        }

        public Builder withAuthor(AuthorEnum authorEnum) {
            this.authorEnum = authorEnum;
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
        this.authorEnum = builder.authorEnum;
        this.numberOfSeats = builder.numberOfSeats;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.workshopData = builder.workshopData;
        this.workshopTypeEnum = builder.workshopTypeEnum;
    }


    public String getWorkshopId() {
        return workshopId;
    }

    public String toString() {
        return "AddWorkshopCommand for workshop " + workshopId;
    }

    public AuthorEnum getAuthorEnum() {
        return authorEnum;
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

}
