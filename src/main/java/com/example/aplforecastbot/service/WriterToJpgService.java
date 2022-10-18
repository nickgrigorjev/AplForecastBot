package com.example.aplforecastbot.service;

import com.example.aplforecastbot.entities.Event;
import com.example.aplforecastbot.entities.Forecast;
import com.example.aplforecastbot.entities.Forecaster;
import com.example.aplforecastbot.entities.MatchResult;
import com.example.aplforecastbot.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WriterToJpgService {
    private static File input = new File("D:\\Для работы\\Projects\\DownloadingImages\\image.jpg");
    private static File inputForecasterResult = new File("D:\\Для работы\\Projects\\AplForecastBot\\inputImages\\fkresult.jpg");
    private static final char up = '⬆';
    private static final char right = '➡';
    private static final char down = '⬇';
    private static void addImageInImageVertical(String teamName, Graphics g, BufferedImage image, int x, int y, boolean isHomeTeam) throws IOException {
        BufferedImage overLay = ImageIO.read(new File("D:\\Для работы\\Projects\\DownloadingImages\\teams\\"+teamName+".png"));
        if(isHomeTeam){x= 1181-x-(overLay.getWidth());}
        else x = 1181+ x ;
        g.drawImage(overLay,x,y,null);

    }
    private static void addTextInImageVertical(String text, Graphics2D w, int x, int y, int position, int fontSize) throws IOException {
        w.setFont(new Font(Font.SANS_SERIF, Font.BOLD,fontSize));
        FontMetrics fontMetrics = w.getFontMetrics();
        Rectangle2D rect = fontMetrics.getStringBounds(text, w);
        if(position==-1){x= (1181-(int)rect.getWidth()-x);}
        if(position==0){x= (1181-((int)rect.getWidth()/2)-x);}
        if(position==1){x=x+1181;}
        w.drawString(text,x,y);
    }
    private static void addImageInImageHorizontal(String icon, Graphics g, BufferedImage image, int x, int y, int position) throws IOException {
        BufferedImage overLay = ImageIO.read(new File("D:\\Для работы\\Projects\\AplForecastBot\\icons\\"+icon+".png"));
        if(position==-1){x= x-(overLay.getWidth());}
        if(position==0){x= x-(overLay.getWidth()/2);}
        if(position==1) {x = x ;}
        g.drawImage(overLay,x,y,null);

    }
    private static void addTextInImageHorizonal(String text, Graphics2D w, int x, int y, int middleX, int position, int fontSize) throws IOException {
        w.setFont(new Font(Font.SANS_SERIF, Font.BOLD,fontSize));
        FontMetrics fontMetrics = w.getFontMetrics();
        Rectangle2D rect = fontMetrics.getStringBounds(text, w);
        if(position==-1){x= (middleX-(int)rect.getWidth()-x);}
        if(position==0){x= (middleX-((int)rect.getWidth()/2)-x);}
        if(position==1){x=x+middleX;}
        if(position==2){x=x;}
        w.drawString(text,x,y);
    }

    private MatchResultRepository matchResultRepository;
    private EventRepository eventRepository;
    private RoundRepository roundRepository;
    private ForecasterRepository forecasterRepository;
    private TeamRepository teamRepository;
    private ForecastRepository forecastRepository;

    @Autowired
    public void setForecastRepository(ForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }

    @Autowired
    public void setTeamRepository(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
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

    public void writeInfoToJpg(long gameId) throws IOException {
        int startYPositionsForEvents=1300;
        Optional<MatchResult> matchResult = matchResultRepository.findById(gameId);
        List<Event> eventList = eventRepository.findAllByMatchResult(matchResult.get());
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        File output = new File("D:\\Для работы\\Projects\\AplForecastBot\\rounds\\"+
                matchResult.get().getNumberOfRound()+"\\" +
                matchResult.get().getHt() + "-" + matchResult.get().getGt() + ".jpg");

        File output2 = new File("D:\\Для работы\\Projects\\AplForecastBot\\rounds\\comonEvents\\"
                + currentTime.toString().replaceAll(":","_") + "_" + matchResult.get().getHt()
                + " - " + matchResult.get().getGt() + ".jpg");



        try {


            eventList.stream().forEach(s-> System.out.println(s.getMatchResult().getIdGameOnSoccer365ru()+":  " + s.getHtEvent()+"-"+s.getMinEvent()+"  "+s.getGtEvent()));

            System.out.println("List of Events - " + eventList.size());
            System.out.println("First Element of List of Events - " + eventList.get(0).getHtEvent());
//        System.out.println("List of Events - " + eventRepository.findAllByIdGameOnSoccer365ru(gameId).size());
            Event event = new Event();

            currentTime.toString().replaceAll(":","_");



            BufferedImage image = ImageIO.read(input);
            int imageType = "png".equalsIgnoreCase("jpg") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage bold = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
            Graphics2D w = (Graphics2D) bold.getGraphics();
            w.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            w.drawImage(image, 0, 0, null);
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            w.setComposite(alpha);
            w.setColor(Color.BLACK);

            addTextInImageVertical(matchResult.get().getGt(), w, 90, 1000, 1, 135);//Команда гостей
            addTextInImageVertical(matchResult.get().getHt(), w, 90, 1000, -1, 135);//Команда хозяев

            addTextInImageVertical(String.valueOf(matchResult.get().getGtGoals()), w, 80, 750, 1, 500);//Счет гостей
            addTextInImageVertical(String.valueOf(matchResult.get().getHtGoals()), w, 80, 750, -1, 500);//Счет хозяев


            addTextInImageVertical(eventList.get(0).getEventDate(), w, 0, 120, 0, 75);
            addTextInImageVertical(eventList.get(0).getEventStatus(), w, 0, 1200, 0, 70);



            for (int i = 0; i < eventList.size(); i++) {
                try {
                    String[] eventsHomeTeam = eventList.get(i).getHtEvent().split(";");
                    addTextInImageVertical(eventsHomeTeam[0], w, 150, startYPositionsForEvents, -1, 60);

                } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
                }

                addTextInImageVertical(eventList.get(i).getMinEvent(), w, 0, startYPositionsForEvents, 0, 60);

                try {
                    String[] eventsATeam = eventList.get(i).getGtEvent().split(";");
                    addTextInImageVertical(eventsATeam[1], w, 150, startYPositionsForEvents, 1, 60);
                } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
                }

                startYPositionsForEvents += 90;
            }
            /**Блок Статистики*/
            addTextInImageVertical(String.valueOf(matchResult.get().getHtShots()), w, 600, 2550, -1, 50);
            addTextInImageVertical("Удары", w, 0, 2550, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtShots()), w, 600, 2550, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtShotsOnTarget()), w, 600, 2650, -1, 50);
            addTextInImageVertical("Удары в створ", w, 0, 2650, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtShotsOnTarget()), w, 600, 2650, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtCornerKicks()), w, 600, 2750, -1, 50);
            addTextInImageVertical("Угловые", w, 0, 2750, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtCornerKicks()), w, 600, 2750, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtFouls()), w, 600, 2850, -1, 50);
            addTextInImageVertical("Нарушения", w, 0, 2850, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtFouls()), w, 600, 2850, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtYellowCards()), w, 600, 2950, -1, 50);
            addTextInImageVertical("Желтые карточки", w, 0, 2950, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtYellowCards()), w, 600, 2950, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtRedCards()), w, 600, 3050, -1, 50);
            addTextInImageVertical("Красные карточки", w, 0, 3050, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtRedCards()), w, 600, 3050, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtOffsides()), w, 600, 3150, -1, 50);
            addTextInImageVertical("Офсайды", w, 0, 3150, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtOffsides()), w, 600, 3150, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtPossession()), w, 600, 3250, -1, 50);
            addTextInImageVertical("Владение мячом, %", w, 0, 3250, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtPossession()), w, 600, 3250, 1, 50);

            w.dispose();
            ImageIO.write(bold, "jpg", output);

            //*****

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);


            addImageInImageVertical(matchResult.get().getHt(), g, image2, 470, 250, true);//Лого хозяев
            addImageInImageVertical(matchResult.get().getGt(), g, image2, 470, 250, false);//Лого гостей


            startYPositionsForEvents = 1300;
            for (int i = 0; i < eventList.size(); i++) {
                try {
                    String[] eventsHomeTeam = eventList.get(i).getHtEvent().split(";");

                    switch (eventsHomeTeam[1]) {
                        case "goal":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "yellowCard":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "yellowRed":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "ownGoal":
                            addImageInImageVertical(eventsHomeTeam[1], g, image, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "penalty":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "redCard":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "mispen":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "var":
                            addImageInImageVertical(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        default:
                            break;
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
                }


                try {
                    String[] eventsATeam = eventList.get(i).getGtEvent().split(";");
                    switch (eventsATeam[0]) {
                        case "goal":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "yellowCard":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "yellowRed":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "ownGoal":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "penalty":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "redCard":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "mispen":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "var":
                            addImageInImageVertical(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        default:
                            break;
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
                }

                startYPositionsForEvents += 90;
            }

            g.dispose();
            ImageIO.write(combined, "png", output);
            ImageIO.write(combined, "png", output2);

        } catch (NullPointerException e) {
            //log.error("Нет данных статистики: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            System.out.println("htYellowCards - " + "-");
            System.out.println("gtYellowCards - " + "-");

            System.out.println("htRedCards - " + "-");
            System.out.println("gtRedCards - " + "-");

            System.out.println("htOffsides - " + "-");
            System.out.println("gtOffsides - " + "-");

            System.out.println("htShots - " + "-");
            System.out.println("gtShots - " + "-");

            System.out.println("htShotsOnTarget - " + "-");
            System.out.println("gtShotsOnTarget - " + "-");

            System.out.println("htCornerKicks - " + "-");
            System.out.println("gtCornerKicks - " + "-");

            System.out.println("htFouls - " + "-");
            System.out.println("gtFouls - " + "-");

            System.out.println("htPossession - " + "-");
            System.out.println("gtPossession - " + "-");


            System.out.println("************************");

            BufferedImage image = ImageIO.read(input);
            int imageType = "png".equalsIgnoreCase("jpg") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage bold = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
            Graphics2D w = (Graphics2D) bold.getGraphics();
            w.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            w.drawImage(image, 0, 0, null);
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            w.setComposite(alpha);
            w.setColor(Color.BLACK);

            addTextInImageVertical(matchResult.get().getGt(), w, 90, 1000, 1, 135);//Команда гостей
            addTextInImageVertical(matchResult.get().getHt(), w, 90, 1000, -1, 135);//Команда хозяев

            addTextInImageVertical(String.valueOf(matchResult.get().getGtGoals()), w, 80, 750, 1, 500);//Счет гостей
            addTextInImageVertical(String.valueOf(matchResult.get().getHtGoals()), w, 80, 750, -1, 500);//Счет хозяев

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");
            String dateTime = matchResult.get().getDateOfMatch().toLocalDateTime().format(formatter);
            String text = "Премьер-Лига, "+roundRepository.findCurrentRound(new Timestamp(System.currentTimeMillis()))+"-й тур, регулярный сезон," +dateTime;

            addTextInImageVertical(text, w, 0, 120, 0, 75);

//            addTextInImage(matchResult.get().getEvents().get(0).getEventDate(), w, 0, 120, 0, 75);
            addTextInImageVertical(matchResult.get().getEvents().get(0).getEventStatus(), w, 0, 1200, 0, 70);

            addTextInImageVertical("-", w, 600, 2550, -1, 50);
            addTextInImageVertical("Удары", w, 0, 2550, 0, 50);
            addTextInImageVertical("-", w, 600, 2550, 1, 50);

            addTextInImageVertical("-", w, 600, 2650, -1, 50);
            addTextInImageVertical("Удары в створ", w, 0, 2650, 0, 50);
            addTextInImageVertical("-", w, 600, 2650, 1, 50);

            addTextInImageVertical("-", w, 600, 2750, -1, 50);
            addTextInImageVertical("Угловые", w, 0, 2750, 0, 50);
            addTextInImageVertical("-", w, 600, 2750, 1, 50);

            addTextInImageVertical("-", w, 600, 2850, -1, 50);
            addTextInImageVertical("Нарушения", w, 0, 2850, 0, 50);
            addTextInImageVertical("-", w, 600, 2850, 1, 50);

            addTextInImageVertical("-", w, 600, 2950, -1, 50);
            addTextInImageVertical("Желтые карточки", w, 0, 2950, 0, 50);
            addTextInImageVertical("-", w, 600, 2950, 1, 50);

            addTextInImageVertical("-", w, 600, 3050, -1, 50);
            addTextInImageVertical("Красные карточки", w, 0, 3050, 0, 50);
            addTextInImageVertical("-", w, 600, 3050, 1, 50);

            addTextInImageVertical("-", w, 600, 3150, -1, 50);
            addTextInImageVertical("Офсайды", w, 0, 3150, 0, 50);
            addTextInImageVertical("-", w, 600, 3150, 1, 50);

            addTextInImageVertical("-", w, 600, 3250, -1, 50);
            addTextInImageVertical("Владение мячом, %", w, 0, 3250, 0, 50);
            addTextInImageVertical("-", w, 600, 3250, 1, 50);

            w.dispose();
            ImageIO.write(bold, "jpg", output);

            //*****

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);


            addImageInImageVertical(matchResult.get().getHt(), g, image2, 470, 250, true);//Лого хозяев
            addImageInImageVertical(matchResult.get().getGt(), g, image2, 470, 250, false);//Лого гостей


            g.dispose();
            ImageIO.write(combined, "png", output);
            ImageIO.write(combined, "png", output2);

        } catch (IndexOutOfBoundsException e){
            log.info("Нет записанных событий");
            System.out.println("**********************************************************************************");
            BufferedImage image = ImageIO.read(input);
            int imageType = "png".equalsIgnoreCase("jpg") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage bold = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
            Graphics2D w = (Graphics2D) bold.getGraphics();
            w.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            w.drawImage(image, 0, 0, null);
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            w.setComposite(alpha);
            w.setColor(Color.BLACK);

            addTextInImageVertical(matchResult.get().getGt(), w, 90, 1000, 1, 135);//Команда гостей
            addTextInImageVertical(matchResult.get().getHt(), w, 90, 1000, -1, 135);//Команда хозяев

            addTextInImageVertical(String.valueOf(matchResult.get().getGtGoals()), w, 80, 750, 1, 500);//Счет гостей
            addTextInImageVertical(String.valueOf(matchResult.get().getHtGoals()), w, 80, 750, -1, 500);//Счет хозяев

            System.out.println("#1 Размер листа Событий " + matchResult.get().getEvents().size());


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            String dateTime = matchResult.get().getDateOfMatch().toLocalDateTime().format(formatter);
            String text = "Премьер-Лига, "+roundRepository.findCurrentRound(new Timestamp(System.currentTimeMillis()))+"-й тур, регулярный сезон, " +dateTime;

            addTextInImageVertical(text, w, 0, 120, 0, 75);
            System.out.println("#2 Размер листа Событий" + matchResult.get().getEvents().size());
            addTextInImageVertical(matchResultRepository.findById(gameId).get().getStatus(), w, 0, 1200, 0, 70);
            System.out.println("#3 Размер листа Событий" + matchResult.get().getEvents().size());

            /**Блок Статистики*/
            addTextInImageVertical(String.valueOf(matchResult.get().getHtShots()), w, 600, 2550, -1, 50);
            addTextInImageVertical("Удары", w, 0, 2550, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtShots()), w, 600, 2550, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtShotsOnTarget()), w, 600, 2650, -1, 50);
            addTextInImageVertical("Удары в створ", w, 0, 2650, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtShotsOnTarget()), w, 600, 2650, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtCornerKicks()), w, 600, 2750, -1, 50);
            addTextInImageVertical("Угловые", w, 0, 2750, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtCornerKicks()), w, 600, 2750, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtFouls()), w, 600, 2850, -1, 50);
            addTextInImageVertical("Нарушения", w, 0, 2850, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtFouls()), w, 600, 2850, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtYellowCards()), w, 600, 2950, -1, 50);
            addTextInImageVertical("Желтые карточки", w, 0, 2950, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtYellowCards()), w, 600, 2950, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtRedCards()), w, 600, 3050, -1, 50);
            addTextInImageVertical("Красные карточки", w, 0, 3050, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getHtRedCards()), w, 600, 3050, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtOffsides()), w, 600, 3150, -1, 50);
            addTextInImageVertical("Офсайды", w, 0, 3150, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtOffsides()), w, 600, 3150, 1, 50);

            addTextInImageVertical(String.valueOf(matchResult.get().getHtPossession()), w, 600, 3250, -1, 50);
            addTextInImageVertical("Владение мячом, %", w, 0, 3250, 0, 50);
            addTextInImageVertical(String.valueOf(matchResult.get().getGtPossession()), w, 600, 3250, 1, 50);

            w.dispose();
            ImageIO.write(bold, "jpg", output);

            //*****

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);


            addImageInImageVertical(matchResult.get().getHt(), g, image2, 470, 250, true);//Лого хозяев
            addImageInImageVertical(matchResult.get().getGt(), g, image2, 470, 250, false);//Лого гостей


            g.dispose();
            ImageIO.write(combined, "png", output);
            ImageIO.write(combined, "png", output2);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeForecastResultToJpg(byte currentRound) throws IOException {

        List<MatchResult> matchResult = matchResultRepository.findAllByNumberOfRound(currentRound).
                stream().sorted((o1, o2) -> (int) (o1.getIdGameOnSoccer365ru()- o2.getIdGameOnSoccer365ru())).collect(Collectors.toList());
        List<Forecaster> allForecasters = forecasterRepository.findAll().stream().sorted((o1, o2) -> o1.getRating()-o2.getRating()).collect(Collectors.toList());

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        File output = new File("D:\\Для работы\\Projects\\AplForecastBot\\rounds\\"+
                currentRound+"\\" + "result.jpg");

        File output2 = new File("D:\\Для работы\\Projects\\AplForecastBot\\rounds\\comonEvents\\" +currentRound +"-result.jpg");



        try {


            currentTime.toString().replaceAll(":","_");



            BufferedImage image = ImageIO.read(inputForecasterResult);
            int imageType = "png".equalsIgnoreCase("jpg") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage bold = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
            Graphics2D w = (Graphics2D) bold.getGraphics();
            w.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            w.drawImage(image, 0, 0, null);
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            w.setComposite(alpha);
            w.setColor(Color.BLACK);


            addTextInImageHorizonal("Результаты " + currentRound+"-ого тура", w, 0, 120, 1744,0, 135);//Результат тура

            int x = 752;
            int y = 662;

            for(MatchResult m:matchResult){

                try {
                    System.out.println(m.getIdGameOnSoccer365ru());
                    System.out.println(m.getDateOfMatch());
                    addTextInImageHorizonal(m.getShortHt(), w, 7, 303, x,-1, 45);//сокращенное название команды хозяев матча
                    addTextInImageHorizonal(m.getShortGt(), w, 7, 303, x,1, 45);//сокращенное название команды гостей матча

                    Date currentDate = new Date (m.getDateOfMatch().getTime());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    String date = dateFormat.format(currentDate);
                    String time = timeFormat.format(currentDate);

                    addTextInImageHorizonal(date, w, 0, 359, x,0, 30);//дата проведения матча
                    addTextInImageHorizonal(time, w, 0, 397, x,0, 30);//время проведения матча

                    addTextInImageHorizonal(String.valueOf(m.getHtGoals()), w, 7, 493, x,-1, 45);//Забитые мячи хозяев матча
                    addTextInImageHorizonal(String.valueOf(m.getGtGoals()), w, 7, 493, x,1, 45);//Забитые мячи гостей матча



                } catch (IOException e) {
                    e.printStackTrace();
                }
                x+=243;
            }

            x = 752;
            y = 659;

            for(Forecaster f:allForecasters){
                int xForForecaster = 752;
                int sumPointsOfRound=0;
                addTextInImageHorizonal(f.getName(), w, 215, y, 0,2, 55);
                for(MatchResult matchResult1:matchResult){
                    sumPointsOfRound+=forecastRepository.findByMatchResult_IdGameOnSoccer365ruAndForecaster_Id(matchResult1.getIdGameOnSoccer365ru(), f.getId()).getPoint();
                }
                for(MatchResult m:matchResult){
                    addTextInImageHorizonal(String.valueOf(forecastRepository.
                                findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getForecastHomeTeamGoals()),
                        w, 7, y, xForForecaster,-1, 55);//Количество мячей хозяев матча спрогнозированных прогнозистом

                    addTextInImageHorizonal(String.valueOf(forecastRepository.
                                findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getForecastGuestTeamGoals()),
                        w, 7, y, xForForecaster,1, 55);//Количество мячей гостей матча спрогнозированных прогнозистом


                    addTextInImageHorizonal(String.valueOf(sumPointsOfRound), w, 0, y, 3156,0, 55);
                    addTextInImageHorizonal(String.valueOf(forecastRepository.sumPoints(f.getId())), w, 0, y, 3366,0, 55);

                    xForForecaster+=243;
                }

                y+=163;
            }


            w.dispose();
            ImageIO.write(bold, "jpg", output);

            /** Блок нанесения изображений на конечное изображение*/

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);

            x = 752;
            y = 604;
            for(Forecaster f:allForecasters){
                int xForForecaster = 752;
                if(f.getArrow()==up){
                    addImageInImageHorizontal("up", g, image2, 130, y, 0);//Arrow Up
                }
                if(f.getArrow()==down){
                    addImageInImageHorizontal("down", g, image2, 130, y, 0);//Arrow Down
                }
                if(f.getArrow()==right){
                    addImageInImageHorizontal("right", g, image2, 130, y, 0);//Arrow Right
                }


                for(MatchResult m:matchResult){

                    if(forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getPoint()!=0){
                        if(forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getPoint()==1){
                            addImageInImageHorizontal("1", g, image2, xForForecaster+82, y-35, 0);//Point +1
                        }
                        if(forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getPoint()==4){
                            addImageInImageHorizontal("4", g, image2, xForForecaster+82, y-35, 0);//Point +4
                        }
                        if(forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getPoint()==9){
                            addImageInImageHorizontal("9", g, image2, xForForecaster+82, y-35, 0);//Point +9
                        }

                    }


                    xForForecaster+=243;
                }

                y+=163;
            }

            g.dispose();
            ImageIO.write(combined, "png", output);
            ImageIO.write(combined, "png", output2);

        }
        catch (NullPointerException e) {

        }

    }
    public void writeForecastBeforeTheRoundToJpg(byte currentRound) throws IOException {

        List<MatchResult> matchResult = matchResultRepository.findAllByNumberOfRound(currentRound).
                stream().sorted((o1, o2) -> (int) (o1.getIdGameOnSoccer365ru()- o2.getIdGameOnSoccer365ru())).collect(Collectors.toList());
        List<Forecaster> allForecasters = forecasterRepository.findAll().stream().sorted((o1, o2) -> o1.getRating()-o2.getRating()).collect(Collectors.toList());

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        File output = new File("D:\\Для работы\\Projects\\AplForecastBot\\rounds\\"+
                currentRound+"\\" + "forecast.jpg");

        File output2 = new File("D:\\Для работы\\Projects\\AplForecastBot\\rounds\\comonEvents\\" +currentRound +"-forecast.jpg");



        try {


            currentTime.toString().replaceAll(":","_");



            BufferedImage image = ImageIO.read(inputForecasterResult);
            int imageType = "png".equalsIgnoreCase("jpg") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage bold = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
            Graphics2D w = (Graphics2D) bold.getGraphics();
            w.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            w.drawImage(image, 0, 0, null);
            AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            w.setComposite(alpha);
            w.setColor(Color.BLACK);


            addTextInImageHorizonal("Прогнозы на " + currentRound+"-й тур", w, 0, 120, 1744,0, 135);//Результат тура

            int x = 752;
            int y = 662;

            for(MatchResult m:matchResult){

                try {
                    System.out.println(m.getIdGameOnSoccer365ru());
                    System.out.println(m.getDateOfMatch());
                    addTextInImageHorizonal(m.getShortHt(), w, 7, 303, x,-1, 45);//сокращенное название команды хозяев матча
                    addTextInImageHorizonal(m.getShortGt(), w, 7, 303, x,1, 45);//сокращенное название команды гостей матча

                    Date currentDate = new Date (m.getDateOfMatch().getTime());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    String date = dateFormat.format(currentDate);
                    String time = timeFormat.format(currentDate);

                    addTextInImageHorizonal(date, w, 0, 359, x,0, 30);//дата проведения матча
                    addTextInImageHorizonal(time, w, 0, 397, x,0, 30);//время проведения матча

                    addTextInImageHorizonal("-", w, 7, 493, x,-1, 45);//Забитые мячи хозяев матча
                    addTextInImageHorizonal("-", w, 7, 493, x,1, 45);//Забитые мячи гостей матча



                } catch (IOException e) {
                    e.printStackTrace();
                }
                x+=243;
            }

            x = 752;
            y = 659;

            for(Forecaster f:allForecasters){
                int xForForecaster = 752;
                addTextInImageHorizonal(f.getName(), w, 215, y, 0,2, 55);

                for(MatchResult m:matchResult){
                    addTextInImageHorizonal(String.valueOf(forecastRepository.
                                findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getForecastHomeTeamGoals()),
                        w, 7, y, xForForecaster,-1, 55);//Количество мячей хозяев матча спрогнозированных прогнозистом

                    addTextInImageHorizonal(String.valueOf(forecastRepository.
                                findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(f,m.getIdGameOnSoccer365ru()).get().getForecastGuestTeamGoals()),
                        w, 7, y, xForForecaster,1, 55);//Количество мячей гостей матча спрогнозированных прогнозистом

                    addTextInImageHorizonal("-", w, 0, y, 3156,0, 55);
                    addTextInImageHorizonal(String.valueOf(forecastRepository.sumPoints(f.getId())), w, 0, y, 3366,0, 55);

                    xForForecaster+=243;
                }

                y+=163;
            }


            w.dispose();
            ImageIO.write(bold, "jpg", output);

            /** Блок нанесения изображений на конечное изображение*/

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);

            x = 752;
            y = 604;
            for(Forecaster f:allForecasters){
                int xForForecaster = 752;
                if(f.getArrow()==up){
                    addImageInImageHorizontal("up", g, image2, 130, y, 0);//Arrow Up
                }
                if(f.getArrow()==down){
                    addImageInImageHorizontal("down", g, image2, 130, y, 0);//Arrow Down
                }
                if(f.getArrow()==right){
                    addImageInImageHorizontal("right", g, image2, 130, y, 0);//Arrow Right
                }


                y+=163;
            }

            g.dispose();
            ImageIO.write(combined, "png", output);
            ImageIO.write(combined, "png", output2);

        }
        catch (NullPointerException e) {

        }

    }
}
