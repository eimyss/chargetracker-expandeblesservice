package de.eimantas.eimantasbackend.service;

import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.entities.converter.EntitiesConverter;
import de.eimantas.eimantasbackend.processing.OverviewProcessor;
import de.eimantas.eimantasbackend.repo.BookingRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;


@Service
public class BookingService {

  @Inject
  SecurityService securityService;
  @Inject
  BookingRepository bookingRepository;

  @Inject
  OverviewProcessor processor;

  @Inject
  EntitiesConverter converter;


  public Iterable<Booking> findAll() {
    return bookingRepository.findAll();
  }

  public Optional<Booking> findById(long id) {
    return bookingRepository.findById(id);
  }

  public Booking save(Booking book) {
    return bookingRepository.save(book);
  }
}
