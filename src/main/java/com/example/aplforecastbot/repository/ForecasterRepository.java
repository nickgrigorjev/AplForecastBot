package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Forecast;
import com.example.aplforecastbot.entities.Forecaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface ForecasterRepository extends JpaRepository<Forecaster,Long> {

    @Query("select sum(b.point) from Forecast b where b.forecaster=:forecaster")
    int getSumOfPointsById(@Param("forecaster") Forecaster forecaster);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecaster set points=:point where id=:forecaster_id")
    int updatePoints(@Param("point") int point, @Param("forecaster_id") long forecaster_id);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecaster set rating=:rating where id=:forecaster_id")
    int updateRating(@Param("rating") int rating, @Param("forecaster_id") long forecaster_id);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,value = "update aplforecastbot.forecaster set arrow=:arrow where id=:forecaster_id")
    int updateArrow(@Param("arrow") char arrow, @Param("forecaster_id") long forecaster_id);

    @Query("select sum(b.forecastHomeTeamGoals) from Forecast b where b.forecaster.id=:forecaster_id and b.matchResult.idGameOnSoccer365ru=:matchResult_id")
    int sumHomeGoals(@Param("forecaster_id") Long forecaster_id,@Param("matchResult_id") Long matchResult_id);

}
