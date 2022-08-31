package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Event;
import com.example.aplforecastbot.entities.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event,Long> {
    @Query("select b from Event b where b.matchResult.idGameOnSoccer365ru=:id")
    List<Event> findAllByIdGameOnSoccer365ru(@Param("id") long id);

    List<Event> findAllByMatchResult(MatchResult matchResult);


//    @Query("delete from Event b where b.matchResult=:matchResult ")
    @Transactional
    void deleteAllByMatchResult(MatchResult matchResult);
}
