package com.example.aplforecastbot.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table(name = "forecaster")
public class Forecaster {

    @Id
    private Long id;
    private int startPoint =0;
    private String name;
    private int rating;
    private int points;
    private Timestamp registeredAt;


    @OneToMany(mappedBy = "forecaster")
    private List<Forecast> forecasts;

    @OneToOne(mappedBy = "forecaster")
    private MessageIds messageIds;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

}
