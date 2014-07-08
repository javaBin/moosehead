package no.java.moosehead.web;

import no.java.moosehead.api.ParticipantApi;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.api.WorkshopStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataServletTest {

    private final DataServlet servlet = new DataServlet();
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse resp = mock(HttpServletResponse.class);
    private final StringWriter jsonContent = new StringWriter();
    private final ParticipantApi participantApi = mock(ParticipantApi.class);

    @Before
    public void setUp() throws Exception {
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonContent));
        servlet.setParticipantApi(participantApi);
    }

    @Test
    public void shouldDisplayWorkshopList() throws Exception {
        when(req.getMethod()).thenReturn("GET");
        when(req.getPathInfo()).thenReturn("/workshopList");

        WorkshopInfo one = new WorkshopInfo("1", "Ws one", "desc", WorkshopStatus.FREE_SPOTS);
        WorkshopInfo two = new WorkshopInfo("2", "Ws two", "desc", WorkshopStatus.FREE_SPOTS);

        when(participantApi.workshops()).thenReturn(Arrays.asList(one, two));

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

    private void mockInputStream(String inputjson) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(inputjson.getBytes("UTF-8"));
        when(req.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }
        });
    }

    @Test
    public void shouldMakeReservation() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        JSONObject reservationJson = new JSONObject();
        reservationJson.put("workshopid", "123");
        reservationJson.put("email", "darth@a.com");
        reservationJson.put("fullname", "Darth Vader");

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).reservation("123","darth@a.com","Darth Vader");
    }

    @Test
    public void shouldRespondWith400IfBadRequest() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        mockInputStream("I am garbage");

        servlet.service(req,resp);

        verify(participantApi,never()).reservation(anyString(),anyString(),anyString());
        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST,"Illegal json input");

    }
}
