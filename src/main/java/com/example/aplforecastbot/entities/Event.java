package com.example.aplforecastbot.entities;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@DynamicUpdate
@Entity
@Data
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String htEvent;
    private String gtEvent;
    private String minEvent;
    private String eventDate;
    private String eventStatus;

    @ManyToOne
    @JoinColumn(name = "id_game_on_soccer365ru")
    private MatchResult matchResult;

}
