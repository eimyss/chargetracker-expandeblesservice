package de.eimantas.eimantasbackend.messaging;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.client.SecurityUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class BookingsMessagingTest {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private MockMvc mockMvc;

  @Autowired
  BookingsSender bookingsSender;

  @Autowired
  private WebApplicationContext webApplicationContext;
  private KeycloakAuthenticationToken mockPrincipal;


  @Before
  public void setup() throws Exception {


    mockPrincipal = Mockito.mock(KeycloakAuthenticationToken.class);
    Mockito.when(mockPrincipal.getName()).thenReturn("test");

    KeycloakPrincipal keyPrincipal = Mockito.mock(KeycloakPrincipal.class);
    RefreshableKeycloakSecurityContext ctx = Mockito.mock(RefreshableKeycloakSecurityContext.class);

    AccessToken token = Mockito.mock(AccessToken.class);
    Mockito.when(token.getSubject()).thenReturn(TestUtils.USER_ID);
    Mockito.when(ctx.getToken()).thenReturn(token);
    Mockito.when(keyPrincipal.getKeycloakSecurityContext()).thenReturn(ctx);
    Mockito.when(mockPrincipal.getPrincipal()).thenReturn(keyPrincipal);

  }


  @Test
  @Ignore
  public void testQueue() throws Exception {
    String token = SecurityUtils.getOnlyToken();
    bookingsSender.notifyCreateExpense(mockPrincipal, TestUtils.getBooking(2));
  }


}
