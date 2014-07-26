package no.java.moosehead.repository;

import no.java.moosehead.api.MockApi;
import no.java.moosehead.api.WorkshopInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkshopRepository {
    private List<WorkshopInfo> workshops;

    public WorkshopRepository() {
        workshops = new ArrayList<>(MockApi.initWorkshops().values());
    }

    public List<WorkshopInfo> allWorkshops() {
        return new ArrayList<>(workshops);
    }

    public Optional<WorkshopInfo> workshopById(String id) {
        return allWorkshops().stream().filter(wi -> wi.getId().equals(id)).findFirst();
    }
}
