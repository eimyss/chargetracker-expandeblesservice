package de.eimantas.eimantasbackend.controller;

import de.eimantas.eimantasbackend.TestUtils;
import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.entities.dto.BookingDTO;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookingControllerTest {

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private MockMvc mockMvc;

  @SuppressWarnings("rawtypes")
  private HttpMessageConverter mappingJackson2HttpMessageConverter;


  private List<Booking> bookingsList = new ArrayList<>();

  @Autowired
  private BookingRepository bookingRepository;


  @Autowired
  private WebApplicationContext webApplicationContext;
  private KeycloakAuthenticationToken mockPrincipal;

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
        .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

    assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();


    bookingRepository.deleteAll();


    Booking b1 = TestUtils.getBooking(3);
    this.bookingsList.add(b1);

    Booking b2 = TestUtils.getBooking(4);
    this.bookingsList.add(b2);

    bookingRepository.saveAll(bookingsList);

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
  public void readSingleBooking() throws Exception {

    mockMvc.perform(get("/booking/get/" + this.bookingsList.get(0).getServerBookingId()).principal(mockPrincipal)).andExpect(status().isOk())
        .andDo(MockMvcResultHandlers.print())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.id", is(((Long)this.bookingsList.get(0).getId()).intValue())))
        .andExpect(jsonPath("$.serverBookingId", is(((Long) this.bookingsList.get(0).getServerBookingId()).intValue())))
        .andExpect(jsonPath("$.name", is("Booking")));
  }

  @Test
  public void readBookings() throws Exception {

    // given(controller.principal).willReturn(allEmployees);
    mockMvc.perform(get("/booking/get/all").principal(mockPrincipal)).andExpect(status().isOk())
        .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].serverBookingId", is(((Long) this.bookingsList.get(0).getServerBookingId()).intValue())))
        .andExpect(jsonPath("$[0].name", is("Booking")))
        .andExpect(jsonPath("$[1].id", is(((Long)this.bookingsList.get(1).getId()).intValue())));
  }

  @Test
  @Transactional
  public void createBooking() throws Exception {

    BookingDTO exp = TestUtils.getBookingDto(3);
    String bookmarkJson = json(exp);

    this.mockMvc.perform(post("/booking/save").principal(mockPrincipal).contentType(contentType).content(bookmarkJson))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk())
        .andDo(MockMvcResultHandlers.print()).andExpect(content().contentType(contentType));
  }

  @SuppressWarnings("unchecked")
  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }

}
