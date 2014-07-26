package no.java.moosehead.repository;

import net.hamnaberg.json.Collection;
import net.hamnaberg.json.Item;
import net.hamnaberg.json.Property;
import net.hamnaberg.json.parser.CollectionParser;
import no.java.moosehead.web.Configuration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopRepository {
    private List<WorkshopData> workshops;

    public WorkshopRepository() {
        List<Item> items = readItems();

        workshops = items.stream()
                .map(it -> it.getDataAsMap())
                .filter(dasm -> {
                    Property published = dasm.get("published");
                    if (!published.hasValue()) {
                        return false;
                    }
                    Property format = dasm.get("format");
                    if (!format.hasValue()) {
                        return false;
                    }
                    return published.getValue().get().asBoolean() && "workshop".equals(format.getValue().get().asString());
                })
                .map(dasm -> {
                    String title = dasm.get("title").getValue().get().asString();
                    String summary = dasm.get("summary").getValue().get().asString();
                    String slug = dasm.get("slug").getValue().get().asString();
                    return new WorkshopData(slug,title,summary);
                })
                .collect(Collectors.toList());


    }

    private List<Item> readItems() {
        URL url;
        try {
            url = new URL(Configuration.emsEventLocation());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Collection events;
        try {
            events = new CollectionParser().parse(urlConnection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return events.getItems();
    }

    public List<WorkshopData> allWorkshops() {
        return new ArrayList<>(workshops);
    }

    public Optional<WorkshopData> workshopById(String id) {
        return allWorkshops().stream().filter(wi -> wi.getId().equals(id)).findFirst();
    }

}
