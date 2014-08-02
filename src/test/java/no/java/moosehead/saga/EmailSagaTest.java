package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class EmailSagaTest {
    @Test
    public void shouldSendEmailOnConfirm() throws Exception {
        EmailSender emailSender = mock(EmailSender.class);
        SystemSetup setup = mock(SystemSetup.class);
        when(setup.emailSender()).thenReturn(emailSender);
        SystemSetup.setSetup(setup);

        EmailSaga emailSaga = new EmailSaga();
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(),2L,"darth@a.com","Darth","one"));

        verify(emailSender).sendEmailConfirmation("darth@a.com","2");
        verifyNoMoreInteractions(emailSender);
    }


}
