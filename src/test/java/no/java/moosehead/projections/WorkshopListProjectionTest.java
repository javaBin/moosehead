package no.java.moosehead.projections;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkshopListProjectionTest {
    @Test
    public void shouldReturnAddedWorkshop() throws Exception {
        SystemSetup systemSetup = mock(SystemSetup.class);
        WorkshopRepository workshopRepository = mock(WorkshopRepository.class);
        Optional<WorkshopData> optworkshop = Optional.of(new WorkshopData("one","title","description"));

        when(workshopRepository.workshopById("one")).thenReturn(optworkshop);
        when(systemSetup.workshopRepository()).thenReturn(workshopRepository);

        SystemSetup.setSetup(systemSetup);

        WorkshopListProjection workshopListProjection = new WorkshopListProjection();
        workshopListProjection.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L,"one",30));

        List<Workshop> workshops = workshopListProjection.getWorkshops();

        assertThat(workshops).hasSize(1);

        WorkshopData workshopData = workshops.get(0).getWorkshopData();

        assertThat(workshopData).isEqualTo(optworkshop.get());


    }
}
