package de.eimantas.eimantasbackend.controller;

import de.eimantas.eimantasbackend.controller.exceptions.BadRequestException;
import de.eimantas.eimantasbackend.controller.exceptions.ErrorDesc;
import de.eimantas.eimantasbackend.controller.exceptions.NonExistingEntityException;
import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.entities.dto.BookingDTO;
import de.eimantas.eimantasbackend.messaging.BookingsSender;
import de.eimantas.eimantasbackend.service.BookingService;
import de.eimantas.eimantasbackend.service.SecurityService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/booking")
public class BookingController {
  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


  @Inject
  SecurityService securityService;

  @Inject
  BookingsSender sender;

  @Inject
  BookingService bookingService;

  @Inject
  EntitiesConverter entitiesConverter;


  @GetMapping(value = "/get/all", produces = MediaType.APPLICATION_JSON_VALUE)
  @CrossOrigin(origins = "*")
  public Collection<BookingDTO> getBookings(Principal principal) {

    Collection bookings = StreamSupport.stream(bookingService.findAll((KeycloakAuthenticationToken) principal)
        .spliterator(), false).collect(Collectors.toList());

    logger.info(String.format("returning count: %d",
        bookings.size()));

    return bookings;
  }


  @GetMapping("/get/{id}")
  @CrossOrigin(origins = "*")
  public BookingDTO getBookingById(@PathVariable long id, Principal principal) throws NonExistingEntityException {
    logger.info("get booking for id: " + id);
    Optional<Booking> bookingOptional = bookingService.findById(id, (KeycloakAuthenticationToken) principal);
    if (!bookingOptional.isPresent()) {
      logger.warn("booking with id : " + id + "is not present");
      throw new NonExistingEntityException("booking by id: '" + id + "' is null");
    }
    logger.info("booking found!");
    return entitiesConverter.getBookingDTO(bookingOptional.get());
  }


  @PostMapping("/save")
  @CrossOrigin(origins = "*")
  @Transactional
  public BookingDTO persisBooking(Principal principal, @RequestBody BookingDTO booking) throws BadRequestException, NonExistingEntityException {

    if (booking.getServerBookingId() > 0) {
      logger.warn("trying to post expense with Server ID ");
      throw new BadRequestException("cannot save a new expense with Server id!");
    }

    logger.info("creating booking: " + booking.toString());
    String userId = securityService.getUserIdFromPrincipal((KeycloakAuthenticationToken) principal);
    logger.info("setting epxense for userid: " + userId);

    Booking book = entitiesConverter.getBookingFromDTO(booking);
    book.setUserId(userId);
    Booking response = bookingService.save(book);
    logger.info("booking is saved: " + response.toString());
    logger.info("Notfiying about created booking");
    sender.notifyCreateExpense((KeycloakAuthenticationToken) principal, response);
    logger.debug("returning booked dto");
    return entitiesConverter.getBookingDTO(response);

  }


  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  ErrorDesc handleException(BadRequestException e) {
    return new ErrorDesc(e.getMessage());
  }

  @ExceptionHandler(NonExistingEntityException.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public @ResponseBody
  ErrorDesc handleException(NonExistingEntityException e) {
    return new ErrorDesc(e.getMessage());
  }


}