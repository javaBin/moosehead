package no.java.moosehead.web;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.java.moosehead.api.*;
import no.java.moosehead.commands.AuthorEnum;
import no.java.moosehead.commands.WorkshopTypeEnum;
import org.jsonbuddy.JsonArray;
import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataServletTest {

    private final DataServlet servlet = new DataServlet();
    private final HttpServletRequest req = mock(HttpServletRequest.class);
    private final HttpServletResponse resp = mock(HttpServletResponse.class);
    private final StringWriter jsonContent = new StringWriter();
    private final ParticipantApi participantApi = mock(ParticipantApi.class);
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        when(resp.getWriter()).thenReturn(new PrintWriter(jsonContent));
        servlet.setParticipantApi(participantApi);

        session = mock(HttpSession.class);
        when(session.getAttribute("captchaAnswer")).thenReturn("123");
        when(req.getSession()).thenReturn(session);
    }

    @Test
    public void shouldDisplayWorkshopList() throws Exception {
        when(req.getMethod()).thenReturn("GET");
        when(req.getPathInfo()).thenReturn("/workshopList");

        WorkshopInfo one = new WorkshopInfo("1", "Ws one", "desc", null, WorkshopStatus.FREE_SPOTS, WorkshopTypeEnum.NORMAL_WORKSHOP);
        WorkshopInfo two = new WorkshopInfo("2", "Ws two", "desc", null, WorkshopStatus.FREE_SPOTS, WorkshopTypeEnum.NORMAL_WORKSHOP);

        when(participantApi.workshops()).thenReturn(Arrays.asList(one, two));

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).workshops();

        JsonArray jsonArray = (JsonArray) JsonParser.parse(jsonContent.toString());
        assertThat(jsonArray.size()).isEqualTo(2);

        JsonObject obone = jsonArray.get(0, JsonObject.class);
        assertThat(obone.requiredString("id")).isEqualTo("1");
        assertThat(obone.requiredString("title")).isEqualTo("Ws one");
        assertThat(obone.requiredString("description")).isEqualTo("desc");
        assertThat(obone.requiredString("status")).isEqualTo(WorkshopStatus.FREE_SPOTS.name());

        JsonObject obtwo = jsonArray.get(1, JsonObject.class);
        assertThat(obtwo.requiredString("id")).isEqualTo("2");
        assertThat(obtwo.requiredString("title")).isEqualTo("Ws two");
        assertThat(obtwo.requiredString("description")).isEqualTo("desc");
        assertThat(obtwo.requiredString("status")).isEqualTo(WorkshopStatus.FREE_SPOTS.name());
    }

    @Test
    public void shouldDisplayMyWorkshops() throws Exception {
        when(req.getMethod()).thenReturn("GET");
        when(req.getPathInfo()).thenReturn("/myReservations");
        when(req.getParameter("email")).thenReturn("a@a.com");

        when(participantApi.myReservations(anyString())).thenReturn(Arrays.asList(
           new ParticipantReservation("1","a@a.com","One",ParticipantReservationStatus.HAS_SPACE,10, Optional.empty()),
           new ParticipantReservation("2","a@a.com","Two",ParticipantReservationStatus.HAS_SPACE,3, Optional.empty())
        ));

        servlet.service(req,resp);

        verify(participantApi).myReservations("a@a.com");
        verify(resp).setContentType("text/json");
        JsonArray jsonArray = (JsonArray) JsonParser.parse(jsonContent.toString());
        assertThat(jsonArray.size()).isEqualTo(2);

    }

    private void mockInputStream(String inputjson) throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(inputjson.getBytes("UTF-8"));
        when(req.getInputStream()).thenReturn(new ServletInputStream() {


            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        });
    }

    @Test
    public void shouldMakeReservation() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        JsonObject reservationJson = JsonFactory.jsonObject();
        reservationJson.withValue("workshopid", "123");
        reservationJson.withValue("email", "darth@a.com");
        reservationJson.withValue("fullname", "Darth Vader");
        reservationJson.withValue("numReservations", "1");
        reservationJson.withValue("captcha", "123");

        when(participantApi.reservation(anyString(), anyString(), anyString(),any(AuthorEnum.class), any(Optional.class), anyInt())).thenReturn(ParticipantActionResult.ok());

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).reservation("123", "darth@a.com", "Darth Vader", AuthorEnum.USER, Optional.empty(), 1);

        JsonObject jsonObject = (JsonObject) JsonParser.parse(jsonContent.toString());
        assertThat(jsonObject.requiredString("status")).isEqualTo(ParticipantActionResult.Status.OK.name());

    }

    @Test
    public void shouldMakeReservationWithGoogleMail() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        JsonObject reservationJson = JsonFactory.jsonObject();
        reservationJson.withValue("workshopid", "123");
        reservationJson.withValue("email", "darth@a.com");
        reservationJson.withValue("fullname", "Darth Vader");
        reservationJson.withValue("captcha", "123");
        reservationJson.withValue("numReservations", "1");

        ObjectNode googleNode = JsonNodeFactory.instance.objectNode();
        googleNode.put("email","darth@a.com");
        googleNode.put("id","435");
        googleNode.put("name", "Darth Vader");

        when(session.getAttribute("user")).thenReturn(googleNode);


        when(participantApi.reservation(anyString(), anyString(), anyString(), any(AuthorEnum.class), any(Optional.class), anyInt())).thenReturn(ParticipantActionResult.ok());

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).reservation("123", "darth@a.com", "Darth Vader", AuthorEnum.USER, Optional.of("darth@a.com"), 1);

        JsonObject jsonObject = (JsonObject) JsonParser.parse(jsonContent.toString());
        assertThat(jsonObject.requiredString("status")).isEqualTo(ParticipantActionResult.Status.OK.name());

    }

    @Test
    public void shouldReturnErrorWhenWrongCaptcha() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        JsonObject reservationJson = new JsonObject();
        reservationJson.withValue("workshopid", "123");
        reservationJson.withValue("email", "darth@a.com");
        reservationJson.withValue("fullname", "Darth Vader");
        reservationJson.withValue("captcha", "456");
        reservationJson.withValue("numReservations", "1");

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verifyNoMoreInteractions(participantApi);

        JsonObject jsonObject = (JsonObject) JsonParser.parse(jsonContent.toString());
        assertThat(jsonObject.requiredString("status")).isEqualTo(ParticipantActionResult.Status.WRONG_CAPTCHA.name());
    }

    @Test
    public void shouldMakeCancellation() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/cancel");

        JsonObject reservationJson = JsonFactory.jsonObject();
        reservationJson.withValue("token", "123");
        reservationJson.withValue("email", "darth@a.com");

        when(participantApi.cancellation(anyString(), any(AuthorEnum.class))).thenReturn(ParticipantActionResult.ok());

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).cancellation("123", AuthorEnum.USER);

        JsonObject jsonObject = (JsonObject) JsonParser.parse(jsonContent.toString());
        assertThat(jsonObject.requiredString("status")).isEqualTo(ParticipantActionResult.Status.OK.name());
    }

    @Test
    public void shouldConfirmEmail() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/confirmEmail");

        JsonObject reservationJson = JsonFactory.jsonObject();
        reservationJson.withValue("token", "123456-123456");

        when(participantApi.confirmEmail(anyString())).thenReturn(ParticipantActionResult.ok());

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(resp).setContentType("text/json");
        verify(participantApi).confirmEmail("123456-123456");

        JsonObject jsonObject = (JsonObject) JsonParser.parse(jsonContent.toString());
        assertThat(jsonObject.requiredString("status")).isEqualTo(ParticipantActionResult.Status.OK.name());
    }

    @Test
    public void shouldRespondWith400IfBadRequest() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        mockInputStream("I am garbage");

        servlet.service(req, resp);

        verify(participantApi, never()).reservation(anyString(), anyString(), anyString(), any(AuthorEnum.class), any(Optional.class), anyInt());
        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal json input");
    }

    @Test
    public void shouldRespondWith400WhenIllegalCharacters() throws Exception {
        when(req.getMethod()).thenReturn("POST");
        when(req.getPathInfo()).thenReturn("/reserve");

        JsonObject reservationJson = JsonFactory.jsonObject();
        reservationJson.withValue("workshopid", "123");
        reservationJson.withValue("email", "darth@a.com");
        reservationJson.withValue("fullname", "Darth <script type='text/javascript'>alert('noe')</script>Vader");
        reservationJson.withValue("captcha", "123");
        reservationJson.withValue("numReservations", "1");

        mockInputStream(reservationJson.toString());

        servlet.service(req, resp);

        verify(participantApi,never()).reservation(anyString(), anyString(), anyString(), any(AuthorEnum.class), any(Optional.class), anyInt());
        JsonObject jsonObject = (JsonObject) JsonParser.parse(jsonContent.toString());
        assertThat(jsonObject.requiredString("status")).isEqualTo(ParticipantActionResult.Status.ERROR.name());
        assertThat(jsonObject.requiredString("message")).isEqualTo("Name and email must be present without spesial characters");
    }


}
