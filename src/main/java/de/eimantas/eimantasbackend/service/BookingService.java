package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.repo.BookingRepository;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;


@Service
public class BookingService {

  @Inject
  SecurityService securityService;
  @Inject
  BookingRepository bookingRepository;


  public Iterable<Booking> findAll(KeycloakAuthenticationToken authentication) {
    return bookingRepository.findByUserId(securityService.getUserIdFromPrincipal(authentication));
  }

  public Optional<Booking> findById(long id, KeycloakAuthenticationToken authentication) {
    return bookingRepository.findByServerBookingIdAndUserId(id, securityService.getUserIdFromPrincipal(authentication));
  }

  public Booking save(Booking book) {
    return bookingRepository.save(book);
  }
}
