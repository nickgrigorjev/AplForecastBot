package com.example.aplforecastbot.entities;

import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "forecast")
public class Forecast {
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private byte forecastHomeTeamGoals;
    private byte forecastGuestTeamGoals;
    private Timestamp dateTimeForecastMade;
    private int point;

    @ManyToOne
    @JoinColumn(name = "match_result_id")
    private MatchResult matchResult;

    @ManyToOne
    @JoinColumn(name = "forecaster_id")
//    @JoinTable(name = "forecasts_forecasters",
//    joinColumns = @JoinColumn(name = "forecast_id"),
//    inverseJoinColumns = @JoinColumn(name = "forecaster_id"))
    private Forecaster forecaster;


}
