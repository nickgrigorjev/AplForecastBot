package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Forecaster;
import com.example.aplforecastbot.entities.MessageIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MessageIdsRepository extends JpaRepository<MessageIds,Long> {
    Optional<MessageIds> findByForecaster_Id(Long forecaster_id);
    Optional<MessageIds> findByForecastMsgId(Long forecasterMsg_id);
    Optional<MessageIds> findByForecastNumbersMsgId(Long forecasterMsg_id);
    @Query("select max(m.forecastMsgId), max(m.forecastNumbersMsgId) from MessageIds m")
    int findMaxId();
}
