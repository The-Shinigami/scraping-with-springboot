package com.example.datascraping.service;

import java.util.*;
import com.example.datascraping.dto.ResponseSD;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
//import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ScrapeServiceImpl implements ScrapeService{


    @Override
    public List<ResponseSD> getTitlesSD() {
        List<ResponseSD> ResponseDtoS = new ArrayList<>();

                extractDataFromSD(ResponseDtoS,"https://www.sciencedirect.com/search?qs=Blockchain");

        return ResponseDtoS;
    }
    private void extractDataFromSD(List<ResponseSD> ResponseDtoS, String url) {
        try {
            //loading the HTML to a Document Object
            Document document = Jsoup.connect(url).get();
            document.getAllElements().forEach(a ->  System.out.println(a));

//Selecting the element which contains the ad list
//           Element element = document.getElementsByClass("result-list-title-link").first();
//           System.out.println(element.attr("href"));
            //getting all the <a> tag elements inside the list-       -3NxGO class
            List<Element> elements =document.getElementsByClass("result-list-title-link");
  System.out.println(elements.size());
            for (Element ads: elements) {

                    ResponseSD ResponseDto = new ResponseSD();

                    ResponseDto.setTitle("");
                    ResponseDto.setUrl("https://www.sciencedirect.com/"+ ads.attr("href"));

                    ResponseDtoS.add(ResponseDto);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<ResponseSD> extreactDataFromSD(){
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Mohammed Berbar\\Desktop\\projects\\springboot\\Project_basics\\firstProject\\src\\main\\resources\\static\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        loadPage(driver);

        List<WebElement> elements = driver.findElements(By.className("result-list-title-link"));
        List<ResponseSD> links = new ArrayList<ResponseSD>();
        elements.forEach(element -> {
            ResponseSD res = new ResponseSD();
            res.setTitle(element.getText());
            res.setUrl(element.getAttribute("href"));
            links.add(res);
        }
        );

        driver.close();
        return links;
    }
    boolean loadPage(WebDriver driver){
        driver.get("https://www.sciencedirect.com/search?qs=Blockchain");
        try {
            Thread.sleep(3000);  // Let the user actually see something!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long l = 2000;
        WebElement element = new WebDriverWait(driver, new Duration(l,10000)).until(ExpectedConditions.visibilityOfElementLocated(By.className("result-list-title-link")));
        while (!element.isDisplayed()){
            System.out.println("loading...");
        }
        return true;
    }
}
