package com.kiebot.paygate.repository;

import com.kiebot.paygate.domain.UserData;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the UserData entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {
    @Query(
        value = "select * from user_data usd where usd.user_id = :id order by created_date desc limit 1",
        nativeQuery = true
    )
    UserData getDataByUserId(int id);
}
