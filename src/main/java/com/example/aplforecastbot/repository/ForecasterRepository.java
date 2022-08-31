package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Forecast;
import com.example.aplforecastbot.entities.Forecaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForecasterRepository extends JpaRepository<Forecaster,Long> {

    @Query("select sum(b.point) from Forecast b where b.forecaster=:forecaster")
    int getSumOfPointsById(@Param("forecaster") Forecaster forecaster);
}
