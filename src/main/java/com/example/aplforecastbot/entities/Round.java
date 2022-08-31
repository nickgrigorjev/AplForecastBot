package com.example.aplforecastbot.entities;


import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table(name = "round")
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Long id;
    @Column(name = "number_of_round")
    private byte numberOfRound;//+
    @Column(name = "start_round")
    private Timestamp dateTimeStartRound;//+
    @Column(name ="post_time_minus30")
    private Timestamp timeMinus30;//+
    @Column(name = "post_date")
    private Timestamp dateTimePostingAllMatchesRound;//+
    @Column(name ="post_time_plus30")
    private Timestamp timePlus30;//+
    @Column(name = "start_first_match_date")
    private Timestamp dateTimeStartFirstMatchRound;//+
    @Column(name = "last_match_start_date")
    private Timestamp dateTimeStartLastMatchRound;
    @Column(name = "finish_date")
    private Timestamp dateTimeEndOfTheRound;


    @OneToMany(mappedBy = "round")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    @LazyCollection(LazyCollectionOption.FALSE)
    List<MatchResult> matchResults;

    @OneToMany(mappedBy = "round")
    private List<Forecaster> forecasters;
}
