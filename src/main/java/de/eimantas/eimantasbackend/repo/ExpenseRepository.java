package de.eimantas.eimantasbackend.repo;

import de.eimantas.eimantasbackend.entities.Expense;
import de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface ExpenseRepository extends CrudRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

  //@Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.user.username = ?#{ principal?.username }")
  @Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.userId = :userId")
  public Stream<Expense> findExpensesInPeriod(@Param("startDate") Instant startDate,
                                              @Param("endDate") Instant endDate,
                                              @Param("userId") String userId);

  //@Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.account.id = :accountId and e.user.username = ?#{ principal?.username }")
  @Query("select e from expenses e where e.createDate  BETWEEN :startDate and :endDate and e.accountId = :accountId and e.userId = :userId ")
  public Stream<Expense> findExpensesInPeriodForAccount(@Param("accountId") long accountId, @Param("startDate") Instant startDate,
                                                        @Param("endDate") Instant endDate,
                                                        @Param("userId") String userId);

  @Query("select e from expenses e where e.createDate between :startDate and :endDate and e.userId = :userId")
  public List<Expense> findExpensesInPeriodGlobaly(@Param("startDate") Instant startDate,
                                                   @Param("endDate") Instant endDate,
                                                   @Param("userId") String userId);

  public List<Expense> findByCreateDateBetweenAndUserId(Instant startDate, Instant endDate, String userId);

  public Collection<Expense> findByUserId(String userId);

  public int countByAccountIdAndUserId(long accountID, String userId);

  // I wanted to check both ways
  @Query("SELECT COUNT(e) FROM expenses e WHERE e.accountId=:accountId and e.userId = :userId")
  public int selectCountForAccount(@Param("accountId") long accountId, @Param("userId") String userId);


  @Query("SELECT " +
      " new de.eimantas.eimantasbackend.entities.dto.CategoryAndCountOverview(e.category, count(e)) " +
      " FROM expenses e WHERE e.accountId=:accountId and e.userId = :userId group by e.category")
  List<CategoryAndCountOverview> findCategoryAndCount(@Param("accountId") long accountId, @Param("userId") String userId);

  Collection<Expense> findByAccountIdAndUserId(long id, String userId);

  Expense findByIdAndUserId(long id, String userIdFromPrincipal);

  @Query("SELECT COUNT(e) FROM expenses e WHERE e.userId = :userId")
  int selectCountByUserId(@Param("userId") String userId);
}