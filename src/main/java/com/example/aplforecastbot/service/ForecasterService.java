package com.example.aplforecastbot.service;

import com.example.aplforecastbot.entities.Forecast;
import com.example.aplforecastbot.entities.Forecaster;
import com.example.aplforecastbot.entities.MatchResult;
import com.example.aplforecastbot.repository.ForecastRepository;
import com.example.aplforecastbot.repository.ForecasterRepository;
import com.example.aplforecastbot.repository.MatchResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ForecasterService {
    private MatchResultRepository matchResultRepository;
    private ForecastRepository forecastRepository;
    private ForecasterRepository forecasterRepository;

    @Autowired
    public void setForecasterRepository(ForecasterRepository forecasterRepository) {
        this.forecasterRepository = forecasterRepository;
    }

    @Autowired
    public void setForecastRepository(ForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }

    @Autowired
    public void setMatchResultRepository(MatchResultRepository matchResultRepository) {
        this.matchResultRepository = matchResultRepository;
    }

    public void setRating(Long idGame){
        List<Forecaster> forecasters = forecasterRepository.findAll().stream().
                sorted(Comparator.comparing(Forecaster::getName)).
                sorted((o1, o2) -> o2.getPoints()- o1.getPoints()).collect(Collectors.toList());

        for(int i=0;i<forecasters.size();i++){
            forecasterRepository.updateRating(i+1,forecasters.get(i).getId());
        }
    }
    public void setPoint(Long idGame){
        List<Forecaster> forecasters = forecasterRepository.findAll();
        forecasters.forEach(fk->{
            forecasterRepository.updatePoints(forecastRepository.sumPoints(fk.getId()),fk.getId());
        });
    }
    public List<Forecaster> checkForecasts(byte numberOfRound){
        MatchResult match = matchResultRepository.findAllByNumberOfRound(numberOfRound).get(0);
        List<Forecast> forecasts = forecastRepository.findByMatchResult(match.getIdGameOnSoccer365ru()).get();
//        List<Long> idForecastersMadeForecast =forecasts.stream().map(Forecast::getForecaster).map(Forecaster::getId).collect(Collectors.toList());
        List<Long> idForecastersMadeForecastInterim =forecasts.stream().map(Forecast::getForecaster).map(Forecaster::getId).collect(Collectors.toList());
        List<Long> idForecastersMadeForecast =forecasts.stream().map(Forecast::getForecaster).map(Forecaster::getId).collect(Collectors.toList());
        List<MatchResult> matchResultsOfRound = matchResultRepository.findAllByNumberOfRound(numberOfRound);


        for(Long f:idForecastersMadeForecastInterim){
            int sum = 0;
            for(MatchResult m:matchResultsOfRound){
                sum += forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(
                        forecasterRepository.findById(f).get(),m.getIdGameOnSoccer365ru()).get().getForecastHomeTeamGoals();
                sum += forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(
                        forecasterRepository.findById(f).get(),m.getIdGameOnSoccer365ru()).get().getForecastGuestTeamGoals();

            }
//            sum+=forecasterRepository.findById(f).get().getForecasts().stream().mapToInt(Forecast::getForecastHomeTeamGoals).sum();
//            sum+=forecasterRepository.findById(f).get().getForecasts().stream().mapToInt(Forecast::getForecastGuestTeamGoals).sum();
            System.out.println("Прогнозист " + f + " и его сумма прогнозируемых голов за тур = " + sum);
            if(sum<14){
                idForecastersMadeForecast.remove(f);
            }
        }
        System.out.println("Проверка! - "+idForecastersMadeForecast);
        List<Long> idForecastersDidNotMadeForecast = forecasterRepository.findAll().stream().map(Forecaster::getId).collect(Collectors.toList());
        List<Forecaster> forecastersDidNotMadeForecast = new ArrayList<>();

        if(forecasts.isEmpty()){
            return forecasterRepository.findAll();
        }
        else {
            for(int i=0;i<idForecastersMadeForecast.size();i++){
                idForecastersDidNotMadeForecast.remove(idForecastersMadeForecast.get(i));
            }

            for(Long l:idForecastersDidNotMadeForecast){
                forecastersDidNotMadeForecast.add(forecasterRepository.findById(l).get());
            }
            return forecastersDidNotMadeForecast;
        }

    }

    public int calculateMaxLengthForecasterName(){
        int length = 0;
        List<Integer> lengthOfForecasterName = forecasterRepository.findAll().stream().map(Forecaster::getName).map(String::length).collect(Collectors.toList());
        length = Collections.max(lengthOfForecasterName);
        return length;
    }
}
