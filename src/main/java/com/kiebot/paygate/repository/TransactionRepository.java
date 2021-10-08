package com.kiebot.paygate.repository;

import com.kiebot.paygate.domain.Transaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Transaction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query(
        value = "select * from transaction t where t.order_id = :orderId order by created_date desc limit 1",
        nativeQuery = true
    )
    Transaction getTransactionByOrder(Long orderId);
}
