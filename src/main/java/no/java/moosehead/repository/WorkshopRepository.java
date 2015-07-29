package no.java.moosehead.repository;

import net.hamnaberg.json.Collection;
import net.hamnaberg.json.Item;
import net.hamnaberg.json.Link;
import net.hamnaberg.json.Property;
import net.hamnaberg.json.extension.Tuple2;
import net.hamnaberg.json.parser.CollectionParser;
import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.eventstore.WorkshopAddedEvent;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.web.Configuration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopRepository implements EventSubscription {
    private List<WorkshopData> workshops;

    public WorkshopRepository() {
        List<Item> items = readItems();

        List<WorkshopData> emsWorkshops = items.stream()
                .map(it -> new Tuple2<>(
                        it.getDataAsMap(),
                        it.linkByRel("slot item")))
                .filter(dasm -> {
                    Map<String, Property> spMap = dasm._1;
                    Property published = spMap.get("published");
                    if (!published.hasValue()) {
                        return false;
                    }
                    Property format = spMap.get("format");
                    if (!format.hasValue()) {
                        return false;
                    }
                    return published.getValue().get().asBoolean() && "workshop".equals(format.getValue().get().asString());
                })
                .map(dasm -> {
                    Map<String, Property> spMap = dasm._1;
                    net.hamnaberg.funclite.Optional<Link> startsAndEnds = dasm._2;
                    String title = spMap.get("title").getValue().get().asString();
                    String summary = spMap.get("summary").getValue().get().asString();
                    String slug = spMap.get("slug").getValue().get().asString();
                    if (startsAndEnds.isSome()) {
                        String[] dates = startsAndEnds.get().getPrompt().get().split("\\+");
                        Instant starts = Instant.parse(dates[0]);
                        Instant ends = Instant.parse(dates[1]);
                        return new WorkshopData(slug, title, summary, starts, ends, Optional.empty(), WorkshopTypeEnum.NORMAL_WORKSHOP);
                    } else {
                        return new WorkshopData(slug, title, summary);
                    }
                })
                .collect(Collectors.toList());
        workshops = new ArrayList<>(emsWorkshops);
    }

    private List<Item> readItems() {
        InputStream inputStream = openEventInputStream();

        Collection events;
        try {
            events = new CollectionParser().parse(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return events.getItems();
    }

    private InputStream openEventInputStream() {
        String eventFile = Configuration.emsEventsFile();
        if (eventFile != null) {
            try {
                return new FileInputStream(new File(eventFile));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        URL url;
        try {
            url = new URL(Configuration.emsEventLocation());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        URLConnection urlConnection;
        InputStream inputStream;
        try {
            urlConnection = url.openConnection();
            inputStream = urlConnection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    public List<WorkshopData> allWorkshops() {
        return new ArrayList<>(workshops);
    }

    public Optional<WorkshopData> workshopById(String id) {
        return allWorkshops().stream().filter(wi -> wi.getId().equals(id)).findFirst();
    }

    @Override
    public void eventAdded(AbstractEvent event) {
        if (event instanceof WorkshopAddedEvent) {
            WorkshopAddedEvent workshopAddedEvent = (WorkshopAddedEvent) event;
            Optional<WorkshopData> workshopData = workshopAddedEvent.getWorkshopData();
            if (workshopData.isPresent()) {
                workshops.add(workshopData.get());
            }
        }
    }
}
