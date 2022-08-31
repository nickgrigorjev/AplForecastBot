package com.example.aplforecastbot.entities;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@DynamicUpdate
@Entity
@Data
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


}
