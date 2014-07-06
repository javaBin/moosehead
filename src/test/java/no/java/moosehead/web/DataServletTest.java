package no.java.moosehead.web;

import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.api.WorkshopStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataServletTest {
    @Test
    public void shouldDisplayWorkshopList() throws Exception {
        DataServlet servlet = new DataServlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getMethod()).thenReturn("GET");
        when(req.getPathInfo()).thenReturn("/workshopList");
        StringWriter jsonContent = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonContent));

        ParticipantApi participantApi = mock(ParticipantApi.class);
        servlet.setParticipantApi(participantApi);

        WorkshopInfo one = new WorkshopInfo("1", "Ws one", "desc", WorkshopStatus.FREE_SPOTS);
        WorkshopInfo two = new WorkshopInfo("2", "Ws two", "desc", WorkshopStatus.FREE_SPOTS);

        when(participantApi.workshops()).thenReturn(Arrays.asList(one,two));

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).workshops();

        JSONArray jsonArray = new JSONArray(jsonContent.toString());
        assertThat(jsonArray.length()).isEqualTo(2);

        JSONObject obone = jsonArray.getJSONObject(0);
        assertThat(obone.getString("id")).isEqualTo("1");
        assertThat(obone.getString("title")).isEqualTo("Ws one");
        assertThat(obone.getString("description")).isEqualTo("desc");
        assertThat(obone.getString("status")).isEqualTo(WorkshopStatus.FREE_SPOTS.name());

        JSONObject obtwo = jsonArray.getJSONObject(1);
        assertThat(obtwo.getString("id")).isEqualTo("2");
        assertThat(obtwo.getString("title")).isEqualTo("Ws two");
        assertThat(obtwo.getString("description")).isEqualTo("desc");
        assertThat(obtwo.getString("status")).isEqualTo(WorkshopStatus.FREE_SPOTS.name());
    }
}
