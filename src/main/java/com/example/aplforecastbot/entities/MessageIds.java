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
@RequiredArgsConstructor
@Table(name = "MessageIds")
public class MessageIds {
    @Id
    private Long id;
    private int forecastMsgId;
    private int forecastNumbersMsgId;
    private int postMatchResultsMsgId;

    @OneToOne
    @JoinColumn(name = "forecaster_id")
    private Forecaster forecaster;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MessageIds that = (MessageIds) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
