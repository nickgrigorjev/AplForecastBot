package com.example.aplforecastbot.service;

import com.example.aplforecastbot.entities.Event;
import com.example.aplforecastbot.entities.MatchResult;
import com.example.aplforecastbot.entities.Round;
import com.example.aplforecastbot.repository.EventRepository;
import com.example.aplforecastbot.repository.ForecasterRepository;
import com.example.aplforecastbot.repository.MatchResultRepository;
import com.example.aplforecastbot.repository.RoundRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
public class ParserService {

    private static final String directoryForSendGroupMessage = "D:\\Для работы\\Projects\\AplForecastBot\\rounds\\";
    private MatchResultRepository matchResultRepository;
    private EventRepository eventRepository;
    private RoundRepository roundRepository;

    private WriterToJpgService writerToJpg;
    private ForecastService forecastService;
    private ForecasterRepository forecasterRepository;

//    @Autowired
//    private TelegramBot telegramBot;

    @Autowired
    public void setWriterToJpg(WriterToJpgService writerToJpg) {
        this.writerToJpg = writerToJpg;
    }
    @Autowired
    public void setForecastService(ForecastService forecastService) {
        this.forecastService = forecastService;
    }
    @Autowired
    public void setForecasterRepository(ForecasterRepository forecasterRepository) {
        this.forecasterRepository = forecasterRepository;
    }

    @Autowired
    public void setRoundRepository(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }

    @Autowired
    public void setMatchResultRepository(MatchResultRepository matchResultRepository) {
        this.matchResultRepository = matchResultRepository;
    }


    @Autowired
    public void setEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }



    void parseAndWriteMatchResultsToDatabase(long gameId) throws IOException {
        List<String> eventsHt = new ArrayList<>();
        List<String> eventsAt = new ArrayList<>();
        List<String> eventsMin = new ArrayList<>();
        Map<String,List<Integer>> statistic = new HashMap<>();
        statistic.clear();
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
        byte htMisPen=0;
        byte gtMisPen=0;

        String url = "https://soccer365.ru/games/"+gameId;
        try{
            Document doc = Jsoup.connect(url).userAgent("YaBrowser/22.7.0.1842").get();
            elements = doc.getElementsByAttributeValue("id", "game_events");
            eventHtElements = doc.select("div>div.event_ht");
            eventMinElements = doc.select("div>div.event_min");
            eventAtElements = doc.select("div>div.event_at");
            statisticNameElements = doc.select("div>div.stats_item>div.stats_title");
            statisticHtAtElements = doc.select("div>div.stats_item>div.stats_inf");

            for (Element e : elements) {
//                addTextInImage(e.getElementsByTag("h2").text(),"jpg",input,output,10,25,false,15);//Заголовок файла
                eventDate = e.getElementsByTag("h2").text();
                System.out.println(eventDate); //Заголовок
                status = e.getElementsByAttributeValue("class", "live_game_status").text();
                System.out.println(e.getElementsByAttributeValue("class", "live_game_status").text());
            }
            elements = doc.getElementsByAttributeValue("class", "live_game left");

            for (Element e : elements) {
                teamHt = e.getElementsByAttributeValue("class", "live_game_ht").tagName("a").text();
                scoreTeamHt = e.getElementsByAttributeValue("class", "live_game_goal").tagName("span").text();
                System.out.print(teamHt); //Команда хозяев
                System.out.print(" " + scoreTeamHt); //Счет хозяев
                System.out.print(" " + e.getElementsByAttributeValue("class", "live_game_tlogo").tagName("img").text()); //Лого хозяев
            }


            elements = doc.getElementsByAttributeValue("class", "live_game right");
            for (Element e : elements) {
                teamAt = e.getElementsByAttributeValue("class", "live_game_at").tagName("a").text();
                scoreTeamAt = e.getElementsByAttributeValue("class", "live_game_goal").tagName("span").text();
                System.out.print(": " + scoreTeamAt); //Счет гостей
                System.out.print(" " + teamAt); //Команда гостей

            }

            System.out.println();

            for (int i = 0; i < eventMinElements.size(); i++) {
                String eventHt = "";
                String eventAt = "";
                Elements goalHtElements = eventHtElements.get(i).getAllElements();
                for (Element e : goalHtElements) {
                    if(e.hasClass("event_ht_icon live_goal")) eventHt="goal";
                    if(e.hasClass("event_ht_icon live_yellowcard")) eventHt="yellowCard";
                    if(e.hasClass("event_ht_icon live_redcard")) eventHt="redCard";
                    if(e.hasClass("event_ht_icon live_yellowred")) eventHt="yellowRed";
                    if(e.hasClass("event_ht_icon live_mispen")) {eventHt="mispen";htMisPen++;}
                    if(e.hasClass("event_ht_icon live_owngoal")) {eventHt="ownGoal";gtOwnGoals++;}
                    if(e.hasClass("event_ht_icon live_pengoal")) {eventHt="penalty";htPenalties++;}
                }
                Elements goalAtElements = eventAtElements.get(i).getAllElements();
                for (Element e : goalAtElements) {
                    if(e.hasClass("event_at_icon live_goal")) eventAt="goal";
                    if(e.hasClass("event_at_icon live_yellowcard")) eventAt="yellowCard";
                    if(e.hasClass("event_at_icon live_redcard")) eventAt="redCard";
                    if(e.hasClass("event_at_icon live_yellowred")) eventAt="yellowRed";
                    if(e.hasClass("event_at_icon live_mispen")) {eventAt="mispen";gtMisPen++;}
                    if(e.hasClass("event_at_icon live_owngoal")) {eventAt="ownGoal";htOwnGoals++;}
                    if(e.hasClass("event_at_icon live_pengoal")) {eventAt="penalty";gtPenalties++;}
                }
                eventsHt.add(eventHtElements.get(i).text() + ";" + eventHt);
                eventsAt.add(eventAt + ";" + eventAtElements.get(i).text());
                eventsMin.add(eventMinElements.get(i).text());
                System.out.printf("%-25s %-10s [%4s] %10s %25s\n", eventHtElements.get(i).text(), eventHt, eventMinElements.get(i).text(), eventAt, eventAtElements.get(i).text());
            }

            System.out.println("*************************************************************************");
            System.out.println(eventsHt);
            System.out.println(eventsAt);
            System.out.println("Инфа для сбора компонентов объекта");
            System.out.println("status - " + status.toLowerCase());
            System.out.println(eventDate);
            String[] data = eventDate.split(",");
            System.out.println("numberOfRound - " + Integer.parseInt(data[1].trim().replaceAll("-й тур", "")));
//            localDateTime1=localDateTimeStringConverter.fromString(data[3].substring(1));
//                System.out.println(data[3].substring(1, 11));
//                String[] date = data[3].substring(1, 11).split("\\.");
//                String[] time = data[3].substring(12).split(":");
//                localDateTime1 = LocalDateTime.of(Integer.parseInt(date[2]), Integer.parseInt(date[1]),
//                        Integer.parseInt(date[0]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), 0, 0);
//                dateOfMatch = new Timestamp(localDateTime1.toInstant(ZoneOffset.ofHours(+4)).toEpochMilli());
//                System.out.println(dateOfMatch);
            System.out.println("ht - " + teamHt);
            System.out.println("gt - " + teamAt);
            System.out.println("ht - " + scoreTeamHt);
            System.out.println("gt - " + scoreTeamAt);


            for (int i=0;i<statisticHtAtElements.size();i++) {
                if(i%2==0)statisticHtElements.add(statisticHtAtElements.get(i));
                else statisticAtElements.add(statisticHtAtElements.get(i));
            }

            for (int i=0;i<statisticNameElements.size(); i++) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add((int) Double.parseDouble(statisticHtElements.get(i).text()));
                list.add((int) Double.parseDouble(statisticAtElements.get(i).text()));
                statistic.put(statisticNameElements.get(i).text().toLowerCase(), list);
            }
            System.out.println("*****************************************"+statistic.size());

        }catch(IOException e){
            e.printStackTrace();
        }

        Optional<MatchResult> matchResultAtDataBase = matchResultRepository.findById(gameId);

        System.out.println("*************************************************************************");
        System.out.println(eventsHt);
        System.out.println(eventsAt);
        System.out.println("Инфа для сбора компонентов объекта");
        System.out.println("status - " + status.toLowerCase());
        System.out.println(eventDate);
        String[] data = eventDate.split(",");
        System.out.println("numberOfRound - " + Integer.parseInt(data[1].trim().replaceAll("-й тур", "")));
        System.out.println("ht - " + teamHt);
        System.out.println("gt - " + teamAt);
        System.out.println("ht - " + scoreTeamHt);
        System.out.println("gt - " + scoreTeamAt);
        System.out.println("htPenalties - " + htPenalties);
        System.out.println("gtPenalties - " + gtPenalties);
        System.out.println("htOwnGoals - " + htOwnGoals);
        System.out.println("gtOwnGoals - " + gtOwnGoals);
        System.out.println(statistic);
        System.out.println("htYellowCards - " + statistic.get("желтые карточки").get(0));
        System.out.println("gtYellowCards - " + statistic.get("желтые карточки").get(1));

        System.out.println("htRedCards - " + statistic.get("красные карточки").get(0));
        System.out.println("gtRedCards - " + statistic.get("красные карточки").get(1));

        System.out.println("htOffsides - " + statistic.get("офсайды").get(0));
        System.out.println("gtOffsides - " + statistic.get("офсайды").get(1));

        System.out.println("htShots - " + statistic.get("удары").get(0));
        System.out.println("gtShots - " + statistic.get("удары").get(1));

        System.out.println("htShotsOnTarget - " + statistic.get("удары в створ").get(0));
        System.out.println("gtShotsOnTarget - " + statistic.get("удары в створ").get(1));

        System.out.println("htCornerKicks - " + statistic.get("угловые").get(0));
        System.out.println("gtCornerKicks - " + statistic.get("угловые").get(1));

        System.out.println("htFouls - " + statistic.get("нарушения").get(0));
        System.out.println("gtFouls - " + statistic.get("нарушения").get(1));

        System.out.println("htPossession - " + statistic.get("владение %").get(0));
        System.out.println("gtPossession - " + statistic.get("владение %").get(1));

            try{



                MatchResult matchResult = new MatchResult();
                matchResult.setIdGameOnSoccer365ru(matchResultRepository.findById(gameId).get().getIdGameOnSoccer365ru());
                matchResult.setStatus(status.toLowerCase());
                matchResult.setNumberOfRound(matchResultRepository.findById(gameId).get().getNumberOfRound());
                matchResult.setDateOfMatch(matchResultRepository.findById(gameId).get().getDateOfMatch());
                matchResult.setHt(matchResultRepository.findById(gameId).get().getHt());
                matchResult.setGt(matchResultRepository.findById(gameId).get().getGt());
                matchResult.setRound(roundRepository.findByNumberOfRound(matchResult.getNumberOfRound()));

                matchResult.setHtGoals(Byte.parseByte(scoreTeamHt));
                matchResult.setGtGoals(Byte.parseByte(scoreTeamAt));

                matchResult.setHtPenalties(htPenalties);
                matchResult.setGtPenalties(gtPenalties);

                matchResult.setHtOwnGoals(htOwnGoals);
                matchResult.setGtOwnGoals(gtOwnGoals);

                matchResult.setHtMisPen(htMisPen);
                matchResult.setGtMisPen(gtMisPen);


//                    matchResult.setHtYellowCards(statistic.get("желтые карточки").get(0));
//                    matchResult.setGtYellowCards(statistic.get("желтые карточки").get(1));
//
//                    matchResult.setHtRedCards(statistic.get("красные карточки").get(0));
//                    matchResult.setGtRedCards(statistic.get("красные карточки").get(1));
//
//                    matchResult.setHtOffsides(statistic.get("офсайды").get(0));
//                    matchResult.setGtOffsides(statistic.get("офсайды").get(1));
//
//                    matchResult.setHtShots(statistic.get("удары").get(0));
//                    matchResult.setGtShots(statistic.get("удары").get(1));
//
//                    matchResult.setHtShotsOnTarget(statistic.get("удары в створ").get(0));
//                    matchResult.setGtShotsOnTarget(statistic.get("удары в створ").get(1));
//
//                    matchResult.setHtCornerKicks(statistic.get("угловые").get(0));
//                    matchResult.setGtCornerKicks(statistic.get("угловые").get(1));
//
//                    matchResult.setHtFouls(statistic.get("нарушения").get(0));
//                    matchResult.setGtFouls(statistic.get("нарушения").get(1));
//
//                    matchResult.setHtPossession(statistic.get("владение %").get(0));
//                    matchResult.setGtPossession(statistic.get("владение %").get(1));

                matchResult.setHtYellowCards(Byte.parseByte(statistic.get("желтые карточки").get(0).toString()));
                matchResult.setGtYellowCards(Byte.parseByte(statistic.get("желтые карточки").get(1).toString()));

                matchResult.setHtRedCards(Byte.parseByte(statistic.get("красные карточки").get(0).toString()));
                matchResult.setGtRedCards(Byte.parseByte(statistic.get("красные карточки").get(1).toString()));

                matchResult.setHtOffsides(Byte.parseByte(statistic.get("офсайды").get(0).toString()));
                matchResult.setGtOffsides(Byte.parseByte(statistic.get("офсайды").get(1).toString()));

                matchResult.setHtShots(Byte.parseByte(statistic.get("удары").get(0).toString()));
                matchResult.setGtShots(Byte.parseByte(statistic.get("удары").get(1).toString()));

                matchResult.setHtShotsOnTarget(Byte.parseByte(statistic.get("удары в створ").get(0).toString()));
                matchResult.setGtShotsOnTarget(Byte.parseByte(statistic.get("удары в створ").get(1).toString()));

                matchResult.setHtCornerKicks(Byte.parseByte(statistic.get("угловые").get(0).toString()));
                matchResult.setGtCornerKicks(Byte.parseByte(statistic.get("угловые").get(1).toString()));

                matchResult.setHtFouls(Byte.parseByte(statistic.get("нарушения").get(0).toString()));
                matchResult.setGtFouls(Byte.parseByte(statistic.get("нарушения").get(1).toString()));

                matchResult.setHtPossession(Byte.parseByte(statistic.get("владение %").get(0).toString()));
                matchResult.setGtPossession(Byte.parseByte(statistic.get("владение %").get(1).toString()));


                    if(matchResultAtDataBase.isPresent()){
                        if(matchResultAtDataBase.get().getHtGoals()==matchResult.getHtGoals()&&
                                matchResultAtDataBase.get().getGtGoals()==matchResult.getGtGoals()&&
//                                matchResultAtDataBase.get().getHtYellowCards()==matchResult.getHtYellowCards()&&
//                                matchResultAtDataBase.get().getGtYellowCards()==matchResult.getGtYellowCards()&&
//                                matchResultAtDataBase.get().getHtOwnGoals()==matchResult.getHtOwnGoals()&&
//                                matchResultAtDataBase.get().getGtOwnGoals()==matchResult.getGtOwnGoals()&&
//                                matchResultAtDataBase.get().getHtMisPen()==matchResult.getHtMisPen()&&
//                                matchResultAtDataBase.get().getGtMisPen()==matchResult.getGtMisPen()&&
                                !matchResult.getStatus().equalsIgnoreCase("завершен"))
                        {System.out.println("новых событий к "+matchResult.getStatus()+" в матче " + matchResult.getHt() + " - " + matchResult.getGt()+" нет");}
                        else{
                            matchResultRepository.save(matchResult);
                            Event event = new Event();
                            eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
                            for(int i=0;i<eventsMin.size();i++){
                                event.setId(gameId);
                                event.setHtEvent(eventsHt.get(i));
                                event.setGtEvent(eventsAt.get(i));
                                event.setMinEvent(eventsMin.get(i));
                                event.setEventDate(eventDate);
                                event.setEventStatus(status);
                                event.setMatchResult(matchResult);
                                eventRepository.saveAndFlush(event);
                            }
                            System.out.println("к "+matchResult.getStatus()+" появилось новое событие в матче " + matchResult.getHt() + " - " + matchResult.getGt());
                            writerToJpg.writeInfoToJpg(matchResult.getIdGameOnSoccer365ru());//метод записи картинки в папку
//                            forecastService.setPointsToForecast(matchResult.getIdGameOnSoccer365ru());//Метод рассчитывающий количество очков

//                            forecasterRepository.findAll().forEach(forecaster -> telegramBot.
//                                    sendGroupPhoto(forecaster.getId(), directoryForSendGroupMessage+roundRepository.
//                                            findCurrentRound(new Timestamp(System.currentTimeMillis()))));
                            //Метод благодаря которому бот отправляет сообщение с обновленными данными
//
                        }

                    }



                matchResultRepository.save(matchResult);

                Event event = new Event();
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(new Date(matchResult.getDateOfMatch().getTime()));

                eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
                if(eventsMin.size()==0){
                    String myDateString = String.valueOf(matchResult.getDateOfMatch());
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    SimpleDateFormat outFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    event.setId(gameId);
                    event.setHtEvent(";");
                    event.setGtEvent(";");
                    event.setMinEvent("");
                    event.setEventDate("Премьер-лига, "+matchResult.getNumberOfRound()+"-й тур, регулярный сезон, " +
                            outFormat.format(inFormat.parse(myDateString)));
                    event.setEventStatus(status);
                    event.setMatchResult(matchResult);
                    eventRepository.saveAndFlush(event);
                } else
                    for(int i=0;i<eventsMin.size();i++){
                        event.setId(gameId);
                        event.setHtEvent(eventsHt.get(i));
                        event.setGtEvent(eventsAt.get(i));
                        event.setMinEvent(eventsMin.get(i));
                        event.setEventDate(eventDate);
                        event.setEventStatus(status);
                        event.setMatchResult(matchResult);
                        eventRepository.saveAndFlush(event);
                    }




            }catch(NullPointerException|NumberFormatException e){
                log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));

                MatchResult matchResult = new MatchResult();
                matchResult.setIdGameOnSoccer365ru(matchResultRepository.findById(gameId).get().getIdGameOnSoccer365ru());
                matchResult.setStatus(status.toLowerCase());
                matchResult.setNumberOfRound(matchResultRepository.findById(gameId).get().getNumberOfRound());
                matchResult.setDateOfMatch(matchResultRepository.findById(gameId).get().getDateOfMatch());
                matchResult.setHt(matchResultRepository.findById(gameId).get().getHt());
                matchResult.setGt(matchResultRepository.findById(gameId).get().getGt());
                matchResult.setRound(roundRepository.findByNumberOfRound(matchResult.getNumberOfRound()));

                matchResult.setHtGoals((byte) 0);
                matchResult.setGtGoals((byte) 0);

                matchResult.setHtPenalties(htPenalties);
                matchResult.setGtPenalties(gtPenalties);

                matchResult.setHtOwnGoals((byte) 0);
                matchResult.setGtOwnGoals((byte) 0);

                matchResult.setHtMisPen((byte)0);
                matchResult.setGtMisPen((byte)0);

                matchResult.setHtYellowCards((byte) 0);
                matchResult.setGtYellowCards((byte) 0);

                matchResult.setHtRedCards((byte) 0);
                matchResult.setGtRedCards((byte) 0);

                matchResult.setHtOffsides((byte) 0);
                matchResult.setGtOffsides((byte) 0);

                matchResult.setHtShots((byte) 0);
                matchResult.setGtShots((byte) 0);

                matchResult.setHtShotsOnTarget((byte) 0);
                matchResult.setGtShotsOnTarget((byte) 0);

                matchResult.setHtCornerKicks((byte) 0);
                matchResult.setGtCornerKicks((byte) 0);

                matchResult.setHtFouls((byte) 0);
                matchResult.setGtFouls((byte) 0);

                matchResult.setHtPossession((byte) 0);
                matchResult.setGtPossession((byte) 0);

                if(matchResultAtDataBase.isPresent()){
                    if(matchResultAtDataBase.get().getHtGoals()==matchResult.getHtGoals()&&
                            matchResultAtDataBase.get().getGtGoals()==matchResult.getGtGoals()&&
                            matchResultAtDataBase.get().getHtYellowCards()==matchResult.getHtYellowCards()&&
                            matchResultAtDataBase.get().getGtYellowCards()==matchResult.getGtYellowCards()&&
                            matchResultAtDataBase.get().getHtOwnGoals()==matchResult.getHtOwnGoals()&&
                            matchResultAtDataBase.get().getGtOwnGoals()==matchResult.getGtOwnGoals()&&
                            matchResultAtDataBase.get().getHtMisPen()==matchResult.getHtMisPen()&&
                            matchResultAtDataBase.get().getGtMisPen()==matchResult.getGtMisPen()&&
                            !matchResult.getStatus().equalsIgnoreCase("завершен"))
                    {System.out.println("новых событий к "+matchResult.getStatus()+" в матче " + matchResult.getHt() + " - " + matchResult.getGt()+" нет");}
                    else{
                        matchResultRepository.save(matchResult);
                        Event event = new Event();
                        eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
                        for(int i=0;i<eventsMin.size();i++){
                            event.setId(gameId);
                            event.setHtEvent(eventsHt.get(i));
                            event.setGtEvent(eventsAt.get(i));
                            event.setMinEvent(eventsMin.get(i));
                            event.setEventDate(eventDate);
                            event.setEventStatus(status);
                            event.setMatchResult(matchResult);
                            eventRepository.saveAndFlush(event);
                        }
                        System.out.println("к "+matchResult.getStatus()+" появилось новое событие в матче " + matchResult.getHt() + " - " + matchResult.getGt());
                        writerToJpg.writeInfoToJpg(matchResult.getIdGameOnSoccer365ru());//метод записи картинки в папку
                        forecastService.setPointsToForecast(matchResult.getIdGameOnSoccer365ru());//Метод рассчитывающий количество очков

//                        forecasterRepository.findAll().forEach(forecaster -> telegramBot.
//                                sendGroupPhoto(forecaster.getId(), directoryForSendGroupMessage+roundRepository.
//                                        findCurrentRound(new Timestamp(System.currentTimeMillis()))));
//                        //Метод благодаря которому бот отправляет сообщение с обновленными данными

                    }
                }



                matchResultRepository.save(matchResult);

                Event event = new Event();
                System.out.println("Проверка "+matchResultRepository.findById(gameId).get().getIdGameOnSoccer365ru());
                eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
                System.out.println("Проверка "+eventsMin.size());
                    event.setId(gameId);
                    event.setHtEvent(";");
                    event.setGtEvent(";");
                    event.setMinEvent(";");
                    event.setEventDate("Премьер-Лига, "+matchResult.getNumberOfRound()+"-й тур, регулярный сезон, " +
                            matchResult.getDateOfMatch().getDay()+"."+matchResult.getDateOfMatch().getMonth()+"."+
                            matchResult.getDateOfMatch().getYear()+" "+matchResult.getDateOfMatch().getHours()+":"+
                            matchResult.getDateOfMatch().getMinutes());
                    event.setEventStatus(status);
                    event.setMatchResult(matchResult);
                    eventRepository.saveAndFlush(event);

            } catch (ParseException e) {
                e.printStackTrace();
            }


    }

    void parseAndWriteMatchResultsToDatabase1(long gameId) throws IOException {
        List<String> eventsHt = new ArrayList<>();
        List<String> eventsAt = new ArrayList<>();
        List<String> eventsMin = new ArrayList<>();
        Map<String,List<Integer>> statistic = new HashMap<>();
        statistic.clear();
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
        byte htMisPen=0;
        byte gtMisPen=0;

        String url = "https://soccer365.ru/games/"+gameId;
        try{
            Document doc = Jsoup.connect(url).userAgent("YaBrowser/22.7.0.1842").get();
            elements = doc.getElementsByAttributeValue("id", "game_events");
            eventHtElements = doc.select("div>div.event_ht");
            eventMinElements = doc.select("div>div.event_min");
            eventAtElements = doc.select("div>div.event_at");
            statisticNameElements = doc.select("div>div.stats_item>div.stats_title");
            statisticHtAtElements = doc.select("div>div.stats_item>div.stats_inf");

            for (Element e : elements) {
//                addTextInImage(e.getElementsByTag("h2").text(),"jpg",input,output,10,25,false,15);//Заголовок файла
                eventDate = e.getElementsByTag("h2").text();
                System.out.println(eventDate); //Заголовок
                status = e.getElementsByAttributeValue("class", "live_game_status").text();
                System.out.println(e.getElementsByAttributeValue("class", "live_game_status").text());
            }
            elements = doc.getElementsByAttributeValue("class", "live_game left");

            for (Element e : elements) {
                teamHt = e.getElementsByAttributeValue("class", "live_game_ht").tagName("a").text();
                scoreTeamHt = e.getElementsByAttributeValue("class", "live_game_goal").tagName("span").text();
                System.out.print(teamHt); //Команда хозяев
                System.out.print(" " + scoreTeamHt); //Счет хозяев
                System.out.print(" " + e.getElementsByAttributeValue("class", "live_game_tlogo").tagName("img").text()); //Лого хозяев
            }


            elements = doc.getElementsByAttributeValue("class", "live_game right");
            for (Element e : elements) {
                teamAt = e.getElementsByAttributeValue("class", "live_game_at").tagName("a").text();
                scoreTeamAt = e.getElementsByAttributeValue("class", "live_game_goal").tagName("span").text();
                System.out.print(": " + scoreTeamAt); //Счет гостей
                System.out.print(" " + teamAt); //Команда гостей

            }

            System.out.println();

            for (int i = 0; i < eventMinElements.size(); i++) {
                String eventHt = "";
                String eventAt = "";
                Elements goalHtElements = eventHtElements.get(i).getAllElements();
                for (Element e : goalHtElements) {
                    if(e.hasClass("event_ht_icon live_goal")) eventHt="goal";
                    if(e.hasClass("event_ht_icon live_yellowcard")) eventHt="yellowCard";
                    if(e.hasClass("event_ht_icon live_redcard")) eventHt="redCard";
                    if(e.hasClass("event_ht_icon live_yellowred")) eventHt="yellowRed";
                    if(e.hasClass("event_ht_icon live_mispen")) {eventHt="mispen";htMisPen++;}
                    if(e.hasClass("event_ht_icon live_owngoal")) {eventHt="ownGoal";gtOwnGoals++;}
                    if(e.hasClass("event_ht_icon live_pengoal")) {eventHt="penalty";htPenalties++;}
                }
                Elements goalAtElements = eventAtElements.get(i).getAllElements();
                for (Element e : goalAtElements) {
                    if(e.hasClass("event_at_icon live_goal")) eventAt="goal";
                    if(e.hasClass("event_at_icon live_yellowcard")) eventAt="yellowCard";
                    if(e.hasClass("event_at_icon live_redcard")) eventAt="redCard";
                    if(e.hasClass("event_at_icon live_yellowred")) eventAt="yellowRed";
                    if(e.hasClass("event_at_icon live_mispen")) {eventAt="mispen";gtMisPen++;}
                    if(e.hasClass("event_at_icon live_owngoal")) {eventAt="ownGoal";htOwnGoals++;}
                    if(e.hasClass("event_at_icon live_pengoal")) {eventAt="penalty";gtPenalties++;}
                }
                eventsHt.add(eventHtElements.get(i).text() + ";" + eventHt);
                eventsAt.add(eventAt + ";" + eventAtElements.get(i).text());
                eventsMin.add(eventMinElements.get(i).text());
                System.out.printf("%-25s %-10s [%4s] %10s %25s\n", eventHtElements.get(i).text(), eventHt, eventMinElements.get(i).text(), eventAt, eventAtElements.get(i).text());
            }

            System.out.println("*************************************************************************");
            System.out.println(eventsHt);
            System.out.println(eventsAt);
            System.out.println("Инфа для сбора компонентов объекта");
            System.out.println("status - " + status.toLowerCase());
            System.out.println(eventDate);
            String[] data = eventDate.split(",");
            System.out.println("numberOfRound - " + Integer.parseInt(data[1].trim().replaceAll("-й тур", "")));
//            localDateTime1=localDateTimeStringConverter.fromString(data[3].substring(1));
//                System.out.println(data[3].substring(1, 11));
//                String[] date = data[3].substring(1, 11).split("\\.");
//                String[] time = data[3].substring(12).split(":");
//                localDateTime1 = LocalDateTime.of(Integer.parseInt(date[2]), Integer.parseInt(date[1]),
//                        Integer.parseInt(date[0]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), 0, 0);
//                dateOfMatch = new Timestamp(localDateTime1.toInstant(ZoneOffset.ofHours(+4)).toEpochMilli());
//                System.out.println(dateOfMatch);
            System.out.println("ht - " + teamHt);
            System.out.println("gt - " + teamAt);
            System.out.println("ht - " + scoreTeamHt);
            System.out.println("gt - " + scoreTeamAt);


            for (int i=0;i<statisticHtAtElements.size();i++) {
                if(i%2==0)statisticHtElements.add(statisticHtAtElements.get(i));
                else statisticAtElements.add(statisticHtAtElements.get(i));
            }

            for (int i=0;i<statisticNameElements.size(); i++) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add((int) Double.parseDouble(statisticHtElements.get(i).text()));
                list.add((int) Double.parseDouble(statisticAtElements.get(i).text()));
                statistic.put(statisticNameElements.get(i).text().toLowerCase(), list);
            }
            System.out.println("*****************************************"+statistic.size());

        }catch(IOException e){
            e.printStackTrace();
        }

        Optional<MatchResult> matchResultAtDataBase = matchResultRepository.findById(gameId);

        System.out.println("*************************************************************************");
        System.out.println(eventsHt);
        System.out.println(eventsAt);
        System.out.println("Инфа для сбора компонентов объекта");
        System.out.println("status - " + status.toLowerCase());
        System.out.println(eventDate);
        String[] data = eventDate.split(",");
        System.out.println("numberOfRound - " + Integer.parseInt(data[1].trim().replaceAll("-й тур", "")));
        System.out.println("ht - " + teamHt);
        System.out.println("gt - " + teamAt);
        System.out.println("ht - " + scoreTeamHt);
        System.out.println("gt - " + scoreTeamAt);
        System.out.println("htPenalties - " + htPenalties);
        System.out.println("gtPenalties - " + gtPenalties);
        System.out.println("htOwnGoals - " + htOwnGoals);
        System.out.println("gtOwnGoals - " + gtOwnGoals);
        System.out.println(statistic);
        System.out.println("htYellowCards - " + statistic.get("желтые карточки").get(0));
        System.out.println("gtYellowCards - " + statistic.get("желтые карточки").get(1));

        System.out.println("htRedCards - " + statistic.get("красные карточки").get(0));
        System.out.println("gtRedCards - " + statistic.get("красные карточки").get(1));

        System.out.println("htOffsides - " + statistic.get("офсайды").get(0));
        System.out.println("gtOffsides - " + statistic.get("офсайды").get(1));

        System.out.println("htShots - " + statistic.get("удары").get(0));
        System.out.println("gtShots - " + statistic.get("удары").get(1));

        System.out.println("htShotsOnTarget - " + statistic.get("удары в створ").get(0));
        System.out.println("gtShotsOnTarget - " + statistic.get("удары в створ").get(1));

        System.out.println("htCornerKicks - " + statistic.get("угловые").get(0));
        System.out.println("gtCornerKicks - " + statistic.get("угловые").get(1));

        System.out.println("htFouls - " + statistic.get("нарушения").get(0));
        System.out.println("gtFouls - " + statistic.get("нарушения").get(1));

        System.out.println("htPossession - " + statistic.get("владение %").get(0));
        System.out.println("gtPossession - " + statistic.get("владение %").get(1));

        try{



            MatchResult matchResult = new MatchResult();
            matchResult.setIdGameOnSoccer365ru(matchResultRepository.findById(gameId).get().getIdGameOnSoccer365ru());
            matchResult.setStatus(status.toLowerCase());
            matchResult.setNumberOfRound(matchResultRepository.findById(gameId).get().getNumberOfRound());
            matchResult.setDateOfMatch(matchResultRepository.findById(gameId).get().getDateOfMatch());
            matchResult.setHt(matchResultRepository.findById(gameId).get().getHt());
            matchResult.setGt(matchResultRepository.findById(gameId).get().getGt());
            matchResult.setRound(roundRepository.findByNumberOfRound(matchResult.getNumberOfRound()));

            matchResult.setHtGoals(Byte.parseByte(scoreTeamHt));
            matchResult.setGtGoals(Byte.parseByte(scoreTeamAt));

            matchResult.setHtPenalties(htPenalties);
            matchResult.setGtPenalties(gtPenalties);

            matchResult.setHtOwnGoals(htOwnGoals);
            matchResult.setGtOwnGoals(gtOwnGoals);

            matchResult.setHtMisPen(htMisPen);
            matchResult.setGtMisPen(gtMisPen);


//                    matchResult.setHtYellowCards(statistic.get("желтые карточки").get(0));
//                    matchResult.setGtYellowCards(statistic.get("желтые карточки").get(1));
//
//                    matchResult.setHtRedCards(statistic.get("красные карточки").get(0));
//                    matchResult.setGtRedCards(statistic.get("красные карточки").get(1));
//
//                    matchResult.setHtOffsides(statistic.get("офсайды").get(0));
//                    matchResult.setGtOffsides(statistic.get("офсайды").get(1));
//
//                    matchResult.setHtShots(statistic.get("удары").get(0));
//                    matchResult.setGtShots(statistic.get("удары").get(1));
//
//                    matchResult.setHtShotsOnTarget(statistic.get("удары в створ").get(0));
//                    matchResult.setGtShotsOnTarget(statistic.get("удары в створ").get(1));
//
//                    matchResult.setHtCornerKicks(statistic.get("угловые").get(0));
//                    matchResult.setGtCornerKicks(statistic.get("угловые").get(1));
//
//                    matchResult.setHtFouls(statistic.get("нарушения").get(0));
//                    matchResult.setGtFouls(statistic.get("нарушения").get(1));
//
//                    matchResult.setHtPossession(statistic.get("владение %").get(0));
//                    matchResult.setGtPossession(statistic.get("владение %").get(1));

            matchResult.setHtYellowCards(Byte.parseByte(statistic.get("желтые карточки").get(0).toString()));
            matchResult.setGtYellowCards(Byte.parseByte(statistic.get("желтые карточки").get(1).toString()));

            matchResult.setHtRedCards(Byte.parseByte(statistic.get("красные карточки").get(0).toString()));
            matchResult.setGtRedCards(Byte.parseByte(statistic.get("красные карточки").get(1).toString()));

            matchResult.setHtOffsides(Byte.parseByte(statistic.get("офсайды").get(0).toString()));
            matchResult.setGtOffsides(Byte.parseByte(statistic.get("офсайды").get(1).toString()));

            matchResult.setHtShots(Byte.parseByte(statistic.get("удары").get(0).toString()));
            matchResult.setGtShots(Byte.parseByte(statistic.get("удары").get(1).toString()));

            matchResult.setHtShotsOnTarget(Byte.parseByte(statistic.get("удары в створ").get(0).toString()));
            matchResult.setGtShotsOnTarget(Byte.parseByte(statistic.get("удары в створ").get(1).toString()));

            matchResult.setHtCornerKicks(Byte.parseByte(statistic.get("угловые").get(0).toString()));
            matchResult.setGtCornerKicks(Byte.parseByte(statistic.get("угловые").get(1).toString()));

            matchResult.setHtFouls(Byte.parseByte(statistic.get("нарушения").get(0).toString()));
            matchResult.setGtFouls(Byte.parseByte(statistic.get("нарушения").get(1).toString()));

            matchResult.setHtPossession(Byte.parseByte(statistic.get("владение %").get(0).toString()));
            matchResult.setGtPossession(Byte.parseByte(statistic.get("владение %").get(1).toString()));


            if(matchResultAtDataBase.isPresent()){
                if(matchResultAtDataBase.get().getHtGoals()==matchResult.getHtGoals()&&
                        matchResultAtDataBase.get().getGtGoals()==matchResult.getGtGoals()&&
//                                matchResultAtDataBase.get().getHtYellowCards()==matchResult.getHtYellowCards()&&
//                                matchResultAtDataBase.get().getGtYellowCards()==matchResult.getGtYellowCards()&&
//                                matchResultAtDataBase.get().getHtOwnGoals()==matchResult.getHtOwnGoals()&&
//                                matchResultAtDataBase.get().getGtOwnGoals()==matchResult.getGtOwnGoals()&&
//                                matchResultAtDataBase.get().getHtMisPen()==matchResult.getHtMisPen()&&
//                                matchResultAtDataBase.get().getGtMisPen()==matchResult.getGtMisPen()&&
                        !matchResult.getStatus().equalsIgnoreCase("завершен"))
                {System.out.println("новых событий к "+matchResult.getStatus()+" в матче " + matchResult.getHt() + " - " + matchResult.getGt()+" нет");}
                else{
                    matchResultRepository.save(matchResult);
                    Event event = new Event();
                    eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
                    for(int i=0;i<eventsMin.size();i++){
                        event.setId(gameId);
                        event.setHtEvent(eventsHt.get(i));
                        event.setGtEvent(eventsAt.get(i));
                        event.setMinEvent(eventsMin.get(i));
                        event.setEventDate(eventDate);
                        event.setEventStatus(status);
                        event.setMatchResult(matchResult);
                        eventRepository.saveAndFlush(event);
                    }
                    System.out.println("к "+matchResult.getStatus()+" появилось новое событие в матче " + matchResult.getHt() + " - " + matchResult.getGt());
                    writerToJpg.writeInfoToJpg(matchResult.getIdGameOnSoccer365ru());//метод записи картинки в папку
//                            forecastService.setPointsToForecast(matchResult.getIdGameOnSoccer365ru());//Метод рассчитывающий количество очков

//                    forecasterRepository.findAll().forEach(forecaster -> telegramBot.
//                            sendGroupPhoto(forecaster.getId(), directoryForSendGroupMessage+roundRepository.
//                                    findCurrentRound(new Timestamp(System.currentTimeMillis()))));
                    //Метод благодаря которому бот отправляет сообщение с обновленными данными
//
                }

            }



            matchResultRepository.save(matchResult);

            Event event = new Event();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(matchResult.getDateOfMatch().getTime()));

            eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
            if(eventsMin.size()==0){
                String myDateString = String.valueOf(matchResult.getDateOfMatch());
                SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                SimpleDateFormat outFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                event.setId(gameId);
                event.setHtEvent(";");
                event.setGtEvent(";");
                event.setMinEvent("");
                event.setEventDate("Премьер-лига, "+matchResult.getNumberOfRound()+"-й тур, регулярный сезон, " +
                        outFormat.format(inFormat.parse(myDateString)));
                event.setEventStatus(status);
                event.setMatchResult(matchResult);
                eventRepository.saveAndFlush(event);
            } else
                for(int i=0;i<eventsMin.size();i++){
                    event.setId(gameId);
                    event.setHtEvent(eventsHt.get(i));
                    event.setGtEvent(eventsAt.get(i));
                    event.setMinEvent(eventsMin.get(i));
                    event.setEventDate(eventDate);
                    event.setEventStatus(status);
                    event.setMatchResult(matchResult);
                    eventRepository.saveAndFlush(event);
                }




        }catch(NullPointerException|NumberFormatException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));

            MatchResult matchResult = new MatchResult();
            matchResult.setIdGameOnSoccer365ru(matchResultRepository.findById(gameId).get().getIdGameOnSoccer365ru());
            matchResult.setStatus(status.toLowerCase());
            matchResult.setNumberOfRound(matchResultRepository.findById(gameId).get().getNumberOfRound());
            matchResult.setDateOfMatch(matchResultRepository.findById(gameId).get().getDateOfMatch());
            matchResult.setHt(matchResultRepository.findById(gameId).get().getHt());
            matchResult.setGt(matchResultRepository.findById(gameId).get().getGt());
            matchResult.setRound(roundRepository.findByNumberOfRound(matchResult.getNumberOfRound()));

            matchResult.setHtGoals((byte) 0);
            matchResult.setGtGoals((byte) 0);

            matchResult.setHtPenalties(htPenalties);
            matchResult.setGtPenalties(gtPenalties);

            matchResult.setHtOwnGoals((byte) 0);
            matchResult.setGtOwnGoals((byte) 0);

            matchResult.setHtMisPen((byte)0);
            matchResult.setGtMisPen((byte)0);

            matchResult.setHtYellowCards((byte) 0);
            matchResult.setGtYellowCards((byte) 0);

            matchResult.setHtRedCards((byte) 0);
            matchResult.setGtRedCards((byte) 0);

            matchResult.setHtOffsides((byte) 0);
            matchResult.setGtOffsides((byte) 0);

            matchResult.setHtShots((byte) 0);
            matchResult.setGtShots((byte) 0);

            matchResult.setHtShotsOnTarget((byte) 0);
            matchResult.setGtShotsOnTarget((byte) 0);

            matchResult.setHtCornerKicks((byte) 0);
            matchResult.setGtCornerKicks((byte) 0);

            matchResult.setHtFouls((byte) 0);
            matchResult.setGtFouls((byte) 0);

            matchResult.setHtPossession((byte) 0);
            matchResult.setGtPossession((byte) 0);

            if(matchResultAtDataBase.isPresent()){
                if(matchResultAtDataBase.get().getHtGoals()==matchResult.getHtGoals()&&
                        matchResultAtDataBase.get().getGtGoals()==matchResult.getGtGoals()&&
                        matchResultAtDataBase.get().getHtYellowCards()==matchResult.getHtYellowCards()&&
                        matchResultAtDataBase.get().getGtYellowCards()==matchResult.getGtYellowCards()&&
                        matchResultAtDataBase.get().getHtOwnGoals()==matchResult.getHtOwnGoals()&&
                        matchResultAtDataBase.get().getGtOwnGoals()==matchResult.getGtOwnGoals()&&
                        matchResultAtDataBase.get().getHtMisPen()==matchResult.getHtMisPen()&&
                        matchResultAtDataBase.get().getGtMisPen()==matchResult.getGtMisPen()&&
                        !matchResult.getStatus().equalsIgnoreCase("завершен"))
                {System.out.println("новых событий к "+matchResult.getStatus()+" в матче " + matchResult.getHt() + " - " + matchResult.getGt()+" нет");}
                else{
                    matchResultRepository.save(matchResult);
                    Event event = new Event();
                    eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
                    for(int i=0;i<eventsMin.size();i++){
                        event.setId(gameId);
                        event.setHtEvent(eventsHt.get(i));
                        event.setGtEvent(eventsAt.get(i));
                        event.setMinEvent(eventsMin.get(i));
                        event.setEventDate(eventDate);
                        event.setEventStatus(status);
                        event.setMatchResult(matchResult);
                        eventRepository.saveAndFlush(event);
                    }
                    System.out.println("к "+matchResult.getStatus()+" появилось новое событие в матче " + matchResult.getHt() + " - " + matchResult.getGt());
                    writerToJpg.writeInfoToJpg(matchResult.getIdGameOnSoccer365ru());//метод записи картинки в папку
                    forecastService.setPointsToForecast(matchResult.getIdGameOnSoccer365ru());//Метод рассчитывающий количество очков

//                        forecasterRepository.findAll().forEach(forecaster -> telegramBot.
//                                sendGroupPhoto(forecaster.getId(), directoryForSendGroupMessage+roundRepository.
//                                        findCurrentRound(new Timestamp(System.currentTimeMillis()))));
//                        //Метод благодаря которому бот отправляет сообщение с обновленными данными

                }
            }



            matchResultRepository.save(matchResult);

            Event event = new Event();
            System.out.println("Проверка "+matchResultRepository.findById(gameId).get().getIdGameOnSoccer365ru());
            eventRepository.deleteAllByMatchResult(matchResultRepository.findById(gameId).get());
            System.out.println("Проверка "+eventsMin.size());
            event.setId(gameId);
            event.setHtEvent(";");
            event.setGtEvent(";");
            event.setMinEvent(";");
            event.setEventDate("Премьер-Лига, "+matchResult.getNumberOfRound()+"-й тур, регулярный сезон, " +
                    matchResult.getDateOfMatch().getDay()+"."+matchResult.getDateOfMatch().getMonth()+"."+
                    matchResult.getDateOfMatch().getYear()+" "+matchResult.getDateOfMatch().getHours()+":"+
                    matchResult.getDateOfMatch().getMinutes());
            event.setEventStatus(status);
            event.setMatchResult(matchResult);
            eventRepository.saveAndFlush(event);

        } catch (ParseException e) {
            e.printStackTrace();
        }


    }


    void parseAndWriteMatchDateToDatabase(long gameId) throws IOException {
        Elements elements;
        Calendar calendar;
        Timestamp dateTimeStartFirstMatchRound;
        Timestamp dateTimeStartRound;
        Timestamp dateTimePostingAllMatchesRound;
        Timestamp timeMinus30;
        Timestamp timePlus30;
        String eventDate="01/01/2000";
        String url = "https://soccer365.ru/games/"+gameId;
        try{
            Document doc = Jsoup.connect(url).userAgent("YaBrowser/22.7.0.1842").get();

            elements = doc.getElementsByAttributeValue("id","game_events");

            for(Element e:elements){
                eventDate = e.getElementsByTag("h2").text();
            }


            try{
                String[] data = eventDate.split(",");
                String[] date = data[3].substring(1,11).split("\\.");
                String[] time = data[3].substring(12).split(":");
                calendar =new GregorianCalendar(Integer.parseInt(date[2]),Integer.parseInt(date[1])-1,
                        Integer.parseInt(date[0]),Integer.parseInt(time[0]),Integer.parseInt(time[1]));
                dateTimeStartFirstMatchRound = new Timestamp(calendar.getTime().getTime());
                matchResultRepository.updateDateOfMatch(dateTimeStartFirstMatchRound,gameId);
                matchResultRepository.updateRoundId(matchResultRepository.findById(gameId).get().getNumberOfRound(),gameId);
            }catch(NumberFormatException e){
                log.error("Error set teamScore: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }catch(NullPointerException e){
            }

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    void checkAndUpdateDatesInDatabase(Round round){
//        List<MatchResult> matchResults = round.getMatchResults().stream().forEach(s->s.);
    }
}
