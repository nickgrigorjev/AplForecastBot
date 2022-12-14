package com.example.aplforecastbot.service;
import com.example.aplforecastbot.entities.Forecast;
import com.example.aplforecastbot.entities.Forecaster;
import com.example.aplforecastbot.entities.MatchResult;
//import javafx.util.converter.LocalDateTimeStringConverter;
import com.example.aplforecastbot.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MatchResultService {
    private static final String directoryForSendGroupMessage = "D:\\Для работы\\Projects\\AplForecastBot\\rounds\\";

    private MatchResultRepository matchResultRepository;
    private UserRepository userRepository;
    private ParserService parserService;
    private EventRepository eventRepository;
    private WriterToJpgService writerToJpg;
    private RoundService roundService;
    private ForecastService forecastService;
    private ForecastRepository forecastRepository;
    private MessageIdsRepository msgRepository;
    private TeamRepository teamRepository;

    @Autowired
    public void setTeamRepository(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    private final char up = '⬆';
    private final char right = '➡';
    private final char down = '⬇';

    @Autowired
    public void setForecasterService(ForecasterService forecasterService) {
        this.forecasterService = forecasterService;
    }

    private ForecasterService forecasterService;

    @Autowired
    public void setMsgRepository(MessageIdsRepository msgRepository) {
        this.msgRepository = msgRepository;
    }

    @Autowired
    public void setForecastRepository(ForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }
    @Autowired
    private TelegramBot telegramBot;

    private ForecasterRepository forecasterRepository;

    @Autowired
    public void setForecasterRepository(ForecasterRepository forecasterRepository) {
        this.forecasterRepository = forecasterRepository;
    }


    @Autowired
    public void setForecastService(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @Autowired
    public void setRoundService(RoundService roundService) {
        this.roundService = roundService;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setMatchResultRepository(MatchResultRepository matchResultRepository) {
        this.matchResultRepository = matchResultRepository;
    }

    @Autowired
    public void setParserService(ParserService parserService) {
        this.parserService = parserService;
    }

    @Autowired
    public void setEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Autowired
    public void setWriterToJpg(WriterToJpgService writerToJpg) {
        this.writerToJpg = writerToJpg;
    }

//    @Scheduled(fixedDelay = 600000)
    void UpdatePointsAndRating() throws IOException {

//        Timestamp currentTimeToday = new Timestamp(System.currentTimeMillis());
        Timestamp currentTimeToday = new Timestamp(122, 9, 13, 19, 00, 0, 0);
        System.out.println(matchResultRepository.findCurrentRound(currentTimeToday));
        byte currentRound = matchResultRepository.findCurrentRound(currentTimeToday);
        System.out.println("Проверка! номер тура - "+matchResultRepository.findCurrentRound(currentTimeToday));
        System.out.println("Проверка! дата начала старта тура - "+roundService.roundRepository.findById((long)currentRound).get().getDateTimeStartRound());
        System.out.println("Проверка! текущая дата - "+currentTimeToday);
        if(currentTimeToday.after(roundService.roundRepository.findById((long)currentRound).get().getDateTimeStartRound())){
            List<MatchResult> matches = matchResultRepository.findAll().stream().
                    filter(matchId->matchId.getIdGameOnSoccer365ru()<1736329).filter(matchId->matchId.getIdGameOnSoccer365ru()>1736308).collect(Collectors.toList());
            long x = (long)(Math.random()*8000);
            for(MatchResult m: matches){

//                try {
//                    Thread.sleep(x);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                try {
//                    parserService.parseAndWriteMatchResultsToDatabase(m.getIdGameOnSoccer365ru());

                    if(m.getStatus().equalsIgnoreCase("завершен")){
                        forecastService.setPointsToForecast(m.getIdGameOnSoccer365ru());
                    }else{
                        forecastService.setNullPointToForecast(m.getIdGameOnSoccer365ru());
                    }
                    forecasterService.setPoint(m.getIdGameOnSoccer365ru());
                    forecasterService.setRating(m.getIdGameOnSoccer365ru());
                    System.out.println("Выполнено обновление для матча "+m.getHt()+" - " + m.getGt());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }else{
            System.out.println("Начисление очков на будущий тур запрещено");
        }



    }


//    void insertShortTeamNames(){
//        matchResultRepository.findAll().forEach(match->{
//            teamRepository.findAll().forEach(team -> {
//                if(team.getName().equalsIgnoreCase(match.getHt())){
//                    matchResultRepository.updateShortHt(team.getShortName(),match.getIdGameOnSoccer365ru());
//                    System.out.println("Внесено в базу короткое имя команды " + team.getShortName() + " для команды " + match.getHt());
//                }
//                if(team.getName().equalsIgnoreCase(match.getGt())){
//                    matchResultRepository.updateShortGt(team.getShortName(),match.getIdGameOnSoccer365ru());
//                    System.out.println("Внесено в базу короткое имя команды " + team.getShortName() + " для команды " + match.getGt());
//                }
//            });
//        });
//        System.out.println("Выполнено");
//    }//Внесение в MatchResult коротких имён команд

//    @Scheduled(fixedDelay = 2400000)
//    @Async
    void sendMessageWhoDidNotMakeForecast(){
        Timestamp currentTimeToday = new Timestamp(System.currentTimeMillis());
        System.out.println(matchResultRepository.findCurrentRound(currentTimeToday));
        byte currentRound = matchResultRepository.findCurrentRound(currentTimeToday);
        currentRound=(byte)(currentRound+1);
        List<Forecaster> forecasters = forecasterService.checkForecasts(currentRound);
        if(forecasters.size()==0){
            System.out.println("Все проставили прогнозы");
        }
        else {
            for(Forecaster f: forecasters){
                Date currentDate = new Date (roundService.roundRepository.findByNumberOfRound(currentRound).getDateTimeStartFirstMatchRound().getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String date = dateFormat.format(currentDate);
                String time = timeFormat.format(currentDate);

                String text = f.getName() +", пора сделать прогноз на " + currentRound+"-й тур\nНачало "+currentRound+"-ого тура " + date + " в " + time;
                telegramBot.sendMessage(f.getId(), text);
                System.out.println("Прогнозист " + f.getName() + " не сделал прогноз на " + currentRound+"-й тур");
            }

            System.out.println("Выполнено");
        }

    }

    @Scheduled(fixedDelay = 60000)
    private void doTestMethods() throws IOException {
        Timestamp currentTimeToday = new Timestamp(System.currentTimeMillis());
//        Timestamp currentTimeToday = new Timestamp(122, 9, 14, 20, 00, 0, 0);
        byte currentNumberOfRound = roundService.roundRepository.findCurrentRound(currentTimeToday);
        System.out.println(currentTimeToday+ " - "+currentNumberOfRound);
        List<MatchResult> matchesOfCurrentRound = matchResultRepository.findAllByNumberOfRound(currentNumberOfRound);

        if(currentNumberOfRound<=38){
            if(currentTimeToday.after(roundService.roundRepository.findById((long)currentNumberOfRound).get().getTimeMinus30())&&
            currentTimeToday.before(roundService.roundRepository.findById((long)currentNumberOfRound).get().getTimePlus30())){
                System.out.println("Размещение поста в телеге");//Запись всех матчей в папку
                matchResultRepository.findAllByNumberOfRound(currentNumberOfRound).stream().forEach(s-> {
                    System.out.println(s.getIdGameOnSoccer365ru());
                    try {
                        writerToJpg.writeForecastBeforeTheRoundToJpg(currentNumberOfRound);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                forecasterRepository.findAll().forEach(forecaster -> telegramBot.
                        sendSinglePhoto(forecaster.getId(), directoryForSendGroupMessage+roundService.roundRepository.
                                findCurrentRound(currentTimeToday)+"\\"+"forecast.jpg","Прогнозы на "+ currentNumberOfRound+"-й тур"));//отправка всех файлов с папки в чат

                System.out.println("Выполнено Размещение поста в телеге");//Запись всех матчей в папку
            }//Размещение поста перед туром

            if(matchesOfCurrentRound.stream().filter(matches->matches.getStatus().equalsIgnoreCase("завершен")).count()==10){

                writerToJpg.writeForecastResultToJpg(currentNumberOfRound);
                //**********************
//                String finalText1 = "Результаты " + currentNumberOfRound + "-ого тура\n#результаты_матчей_тура_" +currentNumberOfRound;
//                forecasterRepository.findAll().stream().forEach(fr->telegramBot.
//                        sendGroupFiles(fr.getId(),directoryForSendGroupMessage+roundService.roundRepository.
//                                findCurrentRound(currentTimeToday),finalText1));
//
//                String finalText2= "Результаты " + currentNumberOfRound + "-ого тура\n#результаты_прогнозов_тура_" +currentNumberOfRound;
//                forecasterRepository.findAll().stream().forEach(fr->telegramBot.
//                        sendSinglePhoto(fr.getId(),directoryForSendGroupMessage+roundService.roundRepository.
//                                findCurrentRound(currentTimeToday)+"\\"+"result.jpg",finalText2));
//
//                String finalText3= "Результаты " + currentNumberOfRound + "-ого тура";
//                forecasterRepository.findAll().stream().forEach(fr->telegramBot.
//                        sendSingleDocument(fr.getId(),directoryForSendGroupMessage+roundService.roundRepository.
//                                findCurrentRound(currentTimeToday)+"\\"+"result.jpg",finalText3+ "\n#документ_результаты_прогнозов_тура_" +currentNumberOfRound));
//                System.out.println("Выполнено");
                //*****************************

//                String finalText1 = "Результаты " + currentNumberOfRound + "-ого тура\n#результаты_матчей_тура_" +currentNumberOfRound;
//                telegramBot.sendGroupFiles(647528114,directoryForSendGroupMessage+roundService.roundRepository.
//                                findCurrentRound(currentTimeToday),finalText1);
//
//                String finalText2= "Результаты " + currentNumberOfRound + "-ого тура\n#результаты_прогнозов_тура_" +currentNumberOfRound;
//                telegramBot.sendSinglePhoto(647528114,directoryForSendGroupMessage+roundService.roundRepository.
//                                findCurrentRound(currentTimeToday)+"\\"+"result.jpg",finalText2);
//
//                String finalText3= "Результаты " + currentNumberOfRound + "-ого тура";
//                telegramBot.sendSingleDocument(647528114,directoryForSendGroupMessage+roundService.roundRepository.
//                                findCurrentRound(currentTimeToday)+"\\"+"result.jpg",finalText3+ "\n#документ_результаты_прогнозов_тура_" +currentNumberOfRound);
//                System.out.println("Выполнено");
            }


            long count = matchResultRepository.findAllByNumberOfRound(currentNumberOfRound).stream().
                    filter(matchResult ->currentTimeToday.after(matchResult.getDateOfMatch())).filter(matchResult -> !matchResult.getStatus().equals("завершен")).count();
            System.out.println("Количество игр на данный момент - " + count);

            List<MatchResult> currentMatches = matchResultRepository.findAllByNumberOfRound(currentNumberOfRound).stream().
                    filter(matchResult ->currentTimeToday.after(matchResult.getDateOfMatch())).filter(matchResult -> !matchResult.getStatus().equals("завершен")).collect(Collectors.toList());

            currentMatches.forEach(s1->{

                        try {
                            Thread.sleep((55000/count)-5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                            try {
                                MatchResult matchResultBefore = matchResultRepository.findById(s1.getIdGameOnSoccer365ru()).get();
                                parserService.parseAndWriteMatchResultsToDatabase(s1.getIdGameOnSoccer365ru());
                                MatchResult matchResultAfter = matchResultRepository.findById(s1.getIdGameOnSoccer365ru()).get();
                                /**Матч завершен*/
                                if(matchResultAfter.getStatus().equalsIgnoreCase("завершен")){
                                    forecastService.setPointsToForecast(s1.getIdGameOnSoccer365ru());
                                    forecasterService.setPoint(s1.getIdGameOnSoccer365ru());
                                    forecasterService.setRating(s1.getIdGameOnSoccer365ru());
                                    sendMessageWithResultMatch(forecasterRepository.findAll(),s1,currentTimeToday);

                                    if(matchesOfCurrentRound.stream().filter(matches->matches.getStatus().equalsIgnoreCase("завершен")).count()==10){
                                        String finalText = "Результаты " + currentNumberOfRound + "-ого тура\n#результаты_матчей_тура_" +currentNumberOfRound;
                                        writerToJpg.writeForecastResultToJpg(currentNumberOfRound);
                                        forecasterRepository.findAll().stream().forEach(fr->telegramBot.
                                                sendGroupFiles(fr.getId(),directoryForSendGroupMessage+roundService.roundRepository.
                                                        findCurrentRound(currentTimeToday),finalText));

                                        forecasterRepository.findAll().stream().forEach(fr->telegramBot.
                                                sendSinglePhoto(fr.getId(),directoryForSendGroupMessage+roundService.roundRepository.
                                                        findCurrentRound(currentTimeToday)+"\\"+"result.jpg",finalText+ "\n#результаты_тура_" +currentNumberOfRound));

                                        forecasterRepository.findAll().stream().forEach(fr->telegramBot.
                                                sendSingleDocument(fr.getId(),directoryForSendGroupMessage+roundService.roundRepository.
                                                        findCurrentRound(currentTimeToday)+"\\"+"result.jpg",finalText+ "\n#документ_результаты_тура_" +currentNumberOfRound));
                                        System.out.println("Выполнено");
                                    }
                                }else{
                                    /**Матч не завершен*/
                                    if(matchResultBefore.getHtGoals()==matchResultAfter.getHtGoals()
                                            &&matchResultBefore.getGtGoals()==matchResultAfter.getGtGoals()){

                                    }else{

                                        List<Forecaster> forecastersBefore = forecasterRepository.findAll();
                                        System.out.println("Проверка! before - " + forecastersBefore.get(0).getId() +" - "+ forecastersBefore.get(0).getRating());
                                        forecastService.setPointsToForecast(s1.getIdGameOnSoccer365ru());
                                        forecasterService.setPoint(s1.getIdGameOnSoccer365ru());
                                        forecasterService.setRating(s1.getIdGameOnSoccer365ru());
                                        List<Forecaster> forecastersAfter = forecasterRepository.findAll();
                                        System.out.println("Проверка! after - " + forecastersAfter.get(0).getId() +" - "+ forecastersAfter.get(0).getRating());
                                        for(Forecaster f1:forecastersBefore){
                                            for(Forecaster f2:forecastersAfter){
                                                if(Objects.equals(f1.getId(), f2.getId())){
                                                    if(f2.getRating()>f1.getRating()){
                                                        forecasterRepository.updateArrow(down,f2.getId());
                                                    }else if(f2.getRating()<f1.getRating()){
                                                        forecasterRepository.updateArrow(up,f2.getId());
                                                    }else{
                                                        forecasterRepository.updateArrow(right,f2.getId());
                                                    }
                                                }
                                            }
                                        }
                                        writerToJpg.writeInfoToJpg(s1.getIdGameOnSoccer365ru());
                                        sendMessageWithResultMatch(forecasterRepository.findAll(),s1,currentTimeToday);
                                    }
                                }



                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }

            });


        }
        else{
            System.out.println("Сезон завершен");
        }

    }

    private void sendMessageWithResultMatch(List<Forecaster> forecasters,MatchResult matchResult, Timestamp currentTimeToday) {
        String text = "";
        List<MatchResult> allMatchesOfRound = matchResultRepository.findAllByNumberOfRound(roundService.roundRepository.findCurrentRound(currentTimeToday));
        for (Forecaster f : forecasters.stream().sorted((o1, o2) -> o1.getRating() - o2.getRating()).collect(Collectors.toList())) {

            List<Forecast> forecasts = f.getForecasts();
            byte forecastHt;
            byte forecastGt;
            int forecastPoint;
            int pointsOfRound=0;
            Forecast forecastFromForecaster = forecastRepository.findByMatchResult_IdGameOnSoccer365ruAndForecaster_Id(matchResult.getIdGameOnSoccer365ru(), f.getId());
            forecastHt = forecastFromForecaster.getForecastHomeTeamGoals();
            forecastGt = forecastFromForecaster.getForecastGuestTeamGoals();
            forecastPoint = forecastFromForecaster.getPoint();
            for(MatchResult matchResult1:allMatchesOfRound){
                pointsOfRound+=forecastRepository.findByMatchResult_IdGameOnSoccer365ruAndForecaster_Id(matchResult1.getIdGameOnSoccer365ru(), f.getId()).getPoint();
            }

            if (forecastPoint == 0) {
                text += f.getRating() + ". " + f.getArrow() + " " + String.format("%02d", f.getPoints()) + " | "+ String.format("%02d", pointsOfRound) + " | "+ f.getName() + " | "
                        + forecastHt + ":" + forecastGt + " | " + forecastPoint + "\n";
            } else {
                text += f.getRating() + ". " + f.getArrow() + " " + String.format("%02d", f.getPoints()) + " | "+ String.format("%02d", pointsOfRound) + " | "+ f.getName() + " | "
                        + forecastHt + ":" + forecastGt + " | +" + forecastPoint + "\n";
            }


        }
        String finalText = text;
        forecasterRepository.findAll().stream().forEach(frks -> telegramBot.
                sendSinglePhoto(frks.getId(), directoryForSendGroupMessage + roundService.roundRepository.
                        findCurrentRound(currentTimeToday) + "\\" + matchResult.getHt() + "-" + matchResult.getGt() + ".jpg", finalText));
    }



    //    @Scheduled(fixedDelay = 14400000)
    private void updateDateTimeMatchResults() throws IOException {
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        byte startIndex = matchResultRepository.findCurrentRound(startTime);
        byte endIndex = (byte) (startIndex+2);
        if(endIndex>38){endIndex = 38;}
        System.out.println("Обновление дат матчей с "+startIndex+ " тура по "+endIndex);

        for(byte i = startIndex;i<endIndex;i++){
            matchResultRepository.findAllByNumberOfRound(i).stream().forEach(s-> {
                try {
                    System.out.println("******");
                    Thread.sleep((long)(Math.random()*8000)+5000);
                    parserService.parseAndWriteMatchDateToDatabase(s.getIdGameOnSoccer365ru());
                    System.out.println("Обновлена дата матча "+ s.getHt()+"-"+s.getGt());
                } catch (IOException e) {
                    log.error("Ошибка чтения сайта ");
                } catch (InterruptedException e) {
                    log.error("Ошибка чтения сайта " + LocalDateTime.now());
                }
            });
        }



//        AtomicInteger count = new AtomicInteger(1);
//            matchResultRepository.findAll().stream().filter(s->s.getIdGameOnSoccer365ru()<1736249).forEach(s-> {
//                try {
////                    System.out.println(roundService.roundRepository.findCurrentRound(new Timestamp(System.currentTimeMillis())));
//                    long x = (long)(Math.random()*23000)+5000;
//                    System.out.println("******");
//                    Thread.sleep(x);
//                    parserService.parseAndWriteMatchResultsToDatabase(s.getIdGameOnSoccer365ru());
////                    parserService.parseAndWriteMatchDateToDatabase(s.getIdGameOnSoccer365ru());
//                    writerToJpg.writeInfoToJpg(s.getIdGameOnSoccer365ru());
//                    System.out.println("Обновлена дата матча "+ s.getHt()+"-"+s.getGt()+" id - "+s.getIdGameOnSoccer365ru());
//                    System.out.println("Обновлено " + count + " записей\n Затрачено времени - " + (double)(x/1000) + " секунд");
//
//                    count.getAndIncrement();
//                }
//                catch (IOException e) {
//                    log.error("Ошибка чтения сайта ");
//                }
//                catch (InterruptedException e) {
//                    log.error("Ошибка чтения сайта " + LocalDateTime.now());
//                }
//            });



        roundService.checkAndUpdateDatesRound();
    }


    private void checkUpdateAndWriteMatchResults(long gameId, Optional<MatchResult> matchResultAtDataBase) throws IOException {
        List<String> eventsHt = new ArrayList<>();
        List<String> eventsAt = new ArrayList<>();
        List<String> eventsMin = new ArrayList<>();
        Calendar calendar;
        Timestamp dateOfMatch;
        Map<String,List<Byte>> statistic = new HashMap<>();
        Elements eventHtElements;
        Elements eventMinElements;
        Elements eventAtElements;
        Elements statisticHtElements =new Elements();
        Elements statisticHtAtElements;
        Elements statisticAtElements=new Elements();
        Elements statisticNameElements;
        Elements elements;
        String teamHt = "home";
        String teamAt = "guest";
        String scoreTeamHt = "0";
        String scoreTeamAt = "0";
        String eventDate="01/01/2000";
        String status="Не начат";
        byte htPenalties=0;
        byte gtPenalties=0;
        byte htOwnGoals=0;
        byte gtOwnGoals=0;

        String url = "https://soccer365.ru/games/"+gameId;
        try{
            Document doc = Jsoup.connect(url).userAgent("YaBrowser/22.7.0.1842").get();

            elements = doc.getElementsByAttributeValue("id","game_events");
            eventHtElements = doc.select("div>div.event_ht");
            eventMinElements = doc.select("div>div.event_min");
            eventAtElements = doc.select("div>div.event_at");
            statisticNameElements = doc.select("div>div.stats_item>div.stats_title");
            statisticHtAtElements = doc.select("div>div.stats_item>div.stats_inf");


            for(Element e:elements){
                eventDate = e.getElementsByTag("h2").text();
                status = e.getElementsByAttributeValue("class","live_game_status").text();
            }
            elements = doc.getElementsByAttributeValue("class","live_game left");

            for(Element e:elements){
                teamHt = e.getElementsByAttributeValue("class","live_game_ht").tagName("a").text();
                scoreTeamHt = e.getElementsByAttributeValue("class","live_game_goal").tagName("span").text();
                if(scoreTeamHt.equals("-")){scoreTeamHt="0";}
            }


            elements = doc.getElementsByAttributeValue("class","live_game right");
            for(Element e:elements){
                teamAt = e.getElementsByAttributeValue("class","live_game_at").tagName("a").text();
                scoreTeamAt = e.getElementsByAttributeValue("class","live_game_goal").tagName("span").text();
                if(scoreTeamAt.equals("-")){scoreTeamAt="0";}
            }


            for(int i=0;i<eventMinElements.size();i++){
                String eventHt = "";
                String eventAt = "";
                Elements goalHtElements = eventHtElements.get(i).getAllElements();
                for(Element e:goalHtElements){
                    if(e.hasClass("event_ht_icon live_goal")) eventHt="goal";
                    if(e.hasClass("event_ht_icon live_yellowcard")) eventHt="yellowCard";
                    if(e.hasClass("event_ht_icon live_yellowred")) eventHt="yellowRed";
                    if(e.hasClass("event_ht_icon live_owngoal")) {eventHt="ownGoal";gtOwnGoals++;}
                    if(e.hasClass("event_ht_icon live_pengoal")) {eventHt="penalty";htPenalties++;}
                }
                Elements goalAtElements = eventAtElements.get(i).getAllElements();
                for(Element e:goalAtElements){
                    if(e.hasClass("event_at_icon live_goal")) eventAt="goal";
                    if(e.hasClass("event_at_icon live_yellowcard")) eventAt="yellowCard";
                    if(e.hasClass("event_at_icon live_yellowred")) eventAt="yellowRed";
                    if(e.hasClass("event_at_icon live_owngoal")) {eventAt="ownGoal";htOwnGoals++;}
                    if(e.hasClass("event_at_icon live_pengoal")) {eventAt="penalty";gtPenalties++;}
                }
                eventsHt.add(eventHtElements.get(i).text()+";"+eventHt);
                eventsAt.add(eventAt+";"+eventAtElements.get(i).text());
                eventsMin.add(eventMinElements.get(i).text());
//                System.out.printf("%-25s %-10s [%4s] %10s %25s\n",eventHtElements.get(i).text(),eventHt,eventMinElements.get(i).text(),eventAt,eventAtElements.get(i).text());
            }

            for(int i=0;i<statisticHtAtElements.size();i++){
                if(i%2==0)statisticHtElements.add(statisticHtAtElements.get(i));
                else statisticAtElements.add(statisticHtAtElements.get(i));
            }

            for(int i=0;i<statisticNameElements.size();i++){
                ArrayList<Byte> list = new ArrayList<>();
                list.add((byte) Double.parseDouble(statisticHtElements.get(i).text()));
                list.add((byte)Double.parseDouble(statisticAtElements.get(i).text()));
                statistic.put(statisticNameElements.get(i).text().toLowerCase(),list);
            }


            try{
                MatchResult matchResultCurrent = new MatchResult();
                matchResultCurrent.setIdGameOnSoccer365ru(gameId);//1
                String[] data = eventDate.split(",");
                matchResultCurrent.setStatus(status.toLowerCase());//2
                matchResultCurrent.setNumberOfRound(Byte.parseByte(data[1].trim().replaceAll("-й тур","")));//3
//                System.out.println(data[3]);
//                String[] date = data[3].substring(1,11).split("\\.");
//                String[] time = data[3].substring(12).split(":");
//                localDateTime1=LocalDateTime.of(Integer.parseInt(date[2]),Integer.parseInt(date[1]),
//                        Integer.parseInt(date[0]),Integer.parseInt(time[0]),Integer.parseInt(time[1]),0,0);
//                System.out.println("Успех#6");
//                dateOfMatch = new Timestamp(localDateTime1.toInstant(ZoneOffset.UTC).toEpochMilli());
//                System.out.println("Успех#7");
//                System.out.println(dateOfMatch);
//                System.out.println("Успех#8");
//                matchResultCurrent.setDateOfMatch(dateOfMatch);//4
                matchResultCurrent.setDateOfMatch(matchResultAtDataBase.get().getDateOfMatch());//4

                matchResultCurrent.setHt(teamHt);
                matchResultCurrent.setGt(teamAt);

                matchResultCurrent.setHtGoals(Byte.parseByte(scoreTeamHt));
                matchResultCurrent.setGtGoals(Byte.parseByte(scoreTeamAt));

                matchResultCurrent.setHtPenalties(htPenalties);
                matchResultCurrent.setGtPenalties(gtPenalties);

                matchResultCurrent.setHtOwnGoals(htOwnGoals);
                matchResultCurrent.setGtOwnGoals(gtOwnGoals);

                matchResultCurrent.setHtYellowCards(statistic.get("желтые карточки").get(0));
                matchResultCurrent.setGtYellowCards(statistic.get("желтые карточки").get(1));

                matchResultCurrent.setHtRedCards(statistic.get("красные карточки").get(0));
                matchResultCurrent.setGtRedCards(statistic.get("красные карточки").get(1));

                matchResultCurrent.setHtOffsides(statistic.get("офсайды").get(0));
                matchResultCurrent.setGtOffsides(statistic.get("офсайды").get(1));

                matchResultCurrent.setHtShots(statistic.get("удары").get(0));
                matchResultCurrent.setGtShots(statistic.get("удары").get(1));

                matchResultCurrent.setHtShotsOnTarget(statistic.get("удары в створ").get(0));
                matchResultCurrent.setGtShotsOnTarget(statistic.get("удары в створ").get(1));

                matchResultCurrent.setHtCornerKicks(statistic.get("угловые").get(0));
                matchResultCurrent.setGtCornerKicks(statistic.get("угловые").get(1));

                matchResultCurrent.setHtFouls(statistic.get("нарушения").get(0));
                matchResultCurrent.setGtFouls(statistic.get("нарушения").get(1));

                matchResultCurrent.setHtPossession(statistic.get("владение %").get(0));
                matchResultCurrent.setGtPossession(statistic.get("владение %").get(1));

                if(matchResultAtDataBase.isPresent()){
                    if(     matchResultAtDataBase.get().getHtGoals()==matchResultCurrent.getHtGoals()&&
                            matchResultAtDataBase.get().getGtGoals()==matchResultCurrent.getGtGoals()&&
                            matchResultAtDataBase.get().getHtYellowCards()==matchResultCurrent.getHtYellowCards()&&
                            matchResultAtDataBase.get().getGtYellowCards()==matchResultCurrent.getGtYellowCards()&&
                            matchResultAtDataBase.get().getHtRedCards()==matchResultCurrent.getHtRedCards()&&
                            matchResultAtDataBase.get().getGtRedCards()==matchResultCurrent.getGtRedCards()&&
                            !matchResultCurrent.getStatus().equalsIgnoreCase("завершен")    )
                    {System.out.println("новых событий к "+matchResultCurrent.getStatus()+" в матче " + matchResultCurrent.getHt() + " - " + matchResultCurrent.getGt()+" нет");}
                    else{matchResultRepository.save(matchResultCurrent);
                        System.out.println("к "+matchResultCurrent.getStatus()+" появилось новое событие в матче " + matchResultCurrent.getHt() + " - " + matchResultCurrent.getGt());
                        writerToJpg.writeInfoToJpg(matchResultCurrent.getIdGameOnSoccer365ru());//метод записи картинки в папку
                        forecastService.setPointsToForecast(matchResultCurrent.getIdGameOnSoccer365ru());//Метод рассчитывающий количество очков

//                        forecasterRepository.findAll().forEach(forecaster -> telegramBot.
//                                sendGroupPhoto(forecaster.getId(), directoryForSendGroupMessage+roundService.roundRepository.
//                                        findCurrentRound(new Timestamp(System.currentTimeMillis()))));//Метод благодаря которому бот отправляет сообщение с обновленными данными
                    }
                }




            }catch(NumberFormatException e){
                log.error("Error set teamScore: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }catch(NullPointerException e){
                log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }

        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
