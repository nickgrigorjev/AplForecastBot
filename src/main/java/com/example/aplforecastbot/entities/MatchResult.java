package com.example.aplforecastbot.entities;

import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@DynamicUpdate
@Data
@Entity()
@Table(name = "matchResults")
public class MatchResult {
    @Id
    @Column(name = "id_game_on_soccer365ru")
    private Long idGameOnSoccer365ru;
    private String status;
    private byte numberOfRound;
    private Timestamp dateOfMatch;
    private String ht;
    private String gt;
    private byte htGoals;
    private byte gtGoals;
    private byte htYellowCards;
    private byte gtYellowCards;
    private byte htRedCards;
    private byte gtRedCards;
    private byte htOffsides;
    private byte gtOffsides;
    private byte htPenalties;
    private byte gtPenalties;
    private byte htOwnGoals;
    private byte gtOwnGoals;
    private byte htShots;
    private byte gtShots;
    private byte htShotsOnTarget;
    private byte gtShotsOnTarget;
    private byte htCornerKicks;
    private byte gtCornerKicks;
    private byte htFouls;
    private byte gtFouls;
    private byte htPossession;
    private byte gtPossession;
    private byte htMisPen;
    private byte gtMisPen;


    @OneToMany(mappedBy = "matchResult")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Event> events;


    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    @OneToMany(mappedBy = "matchResult")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Forecast> forecasts;

}
