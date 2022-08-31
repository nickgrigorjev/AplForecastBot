package com.example.aplforecastbot.service;

import com.example.aplforecastbot.entities.Event;
import com.example.aplforecastbot.entities.MatchResult;
import com.example.aplforecastbot.repository.EventRepository;
import com.example.aplforecastbot.repository.MatchResultRepository;
import com.example.aplforecastbot.repository.RoundRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WriterToJpgService {
    private static File input = new File("D:\\Для работы\\Projects\\DownloadingImages\\image.jpg");
    private static void addImageInImage(String teamName, Graphics g, BufferedImage image, int x, int y, boolean isHomeTeam) throws IOException {
        BufferedImage overLay = ImageIO.read(new File("D:\\Для работы\\Projects\\DownloadingImages\\teams\\"+teamName+".png"));
        if(isHomeTeam){x= 1181-x-(overLay.getWidth());}
        else x = 1181+ x ;
        g.drawImage(overLay,x,y,null);

    }
    private static void addTextInImage(String text, Graphics2D w, int x, int y, int position,int fontSize) throws IOException {
        w.setFont(new Font(Font.SANS_SERIF, Font.BOLD,fontSize));
        FontMetrics fontMetrics = w.getFontMetrics();
        Rectangle2D rect = fontMetrics.getStringBounds(text, w);
        if(position==-1){x= (1181-(int)rect.getWidth()-x);}
        if(position==0){x= (1181-((int)rect.getWidth()/2)-x);}
        if(position==1){x=x+1181;}
        w.drawString(text,x,y);
    }

    private MatchResultRepository matchResultRepository;
    private EventRepository eventRepository;
    private RoundRepository roundRepository;


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

            addTextInImage(matchResult.get().getGt(), w, 90, 1000, 1, 135);//Команда гостей
            addTextInImage(matchResult.get().getHt(), w, 90, 1000, -1, 135);//Команда хозяев

            addTextInImage(String.valueOf(matchResult.get().getGtGoals()), w, 80, 750, 1, 500);//Счет гостей
            addTextInImage(String.valueOf(matchResult.get().getHtGoals()), w, 80, 750, -1, 500);//Счет хозяев


            addTextInImage(eventList.get(0).getEventDate(), w, 0, 120, 0, 75);
            addTextInImage(eventList.get(0).getEventStatus(), w, 0, 1200, 0, 70);



            for (int i = 0; i < eventList.size(); i++) {
                try {
                    String[] eventsHomeTeam = eventList.get(i).getHtEvent().split(";");
                    addTextInImage(eventsHomeTeam[0], w, 150, startYPositionsForEvents, -1, 60);

                } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
                }

                addTextInImage(eventList.get(i).getMinEvent(), w, 0, startYPositionsForEvents, 0, 60);

                try {
                    String[] eventsATeam = eventList.get(i).getGtEvent().split(";");
                    addTextInImage(eventsATeam[1], w, 150, startYPositionsForEvents, 1, 60);
                } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println(e.getMessage());
                }

                startYPositionsForEvents += 90;
            }
            /**Блок Статистики*/
            addTextInImage(String.valueOf(matchResult.get().getHtShots()), w, 600, 2550, -1, 50);
            addTextInImage("Удары", w, 0, 2550, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtShots()), w, 600, 2550, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtShotsOnTarget()), w, 600, 2650, -1, 50);
            addTextInImage("Удары в створ", w, 0, 2650, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtShotsOnTarget()), w, 600, 2650, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtCornerKicks()), w, 600, 2750, -1, 50);
            addTextInImage("Угловые", w, 0, 2750, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtCornerKicks()), w, 600, 2750, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtFouls()), w, 600, 2850, -1, 50);
            addTextInImage("Нарушения", w, 0, 2850, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtFouls()), w, 600, 2850, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtYellowCards()), w, 600, 2950, -1, 50);
            addTextInImage("Желтые карточки", w, 0, 2950, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtYellowCards()), w, 600, 2950, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtRedCards()), w, 600, 3050, -1, 50);
            addTextInImage("Красные карточки", w, 0, 3050, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtRedCards()), w, 600, 3050, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtOffsides()), w, 600, 3150, -1, 50);
            addTextInImage("Офсайды", w, 0, 3150, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtOffsides()), w, 600, 3150, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtPossession()), w, 600, 3250, -1, 50);
            addTextInImage("Владение мячом, %", w, 0, 3250, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtPossession()), w, 600, 3250, 1, 50);

            w.dispose();
            ImageIO.write(bold, "jpg", output);

            //*****

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);


            addImageInImage(matchResult.get().getHt(), g, image2, 470, 250, true);//Лого хозяев
            addImageInImage(matchResult.get().getGt(), g, image2, 470, 250, false);//Лого гостей


            startYPositionsForEvents = 1300;
            for (int i = 0; i < eventList.size(); i++) {
                try {
                    String[] eventsHomeTeam = eventList.get(i).getHtEvent().split(";");

                    switch (eventsHomeTeam[1]) {
                        case "goal":
                            addImageInImage(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "yellowCard":
                            addImageInImage(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "yellowRed":
                            addImageInImage(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "ownGoal":
                            addImageInImage(eventsHomeTeam[1], g, image, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "penalty":
                            addImageInImage(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "redCard":
                            addImageInImage(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
                            break;
                        case "mispen":
                            addImageInImage(eventsHomeTeam[1], g, image2, 70, startYPositionsForEvents - 50, true);
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
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "yellowCard":
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "yellowRed":
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "ownGoal":
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "penalty":
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "redCard":
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
                            break;
                        case "mispen":
                            addImageInImage(eventsATeam[0], g, image2, 70, startYPositionsForEvents - 50, false);
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

            addTextInImage(matchResult.get().getGt(), w, 90, 1000, 1, 135);//Команда гостей
            addTextInImage(matchResult.get().getHt(), w, 90, 1000, -1, 135);//Команда хозяев

            addTextInImage(String.valueOf(matchResult.get().getGtGoals()), w, 80, 750, 1, 500);//Счет гостей
            addTextInImage(String.valueOf(matchResult.get().getHtGoals()), w, 80, 750, -1, 500);//Счет хозяев

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");
            String dateTime = matchResult.get().getDateOfMatch().toLocalDateTime().format(formatter);
            String text = "Премьер-Лига, "+roundRepository.findCurrentRound(new Timestamp(System.currentTimeMillis()))+"-й тур, регулярный сезон," +dateTime;

            addTextInImage(text, w, 0, 120, 0, 75);

//            addTextInImage(matchResult.get().getEvents().get(0).getEventDate(), w, 0, 120, 0, 75);
            addTextInImage(matchResult.get().getEvents().get(0).getEventStatus(), w, 0, 1200, 0, 70);

            addTextInImage("-", w, 600, 2550, -1, 50);
            addTextInImage("Удары", w, 0, 2550, 0, 50);
            addTextInImage("-", w, 600, 2550, 1, 50);

            addTextInImage("-", w, 600, 2650, -1, 50);
            addTextInImage("Удары в створ", w, 0, 2650, 0, 50);
            addTextInImage("-", w, 600, 2650, 1, 50);

            addTextInImage("-", w, 600, 2750, -1, 50);
            addTextInImage("Угловые", w, 0, 2750, 0, 50);
            addTextInImage("-", w, 600, 2750, 1, 50);

            addTextInImage("-", w, 600, 2850, -1, 50);
            addTextInImage("Нарушения", w, 0, 2850, 0, 50);
            addTextInImage("-", w, 600, 2850, 1, 50);

            addTextInImage("-", w, 600, 2950, -1, 50);
            addTextInImage("Желтые карточки", w, 0, 2950, 0, 50);
            addTextInImage("-", w, 600, 2950, 1, 50);

            addTextInImage("-", w, 600, 3050, -1, 50);
            addTextInImage("Красные карточки", w, 0, 3050, 0, 50);
            addTextInImage("-", w, 600, 3050, 1, 50);

            addTextInImage("-", w, 600, 3150, -1, 50);
            addTextInImage("Офсайды", w, 0, 3150, 0, 50);
            addTextInImage("-", w, 600, 3150, 1, 50);

            addTextInImage("-", w, 600, 3250, -1, 50);
            addTextInImage("Владение мячом, %", w, 0, 3250, 0, 50);
            addTextInImage("-", w, 600, 3250, 1, 50);

            w.dispose();
            ImageIO.write(bold, "jpg", output);

            //*****

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);


            addImageInImage(matchResult.get().getHt(), g, image2, 470, 250, true);//Лого хозяев
            addImageInImage(matchResult.get().getGt(), g, image2, 470, 250, false);//Лого гостей


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

            addTextInImage(matchResult.get().getGt(), w, 90, 1000, 1, 135);//Команда гостей
            addTextInImage(matchResult.get().getHt(), w, 90, 1000, -1, 135);//Команда хозяев

            addTextInImage(String.valueOf(matchResult.get().getGtGoals()), w, 80, 750, 1, 500);//Счет гостей
            addTextInImage(String.valueOf(matchResult.get().getHtGoals()), w, 80, 750, -1, 500);//Счет хозяев

            System.out.println("#1 Размер листа Событий " + matchResult.get().getEvents().size());


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            String dateTime = matchResult.get().getDateOfMatch().toLocalDateTime().format(formatter);
            String text = "Премьер-Лига, "+roundRepository.findCurrentRound(new Timestamp(System.currentTimeMillis()))+"-й тур, регулярный сезон, " +dateTime;

            addTextInImage(text, w, 0, 120, 0, 75);
            System.out.println("#2 Размер листа Событий" + matchResult.get().getEvents().size());
            addTextInImage(matchResultRepository.findById(gameId).get().getStatus(), w, 0, 1200, 0, 70);
            System.out.println("#3 Размер листа Событий" + matchResult.get().getEvents().size());

            /**Блок Статистики*/
            addTextInImage(String.valueOf(matchResult.get().getHtShots()), w, 600, 2550, -1, 50);
            addTextInImage("Удары", w, 0, 2550, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtShots()), w, 600, 2550, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtShotsOnTarget()), w, 600, 2650, -1, 50);
            addTextInImage("Удары в створ", w, 0, 2650, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtShotsOnTarget()), w, 600, 2650, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtCornerKicks()), w, 600, 2750, -1, 50);
            addTextInImage("Угловые", w, 0, 2750, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtCornerKicks()), w, 600, 2750, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtFouls()), w, 600, 2850, -1, 50);
            addTextInImage("Нарушения", w, 0, 2850, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtFouls()), w, 600, 2850, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtYellowCards()), w, 600, 2950, -1, 50);
            addTextInImage("Желтые карточки", w, 0, 2950, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtYellowCards()), w, 600, 2950, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtRedCards()), w, 600, 3050, -1, 50);
            addTextInImage("Красные карточки", w, 0, 3050, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getHtRedCards()), w, 600, 3050, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtOffsides()), w, 600, 3150, -1, 50);
            addTextInImage("Офсайды", w, 0, 3150, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtOffsides()), w, 600, 3150, 1, 50);

            addTextInImage(String.valueOf(matchResult.get().getHtPossession()), w, 600, 3250, -1, 50);
            addTextInImage("Владение мячом, %", w, 0, 3250, 0, 50);
            addTextInImage(String.valueOf(matchResult.get().getGtPossession()), w, 600, 3250, 1, 50);

            w.dispose();
            ImageIO.write(bold, "jpg", output);

            //*****

            BufferedImage image2 = ImageIO.read(output);
            int width = Math.max(image.getWidth(), 0);
            int height = Math.max(image.getHeight(), 0);
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = combined.getGraphics();
            g.drawImage(image2, 0, 0, null);


            addImageInImage(matchResult.get().getHt(), g, image2, 470, 250, true);//Лого хозяев
            addImageInImage(matchResult.get().getGt(), g, image2, 470, 250, false);//Лого гостей


            g.dispose();
            ImageIO.write(combined, "png", output);
            ImageIO.write(combined, "png", output2);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
