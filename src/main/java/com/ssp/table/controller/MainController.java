package com.ssp.table.controller;


import com.ssp.table.questions.*;

import org.springframework.beans.factory.annotation.*;

import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;


import java.text.*;
import java.util.*;
import javax.servlet.*;


@Controller
public class MainController {

    @Autowired
    private DataBaseDAO dataBaseDAO;

    @Autowired
    private ServletRequest servletRequest;

    private ResultToCount resultToCount = new ResultToCount();

    private String firstURL,secondURL,thirdURL;
    private int flag = 0;


    /**
     * Метод отвечает за логирование на страницы, а в частности:
     * /,/add,/admin,/send
    */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String logggg(Model model) {
        return "login";
    }

/**
 * Метод необходим для доступа к генерации ссылок,
 * и просмотру результатов тестирования
 */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String mainPage(Model model) {
        model.addAttribute("name",servletRequest.getServerName()+":"+servletRequest.getServerPort());
        if (flag == 1) {
            model.addAttribute("first", firstURL);
            model.addAttribute("second",secondURL);
            model.addAttribute("third",thirdURL);
        }
        if(flag == 0)
            model.addAttribute("butt","Начать");
        else model.addAttribute("butt","Завершить");
        return "admin";
    }
    /**
    * Метод необходим для  обработки информации от страницы  / ,
    * а именно генерация ссылок и начало и завершение тестирования
     */
    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public String test(Model model){
        firstURL = resultToCount.RandomGen("1");
        secondURL = resultToCount.RandomGen("2");
        thirdURL = resultToCount.RandomGen("3");
        if(flag == 0) {
            flag = 1;
        }
        else{
            flag = 0;
        }

        model.addAttribute("name",servletRequest.getServerName()+":"+servletRequest.getServerPort());

        if(flag == 1){
            model.addAttribute("first",firstURL);
            model.addAttribute("second",secondURL);
            model.addAttribute("third",thirdURL);
            model.addAttribute("butt","Завершить");
        }
        else {
            model.addAttribute("butt", "Начать");
        }
        return "admin";
    }

    /**
     * Метод отвечает за доступ к страницам тестирования(рандомно сгенерированным),
     * и отвечает за доступ к страницам которых нет (404)
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public String method(@PathVariable("id") String id, Model model) {

        List<QuestionInfo> list = dataBaseDAO.getQuestions();
        model.addAttribute("allQuestion",list);

        if(id.equals(firstURL)){
        System.out.println("the url value : "+ id );
        return "table1";
        }
        if(id.equals(secondURL)){
            System.out.println("the url value : "+id );
            return "table2";
        }
        if(id.equals(thirdURL)){
            System.out.println("the url value : "+id );
            return "table3";
        }
        return "notfound";
    }

  /**
   * Добавление вопроса форма
   * работа с вопросами в бд(изменение удаление добавление)
   */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addQuestiontToDBget(Model model ) {

        List<QuestionInfo> list = dataBaseDAO.getQuestions();
        model.addAttribute("allQuestion",list);

        return "form";
    }
    /**
     * Добавление вопроса форма POST
     * работа с вопросами в бд(изменение удаление добавление) Post
     * @param change - отвечает за выбранный вариант изменения удалить, изменить, добавить
     * @param idQue - ид вопроса
     * @param question - содержание вопроса
     * @param meaning - смысл вопроса
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addQuestiontToDB(Model model,
                                   @RequestParam("change") String change,
                                   @RequestParam("idQue") String idQue,
                                   @RequestParam("question") String question,
                                   @RequestParam("meaning") String meaning){

        // Добавить
        if(Integer.parseInt(change) == 1) {
            dataBaseDAO.WriteDBQuestion(Long.parseLong(idQue),question,Long.parseLong(meaning));
        }
        //Изменить
        if(Integer.parseInt(change) == 2) {
            dataBaseDAO.ChangeDBQuestion(Long.parseLong(idQue),question,Long.parseLong(meaning));
        }
        //Delete
        if(Integer.parseInt(change) == 3) {
            dataBaseDAO.DeleteDBQuestion(Long.parseLong(idQue));
        }
        System.out.println(change + " " + idQue + " " + question);
        List<QuestionInfo> list = dataBaseDAO.getQuestions();
        model.addAttribute("allQuestion",list);

        return "form";
    }

    /**доступ к просмотру результатов
     */
    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public String resendDataDB(Model model) {
        List<QuestionInfo> list = dataBaseDAO.getQuestions();
        model.addAttribute("allQuestion",list);
        model.addAttribute("igm","3");

        return "send";
    }


/**
 * доступ к спросмотру результатов
 * @param selectDepartment - отвечает за выбранный отдел на данный момент 1-3
 * @param dateFrom начальный промежуток между датами поиска
 * @param dateTo конечный промежуток даты
 */
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public String sendDataDB(Model model,@RequestParam("selectDepartment") String selectDepartment,
                             @RequestParam("dateFrom") String dateFrom,
                             @RequestParam("dateTo") String dateTo) throws ParseException {
        List<QuestionResultInfo> listResult = dataBaseDAO.getQuestionResult(selectDepartment,dateFrom,dateTo);
        List<QuestionInfo> listQuestion = dataBaseDAO.getQuestions();
        Map<Long, Double> avGinfos = dataBaseDAO.getAVGResult(selectDepartment,dateFrom,dateTo);
        System.out.println("Параметры от админа:"+selectDepartment + " " + dateFrom + " " + dateTo);

        if(avGinfos.isEmpty()){
            for (QuestionInfo questionInfo : listQuestion) {
                questionInfo.setAverage("Таких записей нет");
            }
        }
        else {
            for (QuestionInfo questionInfo : listQuestion) {
                questionInfo.setAverage(String.format("%.2f", avGinfos.get(questionInfo.getId())));
            }

        }

        model.addAttribute("dateFrom",dateFrom);
        model.addAttribute("dateTo",dateTo);
        model.addAttribute("selectdepartment",selectDepartment);
        model.addAttribute("allQuestion",listQuestion);
        model.addAttribute("resultQuestion",listResult);
        model.addAttribute("avginf",avGinfos);


        return "send";
    }

    /** обработка и отправление результатов тестирования в БД
     *
     * @param allResult все ответы на вопросы в формате [x1,x2...]
     * @param department отдел в котором было тестирование
     */
    @RequestMapping(value = "/table", method = RequestMethod.POST)
    public String tableDBSand(@RequestParam("variable") String allResult,
                              @RequestParam("department") String department) throws ParseException {
        List<QuestionInfo> listQuestion = dataBaseDAO.getQuestions();
        dataBaseDAO.WriteResultTest(allResult,Long.parseLong(department),listQuestion);
        System.out.println(allResult + " " + department);
        return "table1";
    }

}
