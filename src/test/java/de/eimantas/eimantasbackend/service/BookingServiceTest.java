package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.repo.BookingRepository;
import org.junit.Before;
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

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class BookingServiceTest {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private MockMvc mockMvc;


  @Inject
  private BookingService bookingService;
  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;


  private KeycloakAuthenticationToken mockPrincipal;
  private ArrayList<Booking> bookings;


  @Before
  public void setup() throws Exception {
    // auth stuff
    mockPrincipal = Mockito.mock(KeycloakAuthenticationToken.class);
    Mockito.when(mockPrincipal.getName()).thenReturn("test");

    KeycloakPrincipal keyPrincipal = Mockito.mock(KeycloakPrincipal.class);
    RefreshableKeycloakSecurityContext ctx = Mockito.mock(RefreshableKeycloakSecurityContext.class);

    AccessToken token = Mockito.mock(AccessToken.class);
    Mockito.when(token.getSubject()).thenReturn(TestUtils.USER_ID);
    Mockito.when(ctx.getToken()).thenReturn(token);
    Mockito.when(keyPrincipal.getKeycloakSecurityContext()).thenReturn(ctx);
    Mockito.when(mockPrincipal.getPrincipal()).thenReturn(keyPrincipal);


    bookings = new ArrayList<>();

    bookings.add(TestUtils.getBooking(13));
    bookings.add(TestUtils.getBooking(21));

    bookingRepository.saveAll(bookings);

  }


  @Test
  public void testFindAllBookings() throws Exception {

    Iterable<Booking> found = bookingService.findAll(mockPrincipal);
    assertThat(found).isNotNull();
    List<Booking> bookingsFound = StreamSupport.stream(found.spliterator(), false).collect(Collectors.toList());
    assertThat(bookingsFound).isNotNull();
    assertThat(bookingsFound.size()).isEqualTo(bookings.size());

  }

  @Test
  public void testFindById() throws Exception {

    Optional<Booking> found = bookingService.findById(bookings.get(0).getServerBookingId(), mockPrincipal);
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isNotNull();
    assertThat(found.get().getName()).isEqualTo(bookings.get(0).getName());
    assertThat(found.get().getId()).isEqualTo(bookings.get(0).getId());
    assertThat(found.get().getServerBookingId()).isEqualTo(bookings.get(0).getServerBookingId());
  }


  @Test
  public void testSaveBooking() throws Exception {
    Booking b = TestUtils.getBooking(123);
    b.setName("Saved in Test");
    Booking saved = bookingService.save(b);
    assertThat(saved).isNotNull();
    assertThat(saved.getServerBookingId()).isGreaterThan(0);

    logger.info("Saved booking " + saved.toString());
  }


}
