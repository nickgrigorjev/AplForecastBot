package com.example.aplforecastbot.controllers;

import com.example.aplforecastbot.service.MatchResultService;
import com.example.aplforecastbot.service.RoundService;
import org.springframework.beans.factory.annotation.Autowired;

public class MatchResultController {
    MatchResultService matchResultService;
    RoundService roundService;
    @Autowired
    public void setMatchResultService(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }
    @Autowired
    public void setRoundService(RoundService roundService) {
        this.roundService = roundService;
    }


}
