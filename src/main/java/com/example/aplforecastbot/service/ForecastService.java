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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ForecastService {

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

    static int calculatePoints(byte fhtScore, byte fgtScore, byte htScore, byte gtScore){
        int point = 0;
        if(fhtScore==fgtScore&&htScore==gtScore){
            if(fhtScore==htScore){
                point=9;
            }else {point = 4;}
        }
        if(((fhtScore-fgtScore)>0&&(htScore-gtScore)>0)||(fhtScore-fgtScore)<0&&(htScore-gtScore)<0){
            point=1;
            if((fhtScore-fgtScore)==(htScore-gtScore)){
                point=4;
                if(fhtScore==htScore&&fgtScore==gtScore){
                    point=9;
                }
            }
        }

        return point;
    }

    public void setPointsToForecast(Long idGame){
        List<Forecast> forecastsByGameId = forecastRepository.findAllByMatchResult_IdGame(idGame);
        for(Forecast f:forecastsByGameId){
                Forecast forecast = new Forecast();
                forecast.setForecaster(f.getForecaster());
                forecast.setId(f.getId());
                forecast.setDateTimeForecastMade(f.getDateTimeForecastMade());
                forecast.setMatchResult(f.getMatchResult());
                forecast.setForecastHomeTeamGoals(f.getForecastHomeTeamGoals());
                forecast.setForecastGuestTeamGoals(f.getForecastGuestTeamGoals());
                int point = calculatePoints(f.getForecastHomeTeamGoals(),f.getForecastGuestTeamGoals(),f.getMatchResult().getHtGoals(),f.getMatchResult().getGtGoals());
                forecast.setPoint(point);
                forecastRepository.save(forecast);
                System.out.println("Записаны очки для " + f.getForecaster().getName()+ " " + f.getForecaster().getId());
            System.out.println("matchResultId = "+f.getMatchResult().getIdGameOnSoccer365ru()+ " Forecaster " + f.getForecaster().getName()
                    + " ForecasterId " + f.getForecaster().getId());
        }

    }

    public void setNullPointToForecast(Long idGame){
        List<Forecast> forecastsByGameId = forecastRepository.findAllByMatchResult_IdGame(idGame);
        for(Forecast f:forecastsByGameId){
            Forecast forecast = new Forecast();
            forecast.setForecaster(f.getForecaster());
            forecast.setId(f.getId());
            forecast.setDateTimeForecastMade(f.getDateTimeForecastMade());
            forecast.setMatchResult(f.getMatchResult());
            forecast.setForecastHomeTeamGoals(f.getForecastHomeTeamGoals());
            forecast.setForecastGuestTeamGoals(f.getForecastGuestTeamGoals());
            forecast.setPoint(0);
            forecastRepository.save(forecast);
            System.out.println("Записаны очки для " + f.getForecaster().getName()+ " " + f.getForecaster().getId());
            System.out.println("matchResultId = "+f.getMatchResult().getIdGameOnSoccer365ru()+ " Forecaster " + f.getForecaster().getName()
                    + " ForecasterId " + f.getForecaster().getId());
        }

    }

}
