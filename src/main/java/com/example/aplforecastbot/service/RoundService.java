package com.example.aplforecastbot.service;

import com.example.aplforecastbot.entities.MatchResult;
import com.example.aplforecastbot.entities.Round;
import com.example.aplforecastbot.repository.MatchResultRepository;
import com.example.aplforecastbot.repository.RoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class RoundService {
    MatchResultRepository matchResultRepository;
    RoundRepository roundRepository;

    private ParserService parserService;
    @Autowired
    public void setParserService(ParserService parserService) {
        this.parserService = parserService;
    }

    @Autowired
    public void setRoundRepository(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    @Autowired
    public void setMatchResultRepository(MatchResultRepository matchResultRepository) {
        this.matchResultRepository = matchResultRepository;
    }

//    @Scheduled(fixedDelay = 800000)
    void checkAndUpdateDatesRound() throws IOException {
        Timestamp currentDate = new Timestamp(System.currentTimeMillis());
//
        byte currentNumberOfRound = matchResultRepository.findCurrentRound(currentDate);
//        byte currentNumberOfRound = 1;
        for(byte i=currentNumberOfRound;i<39;i++){
            System.out.println(i);
            Round round = new Round();
            Timestamp dateTimeStartFirstMatchRound = matchResultRepository.findFirstDateOfMatchOfTheRoundByNumberOfRound(i);
            Timestamp dateTimeStartLastMatchRound = matchResultRepository.findLastDateOfMatchOfTheRoundByNumberOfRound(i);

            Timestamp dateTimePostingAllMatchesRound = new Timestamp(dateTimeStartFirstMatchRound.getTime());
            dateTimePostingAllMatchesRound.setHours(dateTimeStartFirstMatchRound.getHours()-3);

            Timestamp timeMinus30 = new Timestamp(dateTimePostingAllMatchesRound.getTime());
            timeMinus30.setSeconds(timeMinus30.getSeconds()-30);

            Timestamp timePlus30 = new Timestamp(dateTimePostingAllMatchesRound.getTime());
            timePlus30.setSeconds(timePlus30.getSeconds()+30);

            Timestamp dateTimeEndOfTheRound = new Timestamp(dateTimeStartLastMatchRound.getTime());
            dateTimeEndOfTheRound.setHours(dateTimeEndOfTheRound.getHours()+3);

            Timestamp dateTimeStartRound = new Timestamp(dateTimeStartFirstMatchRound.getTime());
            dateTimeStartRound.setHours(0);
            dateTimeStartRound.setMinutes(0);

            round.setNumberOfRound(i);
            round.setId((long) i);
            round.setMatchResults(matchResultRepository.findAllByNumberOfRound(i));
            round.setDateTimeStartRound(dateTimeStartRound);
            round.setDateTimePostingAllMatchesRound(dateTimePostingAllMatchesRound);
            round.setTimeMinus30(timeMinus30);
            round.setTimePlus30(timePlus30);
            round.setDateTimeEndOfTheRound(dateTimeEndOfTheRound);
            round.setDateTimeStartFirstMatchRound(dateTimeStartFirstMatchRound);
            round.setDateTimeStartLastMatchRound(dateTimeStartLastMatchRound);
            roundRepository.saveAndFlush(round);
        }
//        List<MatchResult> matchResultList = matchResultRepository.findAll();
//        System.out.println(matchResultList.size());
//        for(MatchResult m:matchResultList){
//            System.out.println(m.getIdGameOnSoccer365ru()+" - "+ m.getNumberOfRound());
//            parserService.parseAndWriteMatchResultsToDatabase(m.getIdGameOnSoccer365ru());
//        }
//        parserService.parseAndWriteMatchResultsToDatabase(1736384);
//            matchResultRepository.findAllByNumberOfRound(currentNumberOfRound).stream().forEach(s-> {
//                try {
//                    System.out.println("id Match - "+s.getIdGameOnSoccer365ru());
//                    parserService.parseAndWriteMatchResultsToDatabase(s.getIdGameOnSoccer365ru());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });

//



        System.out.println("Завершено");
        System.out.println(System.currentTimeMillis()-currentDate.getTime());

    }
}
