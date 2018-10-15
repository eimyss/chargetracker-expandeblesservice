package de.eimantas.eimantasbackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "${feign.client.config.service.booking}", configuration = ClientConfig.class)
@RequestMapping(value = "/bookings")
public interface BookingsClient {

  @GetMapping("/get/{id}")
  ResponseEntity<?> getBookingById(@PathVariable(name = "id") long id);

}
