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
    @Query("select usd from UserData usd where usd.id = :id")
    UserData getDataById(Long id);
}
