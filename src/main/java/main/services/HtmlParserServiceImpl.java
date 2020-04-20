package main.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HtmlParserServiceImpl {

    public static String getTextStringFromHtml(String htmlStringToParse) {
        if (htmlStringToParse == null || htmlStringToParse.isBlank()) {
            log.info("--- Для парсинга передана пустая HTML строка : {" + htmlStringToParse + "}");
            return null;
        }
        Document html = Jsoup.parse(htmlStringToParse);
        String cleanText = html.wholeText();
        log.info("--- Полученная HTML строка : {" + htmlStringToParse + "}  успешно спарсена в {" + cleanText + "}");
        return cleanText;
    }
}