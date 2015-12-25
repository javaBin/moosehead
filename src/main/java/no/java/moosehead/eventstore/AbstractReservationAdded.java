package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.utils.TokenGenerator;

import java.util.Optional;

public abstract class AbstractReservationAdded extends AbstractEvent implements UserWorkshopEvent {
    private String email;
    private String fullname;
    private String workshopId;
    private String reservationToken;
    private int numberOfSeatsReserved;

    public AbstractReservationAdded() {
    }

    public AbstractReservationAdded(Builder builder) {
        super(builder.systemTimeInMillis,builder.revisionId);
        this.email = builder.email;
        this.fullname = builder.fullname;
        this.workshopId = builder.workshopId;
        this.numberOfSeatsReserved = builder.numberOfSeatsReserved;
        this.reservationToken = TokenGenerator.randomUUIDString();
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long systemTimeInMillis;
        private long revisionId;
        private String email;
        private String fullname;
        private String workshopId;
        private int numberOfSeatsReserved;
        protected Optional<String> googleUserEmail;

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

        public ReservationAddedByAdmin createByAdmin() {
            return new ReservationAddedByAdmin(this);
        }

        public ReservationAddedByUser createByUser() {
            return new ReservationAddedByUser(this);
        }
    }
}
