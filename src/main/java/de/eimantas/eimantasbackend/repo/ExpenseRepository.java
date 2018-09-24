package de.eimantas.eimantasbackend.repo;

import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@RepositoryRestResource
@CrossOrigin(origins = "*")
public interface ExpenseRepository extends CrudRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

	public Collection<Expense> findByAccountId(long accountId);

	//@Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.user.username = ?#{ principal?.username }")
	@Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate ")
	public Stream<Expense> findExpensesInPeriod(@Param("startDate") Instant startDate,
			@Param("endDate") Instant endDate);

	//@Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.account.id = :accountId and e.user.username = ?#{ principal?.username }")
    @Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.accountId = :accountId ")
    public Stream<Expense> findExpensesInPeriodForAccount(@Param("accountId") long accountId, @Param("startDate") Instant startDate,
                                                @Param("endDate") Instant endDate);

	@Query("select e from expenses e where e.createDate between :startDate and :endDate")
	public List<Expense> findExpensesInPeriodGlobaly(@Param("startDate") Instant startDate,
			@Param("endDate") Instant endDate);

	public List<Expense> findByCreateDateBetween(Instant startDate, Instant endDate);

	public Collection<Expense> findByUserId(Long userId);


    public int countByAccountId(long accountID);

    // I wanted to check both ways
	@Query("SELECT COUNT(e) FROM expenses e WHERE e.accountId=:accountId")
    public int selectCountForAccount(@Param("accountId") long accountId);


    @Query("SELECT " +
            " new de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview(e.category, count(e)) " +
            " FROM expenses e WHERE e.accountId=:accountId group by e.category")
    List<CategoryAndCountOverview> findCategoryAndCount(@Param("accountId") long accountId);
}