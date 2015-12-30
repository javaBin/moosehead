package no.java.moosehead.domain;

import no.java.moosehead.eventstore.utils.TokenGenerator;
import org.jsonbuddy.JsonObject;

import java.util.Optional;

public class WorkshopReservation {
    private final String email;
    private final String fullname;
    private final String workshopId;
    private final String reservationToken;
    private final int numberOfSeatsReserved;
    private final JsonObject additionalInfo;
    private final long systemTimeInMillis;
    private final long revisionId;
    private final Optional<String> googleUserEmail;


    private WorkshopReservation(Builder builder) {
        this.email = builder.email;
        this.fullname = builder.fullname;
        this.workshopId = builder.workshopId;
        this.reservationToken = builder.reservationToken;
        this.numberOfSeatsReserved = builder.numberOfSeatsReserved;
        this.additionalInfo = builder.additionalInfo;
        this.systemTimeInMillis = builder.systemTimeInMillis;
        this.revisionId = builder.revisionId;
        this.googleUserEmail = builder.googleUserEmail;
    }

    public WorkshopReservation() {
        email = null;
        fullname = null;
        workshopId = null;
        reservationToken = null;
        numberOfSeatsReserved = 0;
        additionalInfo = null;
        systemTimeInMillis = 0;
        revisionId = 0;
        googleUserEmail = null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String fullname;
        private String workshopId;
        private int numberOfSeatsReserved = 1;
        private Optional<String> googleUserEmail = Optional.empty();
        private String reservationToken = TokenGenerator.randomUUIDString();
        private JsonObject additionalInfo;
        private long systemTimeInMillis;
        private long revisionId;


        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setFullname(String fullname) {
            this.fullname = fullname;
            return this;
        }

        public Builder setWorkshopId(String workshopId) {
            this.workshopId = workshopId;
            return this;
        }

        public Builder setNumberOfSeatsReserved(int numberOfSeatsReserved) {
            this.numberOfSeatsReserved = numberOfSeatsReserved;
            return this;
        }

        public Builder setGoogleUserEmail(Optional<String> googleUserEmail) {
            this.googleUserEmail = googleUserEmail;
            return this;
        }

        public Builder setSystemTimeInMillis(long systemTimeInMillis) {
            this.systemTimeInMillis = systemTimeInMillis;
            return this;
        }

        public Builder setRevisionId(long revisionId) {
            this.revisionId = revisionId;
            return this;
        }

        public Builder setReservationToken(String reservationToken) {
            this.reservationToken = reservationToken;
            return this;
        }

        public Builder setAdditionalInfo(JsonObject additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        public WorkshopReservation create() {
            return new WorkshopReservation(this);
        }

    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String getReservationToken() {
        return reservationToken;
    }

    public int getNumberOfSeatsReserved() {
        return numberOfSeatsReserved;
    }

    public JsonObject getAdditionalInfo() {
        return additionalInfo;
    }

    public long getSystemTimeInMillis() {
        return systemTimeInMillis;
    }

    public long getRevisionId() {
        return revisionId;
    }

    public Optional<String> getGoogleUserEmail() {
        return googleUserEmail;
    }
}
