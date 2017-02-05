package no.java.moosehead.eventstore.core;

import no.java.moosehead.database.Postgres;
import no.java.moosehead.eventstore.utils.ClassSerializer;
import no.java.moosehead.web.Configuration;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbEventStore implements Eventstore {
    private List<EventSubscription> subscribers = new ArrayList<>();
    private List<AbstractEvent> storage = new ArrayList<>();
    private final ClassSerializer classSerializer = new ClassSerializer();

    public DbEventStore() {
        try (
                Connection connection = Postgres.openConnection();
                PreparedStatement ps = connection.prepareStatement("select payload from event order by id");
                ResultSet resultSet = ps.executeQuery();
        ) {
            while (resultSet.next()) {
                AbstractEvent ev = classSerializer.asObject(resultSet.getString(1));
                storage.add(ev);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addEventSubscriber(EventSubscription eventSubscription) {
        subscribers.add(eventSubscription);
    }

    @Override
    public List<AbstractEvent> getEventstorageCopy() {
        return new ArrayList<>(storage);
    }

    @Override
    public void addEvent(AbstractEvent event) {
        if (!(event instanceof TransientEvent)) {
            try (
                Connection connection = Postgres.openConnection();
                PreparedStatement ps = connection.prepareStatement("insert into event(id,payload) VALUES (?,?)");
            ) {
               ps.setLong(1,event.getRevisionId());
               String payload = classSerializer.asString(event);
               ps.setString(2,payload);
               ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        storage.add(event);
        for (EventSubscription eventSubscribers : this.subscribers) {
            eventSubscribers.eventAdded(event);
        }
    }

    @Override
    public List<EventSubscription> getEventSubscribers() {
        return new ArrayList<>(subscribers);
    }
}
