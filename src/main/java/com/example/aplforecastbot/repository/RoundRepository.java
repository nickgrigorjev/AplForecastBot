package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {

    @Query("select distinct b.numberOfRound from Round b where b.dateTimeStartRound in (select max(b.dateTimeStartRound) from Round b where b.dateTimeStartRound<:currentTime)")
    byte findCurrentRound(@Param("currentTime") Timestamp currentTime);

    Round findByNumberOfRound(byte numberOfRound);

}
