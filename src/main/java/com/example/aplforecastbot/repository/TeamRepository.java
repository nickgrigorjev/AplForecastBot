package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.Team;
import com.example.aplforecastbot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {

}