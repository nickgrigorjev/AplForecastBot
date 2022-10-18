package com.example.aplforecastbot.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "forecaster")
public class Forecaster {

    @Id
    private Long id;
    private int startPoint =0;
    private String name;
    private int rating;
    private int points;
    private Timestamp registeredAt;
    private char arrow;


    @OneToMany(fetch = FetchType.EAGER,mappedBy = "forecaster",cascade = CascadeType.ALL)
    private List<Forecast> forecasts;

    @OneToOne(mappedBy = "forecaster")
    private MessageIds messageIds;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Forecaster that = (Forecaster) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
