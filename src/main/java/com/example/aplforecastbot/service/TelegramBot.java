package com.example.aplforecastbot.service;

import com.example.aplforecastbot.config.BotConfig;
import com.example.aplforecastbot.entities.*;
import com.example.aplforecastbot.entities.User;
import com.example.aplforecastbot.repository.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.*;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String directoryForSendGroupMessage = "C:\\Users\\Николай\\Desktop\\Новая папка (2)";
    private static final String directoryForSingleSendMessage = "C:\\Users\\Николай\\Downloads\\c7088df551317fb2915a5f15f10ae8ee.jpg";

    final BotConfig config;
//    private TelegramBot(BotConfig config){
//        this.config = config;
//        List<BotCommand> listOfCommands = new ArrayList<>();
//        listOfCommands.add(new BotCommand("/start","get a welcome message"));
//        listOfCommands.add(new BotCommand("/mydata","get your data stored"));
//        listOfCommands.add(new BotCommand("/deletedata","delete my data"));
//        listOfCommands.add(new BotCommand("/help","info how tp use this bot"));
//        listOfCommands.add(new BotCommand("/settings","set your preferences"));
//        try{
//            this.execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(),null));
//        }catch (TelegramApiException e){
//            log.error("Errors setting bot's command list: "+ e.getMessage());
//        }
//    }
//    private static class TelegramBotHolder {
//        public static final TelegramBot HOLDER_INSTANCE = new TelegramBot(config);
//    }


    @Autowired
    private MatchResultRepository matchResultRepository;
    @Autowired
    private UserRepository userRepository;

    private RoundRepository roundRepository;
    private ForecastRepository forecastRepository;
    private ForecasterRepository forecasterRepository;
    private MessageIdsRepository msgRepository;

    @Autowired
    public void setMsgRepository(MessageIdsRepository msgRepository) {
        this.msgRepository = msgRepository;
    }
    //    private ForecastsForecastersRepository ffRepository;

//    @Autowired
//    public void setFfRepository(ForecastsForecastersRepository ffRepository) {
//        this.ffRepository = ffRepository;
//    }

    @Autowired
    public void setForecasterRepository(ForecasterRepository forecasterRepository) {
        this.forecasterRepository = forecasterRepository;
    }

    @Autowired
    public void setForecastRepository(ForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }

    @Autowired
    public void setRoundRepository(RoundRepository roundRepository) {
        this.roundRepository = roundRepository;
    }


    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message \n\n"+
            "Type /mydata to see data stored about yourself\n\n"+
            "Type /help to see this message again";

    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata","get your data stored"));
        listOfCommands.add(new BotCommand("/deletedata","delete my data"));
        listOfCommands.add(new BotCommand("/help","info how tp use this bot"));
        listOfCommands.add(new BotCommand("/settings","set your preferences"));
        try{
            this.execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
            log.error("Errors setting bot's command list: "+ e.getMessage());
        }
    }
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage()&&update.getMessage().hasText()){

            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            System.out.println(update.getMessage().getMediaGroupId());
            switch(message){
                case "/start":
//                    try {
//                        addAllMatches();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    if(!msgRepository.findByForecaster_Id(update.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(update.getMessage().getMessageId());
                        messageIds.setForecastNumbersMsgId(0);
                        messageIds.setId(msgRepository.count()+1);
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastNumbersMsgId());
                        messageIds.setId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);


                        deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }
                    registerUser(update.getMessage());
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                    if(!msgRepository.findByForecaster_Id(update.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(update.getMessage().getMessageId());
                        messageIds.setForecastNumbersMsgId(0);
                        messageIds.setId(msgRepository.count()+1);
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastNumbersMsgId());
                        messageIds.setId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case "/help":
                    sendMessage(chatId,HELP_TEXT);
                    break;

                case "statistic":
//                    sendFormForForecast(chatId,update.getMessage().getChat().getFirstName());
//                    sendSinglePhoto(chatId,directoryForSingleSendMessage);

                    if(!msgRepository.findByForecaster_Id(update.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(update.getMessage().getMessageId());
                        messageIds.setForecastNumbersMsgId(0);
                        messageIds.setId(msgRepository.count()+1);
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastNumbersMsgId());
                        messageIds.setId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);


                        deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }
                    testKeyboard(chatId);
                    if(!msgRepository.findByForecaster_Id(update.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(update.getMessage().getMessageId());
                        messageIds.setForecastNumbersMsgId(0);
                        messageIds.setId(msgRepository.count()+1);
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastNumbersMsgId());
                        messageIds.setId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);


                        deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;

                case "Регистрация":
                    registerForecaster(update.getMessage());
                    sendMessage(chatId,update.getMessage().getChat().getFirstName()+", регистрация прошла успешно, добро пожаловать на турнир! Теперь можно составить прогноз на будущий тур.\nУдачи!");
                    break;
                case "Мой прогноз":

                    System.out.println("Сюда приходим после клика на Мой прогноз");
                    if(!msgRepository.findByForecaster_Id(update.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(update.getMessage().getMessageId());
                        messageIds.setForecastNumbersMsgId(0);
                        messageIds.setId(msgRepository.count()+1);
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastNumbersMsgId());
                        messageIds.setId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);


                        deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }
                    sendInlineKeyBoardMessage(chatId);
                    if(!msgRepository.findByForecaster_Id(update.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(update.getMessage().getMessageId());
                        messageIds.setForecastNumbersMsgId(0);
                        messageIds.setId(msgRepository.count()+1);
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getForecastNumbersMsgId());
                        messageIds.setId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(update.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(update.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);


                        deleteMessage = new DeleteMessage(String.valueOf(update.getMessage().getChatId()),update.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case "Удалить сообщения":
                    forecasterRepository.findAll().forEach(forecaster -> deleteForecasterMessage(forecaster.getId(),msgRepository.findByForecaster_Id(forecaster.getId()).get().getForecastMsgId()));
                    forecasterRepository.findAll().forEach(forecaster -> deleteForecasterMessage(forecaster.getId(),msgRepository.findByForecaster_Id(forecaster.getId()).get().getForecastNumbersMsgId()));
                    break;
                case "Результаты":
                    System.out.println("Message *********");
                    break;


                default:sendMessage(chatId,"Извини, Коля меня ещё пока не научил обрабатывать такие запросы");
            }

        }else if(update.hasCallbackQuery()){
            handlerMessage(update);
        }
    }

    private void handlerMessage(Update update){
        CallbackQuery callbackQuery = update.getCallbackQuery();
        System.out.println(callbackQuery.getMessage().getText());

        if(callbackQuery.getData().equals("/delete")){
            System.out.println("MessageId - "+callbackQuery.getMessage().getMessageId());
            System.out.println("InlineMessageId - "+callbackQuery.getInlineMessageId());
            System.out.println("CallbackQueryId - "+callbackQuery.getId());
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),callbackQuery.getMessage().getMessageId());

            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            testKeyboard(callbackQuery.getMessage().getChatId());
        }
        if(callbackQuery.getData().contains("/")){
            String[] data = callbackQuery.getData().split("/");
            System.out.println(data.length);


            if(data.length==2){


                System.out.println("MessageId - "+callbackQuery.getMessage().getMessageId());

                if(!msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).isPresent()){
                    MessageIds messageIds = new MessageIds();
                    messageIds.setForecastMsgId(callbackQuery.getMessage().getMessageId());
                    messageIds.setForecastNumbersMsgId(0);
                    messageIds.setId(msgRepository.count()+1);
                    messageIds.setPostMatchResultsMsgId(0);
                    messageIds.setForecaster(forecasterRepository.findById(callbackQuery.getMessage().getChatId()).get());
                    msgRepository.save(messageIds);

                    DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),callbackQuery.getMessage().getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                }else{
                    System.out.println();
                    DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getForecastMsgId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    MessageIds messageIds = new MessageIds();
                    messageIds.setForecastMsgId(callbackQuery.getMessage().getMessageId()+1);
                    messageIds.setForecastNumbersMsgId(messageIds.getForecastNumbersMsgId());
                    messageIds.setId(msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getId());
                    messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                    messageIds.setForecaster(forecasterRepository.findById(callbackQuery.getMessage().getChatId()).get());
                    msgRepository.save(messageIds);

                    deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),callbackQuery.getMessage().getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }

                sendNumbers(callbackQuery.getMessage().getChatId(),callbackQuery.getData());

            }


            if(data.length==3){
                try{


                    if(!msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).isPresent()){
                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(0);
                        messageIds.setForecastNumbersMsgId(callbackQuery.getMessage().getMessageId());
                        messageIds.setId(msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(0);
                        messageIds.setForecaster(forecasterRepository.findById(callbackQuery.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),callbackQuery.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                    }else{
                        System.out.println();
                        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getForecastMsgId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        MessageIds messageIds = new MessageIds();
                        messageIds.setForecastMsgId(msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getForecastMsgId()+1);
                        messageIds.setForecastNumbersMsgId(callbackQuery.getMessage().getMessageId());
                        messageIds.setId(msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getId());
                        messageIds.setPostMatchResultsMsgId(msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getPostMatchResultsMsgId());
                        messageIds.setForecaster(forecasterRepository.findById(callbackQuery.getMessage().getChatId()).get());
                        msgRepository.save(messageIds);

                        deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),callbackQuery.getMessage().getMessageId());
                        try {
                            execute(deleteMessage);
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    }

                    Forecast forecast = new Forecast();
                    Forecaster forecaster = forecasterRepository.findById(callbackQuery.getMessage().getChatId()).get();
                    MatchResult matchResult = matchResultRepository.findById(Long.parseLong(data[0])).get();

                    forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
                    forecast.setPoint(0);
                    forecast.setForecaster(forecaster);
                    forecast.setMatchResult(matchResult);
                    forecast.setId(forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,Long.parseLong(data[0])).get().getId());

                    if(data[1].equals("home")){
                        forecast.setForecastHomeTeamGoals(Byte.parseByte(data[2]));
                        forecast.setForecastGuestTeamGoals(forecastRepository.
                                findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,Long.parseLong(data[0])).get().getForecastGuestTeamGoals());
                    }
                    if(data[1].equals("guest")){
                        forecast.setForecastHomeTeamGoals(forecastRepository.
                                findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,Long.parseLong(data[0])).get().getForecastHomeTeamGoals());
                        forecast.setForecastGuestTeamGoals(Byte.parseByte(data[2]));
                    }

                    forecastRepository.save(forecast);

                }catch(NoSuchElementException e){
                    log.error("Не найдено элементов " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
                }

                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),callbackQuery.getMessage().getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }


//                DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(callbackQuery.getMessage().getChatId()),msgRepository.findByForecaster_Id(callbackQuery.getMessage().getChatId()).get().getForecastNumbersMsgId());
//
//                try {
//                    execute(deleteMessage);
//                } catch (TelegramApiException e) {
//
//                }

                sendInlineKeyBoardMessage(callbackQuery.getMessage().getChatId());

            }
        }

    }

    private void deleteForecasterMessage(Long forecasterId, int messageId){
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(forecasterId),messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addAllMatches() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("D:\\Для работы\\Projects\\AplForecastBot\\Matches.txt"));
        Calendar calendar;
        Timestamp dateOfMatch;
        int count = 1;
        while(reader.ready()){
            String s = reader.readLine();
            String[] data = s.split(",");
            Forecast forecast = new Forecast();
            forecast.setId((long) count);
            forecastRepository.save(forecast);
            count++;
                log.info("Успешно внесено в базу " + count + " записей");
        }
        System.out.println("Успешно внесено в базу " + count + " записей");
        reader.close();
    }

    private void registerUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }
    private void registerForecaster(Message msg) {
        if(forecastRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            Forecaster forecaster = new Forecaster();
            forecaster.setId(chatId);
            forecaster.setName(chat.getFirstName());
            forecaster.setPoints(0);
            forecaster.setRating(0);
            forecaster.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            forecasterRepository.save(forecaster);
            log.info("forecaster registered: " + forecaster);
        }
    }

    private void startCommandReceived(long chatId,String name){

//        String answer = "Hi, " + name +", nice to meet you";
        String answer = EmojiParser.parseToUnicode("Привет, " + name  + " \uD83D\uDD96");
        log.info("Replied to user " + name);
        sendMessage(chatId,answer);
    }
    private void forecastCommandReceived(long chatId,String name){

//        String answer = "Hi, " + name +", nice to meet you";
        String answer = "Hello " + name +" please take forecast";
        log.info("Replied to user " + name);
        sendMessage(chatId,answer);
    }
    public void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        if(forecasterRepository.existsById(chatId)){
            row.add("Мой прогноз");
        }
        row.add("statistic");
        keyboardRows.add(row);
        row = new KeyboardRow();
        if(!forecasterRepository.existsById(chatId)){
            row.add("Регистрация");
        }
        row.add("delete my data");
        if(forecasterRepository.findById(chatId).get().getId()==647528114L){
            row.add("Удалить сообщения");
        }
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setOneTimeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try{
            execute(message);
        }catch(TelegramApiException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) );
        }
    }

    public void sendInlineKeyBoardMessage(long chatId) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
//        Timestamp currentTime = new Timestamp(122, 7, 29, 12, 30, 0, 0);
        List<MatchResult> matchResultList = new ArrayList<>(matchResultRepository.findAllByNumberOfRound((byte) (roundRepository.findCurrentRound(currentTime)+1)));
//        List<MatchResult> matchResultList = new ArrayList<>(matchResultRepository.findAllByNumberOfRound(roundRepository.findCurrentRound(currentTime)));
        SendMessage message = new SendMessage();
//        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton6 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton7 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton8 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton9 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton10 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton11 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton12 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton13 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton14 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton15 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton16 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton17 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton18 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton19 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton20 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton21 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton22 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton23 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton24 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton25 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton26 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton27 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton28 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton29 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton30 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton31 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton32 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton33 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton34 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton35 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton36 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton37 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton38 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton39 = new InlineKeyboardButton();
        InlineKeyboardButton inlineKeyboardButton40 = new InlineKeyboardButton();

        /**1-я пара*/
        inlineKeyboardButton1.setText(matchResultList.get(0).getHt());
        inlineKeyboardButton4.setText(matchResultList.get(0).getGt());
        Long id = matchResultList.get(0).getIdGameOnSoccer365ru();
        Forecaster forecaster = forecasterRepository.findById(chatId).get();
        Forecast forecast;
        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton2.setText("-");
            inlineKeyboardButton3.setText("-");
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton2.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));
            inlineKeyboardButton3.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));
        }

        inlineKeyboardButton1.setCallbackData(matchResultList.get(0).getHt());
        inlineKeyboardButton2.setCallbackData(matchResultList.get(0).getIdGameOnSoccer365ru()+"/home");
        inlineKeyboardButton3.setCallbackData(matchResultList.get(0).getIdGameOnSoccer365ru()+"/guest");
        inlineKeyboardButton4.setCallbackData(matchResultList.get(0).getGt());

        /**2-я пара*/

        inlineKeyboardButton5.setText(matchResultList.get(1).getHt());                                                    // *********
        inlineKeyboardButton8.setText(matchResultList.get(1).getGt());                                                    // *********
        id = matchResultList.get(1).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton6.setText("-");                                                                           // *********
            inlineKeyboardButton7.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton6.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton7.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton5.setCallbackData(matchResultList.get(1).getHt());                                             // *********
        inlineKeyboardButton6.setCallbackData(matchResultList.get(1).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton7.setCallbackData(matchResultList.get(1).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton8.setCallbackData(matchResultList.get(1).getGt());                                             // *********

        /**3-я пара*/

        inlineKeyboardButton9.setText(matchResultList.get(2).getHt());                                                    // *********
        inlineKeyboardButton12.setText(matchResultList.get(2).getGt());                                                    // *********
        id = matchResultList.get(2).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton10.setText("-");                                                                           // *********
            inlineKeyboardButton11.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton10.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton11.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton9.setCallbackData(matchResultList.get(2).getHt());                                             // *********
        inlineKeyboardButton10.setCallbackData(matchResultList.get(2).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton11.setCallbackData(matchResultList.get(2).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton12.setCallbackData(matchResultList.get(2).getGt());                                             // *********


        /**4-я пара*/

        inlineKeyboardButton13.setText(matchResultList.get(3).getHt());                                                    // *********
        inlineKeyboardButton16.setText(matchResultList.get(3).getGt());                                                    // *********
        id = matchResultList.get(3).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton14.setText("-");                                                                           // *********
            inlineKeyboardButton15.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton14.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton15.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton13.setCallbackData(matchResultList.get(3).getHt());                                             // *********
        inlineKeyboardButton14.setCallbackData(matchResultList.get(3).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton15.setCallbackData(matchResultList.get(3).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton16.setCallbackData(matchResultList.get(3).getGt());                                             // *********


        /**5-я пара*/

        inlineKeyboardButton17.setText(matchResultList.get(4).getHt());                                                    // *********
        inlineKeyboardButton20.setText(matchResultList.get(4).getGt());                                                    // *********
        id = matchResultList.get(4).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton18.setText("-");                                                                           // *********
            inlineKeyboardButton19.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton18.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton19.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton17.setCallbackData(matchResultList.get(4).getHt());                                             // *********
        inlineKeyboardButton18.setCallbackData(matchResultList.get(4).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton19.setCallbackData(matchResultList.get(4).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton20.setCallbackData(matchResultList.get(4).getGt());                                             // *********


        /**6-я пара*/

        inlineKeyboardButton21.setText(matchResultList.get(5).getHt());                                                    // *********
        inlineKeyboardButton24.setText(matchResultList.get(5).getGt());                                                    // *********
        id = matchResultList.get(5).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton22.setText("-");                                                                           // *********
            inlineKeyboardButton23.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton22.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton23.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton21.setCallbackData(matchResultList.get(5).getHt());                                             // *********
        inlineKeyboardButton22.setCallbackData(matchResultList.get(5).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton23.setCallbackData(matchResultList.get(5).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton24.setCallbackData(matchResultList.get(5).getGt());                                             // *********


        /**7-я пара*/

        inlineKeyboardButton25.setText(matchResultList.get(6).getHt());                                                    // *********
        inlineKeyboardButton28.setText(matchResultList.get(6).getGt());                                                    // *********
        id = matchResultList.get(6).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton26.setText("-");                                                                           // *********
            inlineKeyboardButton27.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton26.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton27.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton25.setCallbackData(matchResultList.get(6).getHt());                                             // *********
        inlineKeyboardButton26.setCallbackData(matchResultList.get(6).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton27.setCallbackData(matchResultList.get(6).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton28.setCallbackData(matchResultList.get(6).getGt());                                             // *********

        /**8-я пара*/

        inlineKeyboardButton29.setText(matchResultList.get(7).getHt());                                                    // *********
        inlineKeyboardButton32.setText(matchResultList.get(7).getGt());                                                    // *********
        id = matchResultList.get(7).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton30.setText("-");                                                                           // *********
            inlineKeyboardButton31.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton30.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton31.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton29.setCallbackData(matchResultList.get(7).getHt());                                             // *********
        inlineKeyboardButton30.setCallbackData(matchResultList.get(7).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton31.setCallbackData(matchResultList.get(7).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton32.setCallbackData(matchResultList.get(7).getGt());                                             // *********


        /**9-я пара*/

        inlineKeyboardButton33.setText(matchResultList.get(8).getHt());                                                    // *********
        inlineKeyboardButton36.setText(matchResultList.get(8).getGt());                                                    // *********
        id = matchResultList.get(8).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton34.setText("-");                                                                           // *********
            inlineKeyboardButton35.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton34.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton35.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton33.setCallbackData(matchResultList.get(8).getHt());                                             // *********
        inlineKeyboardButton34.setCallbackData(matchResultList.get(8).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton35.setCallbackData(matchResultList.get(8).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton36.setCallbackData(matchResultList.get(8).getGt());                                             // *********


        /**10-я пара*/

        inlineKeyboardButton37.setText(matchResultList.get(9).getHt());                                                    // *********
        inlineKeyboardButton40.setText(matchResultList.get(9).getGt());                                                    // *********
        id = matchResultList.get(9).getIdGameOnSoccer365ru();
        forecaster = forecasterRepository.findById(chatId).get();

        if(!forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).isPresent()){
            inlineKeyboardButton38.setText("-");                                                                           // *********
            inlineKeyboardButton39.setText("-");                                                                           // *********
            forecast = new Forecast();
            MatchResult matchResult = matchResultRepository.findById(id).get();
            forecast.setDateTimeForecastMade(new Timestamp(System.currentTimeMillis()));
            forecast.setForecaster(forecasterRepository.findById(chatId).get());
            forecast.setForecastHomeTeamGoals((byte)0);
            forecast.setForecastGuestTeamGoals((byte)0);
            forecast.setPoint(0);
            forecast.setMatchResult(matchResult);
            forecast.setId(forecastRepository.count()+1);
            forecastRepository.save(forecast);

        }else{
            forecast = forecastRepository.findForecastByForecasterAndMatchResult_IdGameOnSoccer365ru(forecaster,id).get();
            inlineKeyboardButton38.setText(String.valueOf(forecast.getForecastHomeTeamGoals()));                           // *********
            inlineKeyboardButton39.setText(String.valueOf(forecast.getForecastGuestTeamGoals()));                          // *********
        }

        inlineKeyboardButton37.setCallbackData(matchResultList.get(9).getHt());                                             // *********
        inlineKeyboardButton38.setCallbackData(matchResultList.get(9).getIdGameOnSoccer365ru()+"/home");                    // *********
        inlineKeyboardButton39.setCallbackData(matchResultList.get(9).getIdGameOnSoccer365ru()+"/guest");                   // *********
        inlineKeyboardButton40.setCallbackData(matchResultList.get(9).getGt());                                             // *********

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow3 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow4 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow5 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow6 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow7 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow8 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow9 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow10 = new ArrayList<>();

        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);
        keyboardButtonsRow1.add(inlineKeyboardButton3);
        keyboardButtonsRow1.add(inlineKeyboardButton4);

        keyboardButtonsRow2.add(inlineKeyboardButton5);
        keyboardButtonsRow2.add(inlineKeyboardButton6);
        keyboardButtonsRow2.add(inlineKeyboardButton7);
        keyboardButtonsRow2.add(inlineKeyboardButton8);

        keyboardButtonsRow3.add(inlineKeyboardButton9);
        keyboardButtonsRow3.add(inlineKeyboardButton10);
        keyboardButtonsRow3.add(inlineKeyboardButton11);
        keyboardButtonsRow3.add(inlineKeyboardButton12);

        keyboardButtonsRow4.add(inlineKeyboardButton13);
        keyboardButtonsRow4.add(inlineKeyboardButton14);
        keyboardButtonsRow4.add(inlineKeyboardButton15);
        keyboardButtonsRow4.add(inlineKeyboardButton16);

        keyboardButtonsRow5.add(inlineKeyboardButton17);
        keyboardButtonsRow5.add(inlineKeyboardButton18);
        keyboardButtonsRow5.add(inlineKeyboardButton19);
        keyboardButtonsRow5.add(inlineKeyboardButton20);

        keyboardButtonsRow6.add(inlineKeyboardButton21);
        keyboardButtonsRow6.add(inlineKeyboardButton22);
        keyboardButtonsRow6.add(inlineKeyboardButton23);
        keyboardButtonsRow6.add(inlineKeyboardButton24);

        keyboardButtonsRow7.add(inlineKeyboardButton25);
        keyboardButtonsRow7.add(inlineKeyboardButton26);
        keyboardButtonsRow7.add(inlineKeyboardButton27);
        keyboardButtonsRow7.add(inlineKeyboardButton28);

        keyboardButtonsRow8.add(inlineKeyboardButton29);
        keyboardButtonsRow8.add(inlineKeyboardButton30);
        keyboardButtonsRow8.add(inlineKeyboardButton31);
        keyboardButtonsRow8.add(inlineKeyboardButton32);

        keyboardButtonsRow9.add(inlineKeyboardButton33);
        keyboardButtonsRow9.add(inlineKeyboardButton34);
        keyboardButtonsRow9.add(inlineKeyboardButton35);
        keyboardButtonsRow9.add(inlineKeyboardButton36);

        keyboardButtonsRow10.add(inlineKeyboardButton37);
        keyboardButtonsRow10.add(inlineKeyboardButton38);
        keyboardButtonsRow10.add(inlineKeyboardButton39);
        keyboardButtonsRow10.add(inlineKeyboardButton40);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);
        rowList.add(keyboardButtonsRow3);
        rowList.add(keyboardButtonsRow4);
        rowList.add(keyboardButtonsRow5);
        rowList.add(keyboardButtonsRow6);
        rowList.add(keyboardButtonsRow7);
        rowList.add(keyboardButtonsRow8);
        rowList.add(keyboardButtonsRow9);
        rowList.add(keyboardButtonsRow10);


        inlineKeyboardMarkup.setKeyboard(rowList);
//        editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);
        message.setChatId(String.valueOf(chatId));
        message.setText("Премьер-Лига. Регулярный сезон.\nПрогноз на " + (roundRepository.findCurrentRound(currentTime)+1) + "-й тур");
//        message.setText("Премьер-Лига. Регулярный сезон.\nПрогноз на " + roundRepository.findCurrentRound(currentTime) + "-й тур");
        message.setReplyMarkup(inlineKeyboardMarkup);
        try{
            execute(message);
        }catch(TelegramApiException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) );
        }
    }

    public void testKeyboard(long chatId){
        SendMessage message = new SendMessage();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Button");
        button1.setCallbackData("/delete");
        keyboardRow.add(button1);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardRow);

        keyboardMarkup.setKeyboard(rowList);
        message.setChatId(String.valueOf(chatId));
        message.setText("MessageText");

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void sendSinglePhoto(long chatId, String path){
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        InputFile inputFile = new InputFile();
        File file = new File(path);
        inputFile.setMedia(file,"New Photo");
        sendPhoto.setPhoto(inputFile);
        try{
            execute(sendPhoto);
        }
        catch(TelegramApiException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) );
        }
    }

    public void sendGroupPhoto(long chatId, String path){
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<InputMedia> inputMedia = new ArrayList<>();
        for(File f:files){
            InputMedia input = new InputMediaPhoto();
            input.setMedia(f,f.getName());
//            input.setMediaName(f.getName());
            if(inputMedia.size()<=10){
                inputMedia.add(input);
            }
            StringBuilder caption = new StringBuilder();
            caption.append("Результаты ");
            caption.append(roundRepository.findCurrentRound(currentTime));
            caption.append(" -ого тура");
            inputMedia.get(0).setCaption(String.valueOf(caption));

        }


        MessageId messageId = new MessageId();
        messageId.setMessageId(100000L);

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedias(inputMedia);
        sendMediaGroup.setChatId(String.valueOf(chatId));


        try{
            execute(sendMediaGroup);
        }
        catch(TelegramApiException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) );
        }
    }
    public void sendPreview(long chatId, String path){
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<InputMedia> inputMedia = new ArrayList<>();
        for(File f:files){
            InputMedia input = new InputMediaPhoto();
            input.setMedia(f,f.getName());
            input.setMediaName(f.getName());
            input.setCaption("Премьер-Лига");
            if(inputMedia.size()<=10){
                inputMedia.add(input);
            }
        }

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setMedias(inputMedia);
        sendMediaGroup.setChatId(String.valueOf(chatId));

        try{

            execute(sendMediaGroup);
        }
        catch(TelegramApiException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) );
        }
    }

    private void sendNumbers(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        String[] data = textToSend.split("/");
        if(data[1].equals("home")){message.setText("Сколько забьет "+matchResultRepository.findById(Long.parseLong(data[0])).get().getHt()+"?");}
        if(data[1].equals("guest")){message.setText("Сколько забьет "+matchResultRepository.findById(Long.parseLong(data[0])).get().getGt()+"?");}

        message.setChatId(String.valueOf(chatId));
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRow1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("1");
        inlineKeyboardButton1.setCallbackData(textToSend+"/1");
        inlineKeyboardButtonsRow1.add(inlineKeyboardButton1);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("2");
        inlineKeyboardButton2.setCallbackData(textToSend+"/2");
        inlineKeyboardButtonsRow1.add(inlineKeyboardButton2);

        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("3");
        inlineKeyboardButton3.setCallbackData(textToSend+"/3");
        inlineKeyboardButtonsRow1.add(inlineKeyboardButton3);

        List<InlineKeyboardButton> inlineKeyboardButtonsRow2 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton4.setText("4");
        inlineKeyboardButton4.setCallbackData(textToSend+"/4");
        inlineKeyboardButtonsRow2.add(inlineKeyboardButton4);

        InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
        inlineKeyboardButton5.setText("5");
        inlineKeyboardButton5.setCallbackData(textToSend+"/5");
        inlineKeyboardButtonsRow2.add(inlineKeyboardButton5);

        InlineKeyboardButton inlineKeyboardButton6 = new InlineKeyboardButton();
        inlineKeyboardButton6.setText("6");
        inlineKeyboardButton6.setCallbackData(textToSend+"/6");
        inlineKeyboardButtonsRow2.add(inlineKeyboardButton6);

        List<InlineKeyboardButton> inlineKeyboardButtonsRow3 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton7 = new InlineKeyboardButton();
        inlineKeyboardButton7.setText("7");
        inlineKeyboardButton7.setCallbackData(textToSend+"/7");
        inlineKeyboardButtonsRow3.add(inlineKeyboardButton7);

        InlineKeyboardButton inlineKeyboardButton8 = new InlineKeyboardButton();
        inlineKeyboardButton8.setText("8");
        inlineKeyboardButton8.setCallbackData(textToSend+"/8");
        inlineKeyboardButtonsRow3.add(inlineKeyboardButton8);

        InlineKeyboardButton inlineKeyboardButton9 = new InlineKeyboardButton();
        inlineKeyboardButton9.setText("9");
        inlineKeyboardButton9.setCallbackData(textToSend+"/9");
        inlineKeyboardButtonsRow3.add(inlineKeyboardButton9);

        List<InlineKeyboardButton> inlineKeyboardButtonsRow4 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton10 = new InlineKeyboardButton();
        inlineKeyboardButton10.setText("10");
        inlineKeyboardButton10.setCallbackData(textToSend+"/10");
        inlineKeyboardButtonsRow4.add(inlineKeyboardButton10);

        InlineKeyboardButton inlineKeyboardButton11 = new InlineKeyboardButton();
        inlineKeyboardButton11.setText("11");
        inlineKeyboardButton11.setCallbackData(textToSend+"/11");
        inlineKeyboardButtonsRow4.add(inlineKeyboardButton11);

        InlineKeyboardButton inlineKeyboardButton12 = new InlineKeyboardButton();
        inlineKeyboardButton12.setText("12");
        inlineKeyboardButton12.setCallbackData(textToSend+"/12");
        inlineKeyboardButtonsRow4.add(inlineKeyboardButton12);

        List<InlineKeyboardButton> inlineKeyboardButtonsRow5 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton13 = new InlineKeyboardButton();
        inlineKeyboardButton13.setText("0");
        inlineKeyboardButton13.setCallbackData(textToSend+"/0");
        inlineKeyboardButtonsRow5.add(inlineKeyboardButton13);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(inlineKeyboardButtonsRow1);
        rowList.add(inlineKeyboardButtonsRow2);
        rowList.add(inlineKeyboardButtonsRow3);
        rowList.add(inlineKeyboardButtonsRow4);
        rowList.add(inlineKeyboardButtonsRow5);
        keyboardMarkup.setKeyboard(rowList);
        message.setChatId(String.valueOf(chatId));
        message.setReplyMarkup(keyboardMarkup);


        try{
            execute(message);
        }catch(TelegramApiException e){
            log.error("Error occurred: " + e.getMessage()+"; " + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) );
        }
    }



    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }
}
