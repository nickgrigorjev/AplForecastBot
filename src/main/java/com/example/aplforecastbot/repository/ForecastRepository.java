package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Forecast;
import com.example.aplforecastbot.entities.Forecaster;
import com.example.aplforecastbot.entities.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast,Long> {

    @Query("select distinct b from Forecast b where b.forecaster=:forecaster and b.matchResult.idGameOnSoccer365ru=:id")
    Optional<Forecast> findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(@Param("forecaster") Forecaster forecaster, @Param("id") Long id);

    @Query("select distinct b from Forecast b where b.matchResult.idGameOnSoccer365ru=:id")
    List<Forecast> findAllByMatchResult_IdGame(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecast set date_time_forecast_made=:dateTime where match_result_id = :gameId")
    int updateDateTimeForecastMade(@Param("dateTime") Timestamp dateTime, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecast set forecast_guest_team_goals=:gtGoal where match_result_id = :gameId")
    int updateForecastGuestTeamGoal(@Param("gtGoal") byte gtGoal, @Param("gameId") long gameId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecast set forecast_home_team_goals=:htGoal where match_result_id = :gameId")
    int updateForecastHomeTeamGoal(@Param("htGoal") byte htGoal, @Param("gameId") long gameId);

    Optional <Forecast> findByMatchResult(MatchResult matchResult);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecast set point=:point where match_result_id = :gameId and forecaster_id = :forecasterId")
    int updateForecastPoint(@Param("point") int point, @Param("gameId") long gameId, @Param("forecasterId") long forecasterId);

//    @Modifying
//    @Transactional
//    @Query(nativeQuery = true,value = "select point from aplforecastbot.forecast where match_result_id = :gameId and forecaster_id = :forecasterId")
//    int selectForecastPoint(@Param("gameId") long gameId, @Param("forecasterId") long forecasterId);



    Forecast findByMatchResult_IdGameOnSoccer365ruAndForecaster_Id(@Param("gameId") long gameId, @Param("forecasterId") long forecasterId);

//    @Query("update Forecast b set b.point=:point where b.forecaster=: forecaster and b.matchResult=:matchResult")
//    int updateForecastPoint(@Param("point") int point, @Param("matchResult") MatchResult matchResult, @Param("forecaster") Forecaster forecaster);

}
