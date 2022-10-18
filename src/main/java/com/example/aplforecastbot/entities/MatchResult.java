package com.example.aplforecastbot.entities;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@DynamicUpdate
@Getter
@Setter
@ToString
@RequiredArgsConstructor
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
    private String shortHt;
    private String shortGt;
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
    @ToString.Exclude
    private List<Event> events;


    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    @OneToMany(mappedBy = "matchResult")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @LazyCollection(LazyCollectionOption.FALSE)
    @ToString.Exclude
    private List<Forecast> forecasts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MatchResult that = (MatchResult) o;
        return idGameOnSoccer365ru != null && Objects.equals(idGameOnSoccer365ru, that.idGameOnSoccer365ru);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
