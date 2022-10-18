package com.example.aplforecastbot.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JoinColumn(name = "forecaster_id")
//    @JoinTable(name = "forecasts_forecasters",
//    joinColumns = @JoinColumn(name = "forecast_id"),
//    inverseJoinColumns = @JoinColumn(name = "forecaster_id"))
    private Forecaster forecaster;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Forecast forecast = (Forecast) o;
        return id != null && Objects.equals(id, forecast.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
