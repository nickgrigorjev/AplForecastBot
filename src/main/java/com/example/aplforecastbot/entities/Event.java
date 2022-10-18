package com.example.aplforecastbot.entities;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Objects;

@DynamicUpdate
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_game_on_soccer365ru")
    @ToString.Exclude
    private MatchResult matchResult;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Event event = (Event) o;
        return id != null && Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
