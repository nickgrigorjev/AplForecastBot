package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult,Long> {
//    @Query(value = "select b from MatchResult b where b.dateOfMatch < CAST(:date as timestamp without time zone)")
//    @Query("select b.numberOfRound from MatchResult b where b.dateOfMatch>:somedata in(select max(b.dateOfMatch) from MatchResult b)")
    @Query("select distinct b.numberOfRound from MatchResult b where b.dateOfMatch in (select max(b.dateOfMatch) from MatchResult b where b.dateOfMatch<:someDate)")
    byte findCurrentRound(@Param("someDate")Timestamp someDate);

    @Query("select b from MatchResult b where b.numberOfRound=:round")
    List<MatchResult> findAllByNumberOfRound(@Param("round") byte round);

    @Query("select distinct b.dateOfMatch from MatchResult b where b.dateOfMatch in (select min(b.dateOfMatch) from MatchResult b where b.numberOfRound=:round)")
    Timestamp findFirstDateOfMatchOfTheRoundByNumberOfRound(@Param("round") byte round);

    @Query("select distinct b.dateOfMatch from MatchResult b where b.dateOfMatch in (select max(b.dateOfMatch) from MatchResult b where b.numberOfRound=:round)")
    Timestamp findLastDateOfMatchOfTheRoundByNumberOfRound(@Param("round") byte round);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.match_results set date_of_match=:dateTimeOfMatch where id_game_on_soccer365ru = :gameId")
    int updateDateOfMatch(@Param("dateTimeOfMatch") Timestamp dateTimeOfMatch, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.match_results set round_id=:numberOfRound where id_game_on_soccer365ru = :gameId")
    int updateRoundId(@Param("numberOfRound") byte numberOfRound, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.match_results set status=:status where id_game_on_soccer365ru = :gameId")
    int updateStatus(@Param("status") byte status, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.match_results set short_ht=:short_ht where id_game_on_soccer365ru = :gameId")
    int updateShortHt(@Param("short_ht") String short_ht, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.match_results set short_gt=:short_gt where id_game_on_soccer365ru = :gameId")
    int updateShortGt(@Param("short_gt") String short_gt, @Param("gameId") long gameId);
}
