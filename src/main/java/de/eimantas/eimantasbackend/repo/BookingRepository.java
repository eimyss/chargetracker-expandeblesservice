package de.eimantas.eimantasbackend.repo;

import de.eimantas.eimantasbackend.entities.Booking;
import de.eimantas.eimantasbackend.entities.Expense;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

public interface BookingRepository extends CrudRepository<Booking, Long>, JpaSpecificationExecutor<Expense> {

}