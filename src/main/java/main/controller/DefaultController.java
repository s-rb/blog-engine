package main.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Controller
public class DefaultController {

    @RequestMapping("/")
    public String index(Model model) {
        log.info("--- Получен запрос на \"/\". Вернули index.html");
        return "index";
    }

    @RequestMapping(method =
            {RequestMethod.OPTIONS, RequestMethod.GET},
            value = "/**/{path:[^\\.]*}")
    public String redirectToIndex() {
        log.info("--- Получен запрос на адрес соответствующий /**/{path:[^\\.]*}. Выполнили перенаправление forward:/");
        return "forward:/";
    }
}