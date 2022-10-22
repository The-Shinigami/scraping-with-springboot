package com.example.datascraping.service;



import com.example.datascraping.dto.DetailsSD;
import com.example.datascraping.dto.ResponseSD;
import com.example.datascraping.repository.ScrapeRepository;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
//import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapeServiceImpl implements ScrapeService{

    @Autowired
    private Environment env;

    @Autowired
    private ScrapeRepository repo;

    @Override
    public List<ResponseSD> extreactDataFromSD(){
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        loadPage(driver,"https://www.sciencedirect.com/search?qs=Blockchain","SD");

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

    boolean loadPages(WebDriver driver,String url,String journal) {
        driver.get(url);
        try {
            Thread.sleep(3000);  // Let the user actually see something!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long l = 2000;
        if(journal=="SD") {
            while (!driver.findElement(By.className("title-text")).isDisplayed()){
                System.out.println("loading...");
            }
        }else if(journal=="ACM") {
            while (!driver.findElement(By.className("citation__title")).isDisplayed()){
                System.out.println("loading...");
            }
        }else {
            while (!driver.findElement(By.className("document-title")).isDisplayed()){
                System.out.println("loading...");
            }
        }
        return true;
    }

    boolean loadPage(WebDriver driver,String url,String journal){
        driver.get(url);
        try {
            Thread.sleep(3000);  // Let the user actually see something!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long l = 2000;


        if(journal=="SD") {
            while (!driver.findElement(By.className("result-list-title-link")).isDisplayed()){
                System.out.println("loading...");
            }
        }else if(journal=="ACM") {
            while (!driver.findElement(By.className("hlFld-Title")).isDisplayed()){
                System.out.println("loading...");
            }
        }else {
            while (!driver.findElement(By.className("main-section")).isDisplayed()){
                System.out.println("loading...");
            }
        }
        return true;
    }

    @Override
    public List<DetailsSD> extreactDetailsFromSD() {
        List<ResponseSD> links=this.extreactDataFromSD();
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        List<String> urls=new ArrayList<String>();
        List<DetailsSD> dets=new ArrayList<DetailsSD>();

        links.forEach(link -> {
                    loadPages(driver,link.getUrl(),"SD");
                    urls.add(link.getUrl());

                    WebElement elt =driver.findElement(By.className("title-text"));

                    List<WebElement> elms = driver.findElements(By.className("keyword"));
                    List<WebElement> authors = driver.findElements(By.className("content"));
                    WebElement button=driver.findElement(By.id("show-more-btn"));
                    button.click();
                    List<WebElement> universeties = driver.findElements(By.className("affiliation"));
                    System.out.println(universeties.get(0).getText());
                    WebElement date =driver.findElement(By.tagName("p"));
                    System.out.println(date.getText());

                    DetailsSD dts = new DetailsSD();
                    dts.setTitle(elt.getText());

                    List<String> keywords=new ArrayList<String>();
                    List<String> auths=new ArrayList<String>();
                    List<String> univs=new ArrayList<String>();
                    elms.forEach(elm -> {
                        keywords.add(elm.getText());
                    });
                    dts.setKeywords(keywords);

                    authors.forEach(author -> {
                        auths.add(author.findElement(By.className("given-name")).getText()+" "+author.findElement(By.className("surname")).getText());

                    });
                    universeties.forEach(unv -> {
                        univs.add(unv.findElement(By.tagName("dd")).getText());

                    });
                    dts.setAuthors(auths);
                    dts.setUniverseties(univs);

                    String[] splits = date.getText().split(",");
                    String datePub=null;
                    for(int i=0;i<splits.length;i++){
                        if(splits[i].contains("Available online")) {
                            datePub=splits[i];
                        }

                    }


                    String dateTrue=null;

                    String[] tokens =datePub.split(" ");
                    dateTrue=tokens[tokens.length - 3]+" "+tokens[tokens.length - 2]+" "+tokens[tokens.length - 1];

                    dts.setDate(dateTrue);
                    dts.setJournal("SD");
                    dets.add(dts);
                    repo.save(dts);
                }
        );

        driver.close();

        return dets;
    }

    ////////////////////ACM//////////////////////////
    @Override
    public List<ResponseSD> extreactDataFromACM() {
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        loadPage(driver,"https://dl.acm.org/action/doSearch?AllField=Blockchain","ACM");

        List<WebElement> elements = driver.findElements(By.className("hlFld-Title"));
        List<ResponseSD> links = new ArrayList<ResponseSD>();
        elements.forEach(element -> {
                    ResponseSD res = new ResponseSD();

                    res.setTitle(element.getText());
                    res.setUrl(element.findElement(By.tagName("a")).getAttribute("href"));
                    links.add(res);
                }
        );

        driver.close();
        return links;
    }

    @Override
    public List<DetailsSD> extreactDetailsFromACM() {
        List<ResponseSD> links=this.extreactDataFromACM();
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        List<String> urls=new ArrayList<String>();
        List<DetailsSD> dets=new ArrayList<DetailsSD>();

        links.forEach(link -> {
                    loadPages(driver,link.getUrl(),"ACM");
                    urls.add(link.getUrl());

                    //         List<WebElement> elms = driver.findElements(By.className("keyword"));
                    List<WebElement> authors = driver.findElements(By.className("loa__author-name"));


                    WebElement date =driver.findElement(By.className("CitationCoverDate"));
                    System.out.println(date.getText());

                    DetailsSD dts = new DetailsSD();
                    dts.setTitle(link.getTitle());
                    //          List<String> keywords=new ArrayList<String>();
                    List<String> auths=new ArrayList<String>();
                    List<String> univs=new ArrayList<String>();
//             elms.forEach(elm -> {
//                  keywords.add(elm.getText());
//           });
                    //                dts.setKeywords(keywords);

                    authors.forEach(author -> {
                        auths.add(author.findElement(By.tagName("span")).getText());
                    });


                    try {
                        WebElement button=driver.findElement(By.className("loa__link"));
                        button.sendKeys(Keys.ENTER);
                        Thread.sleep(10000);
                        List<WebElement> universeties = driver.findElements(By.className("auth-info"));

                        universeties.forEach(unv -> {
                            if(unv.findElements(By.tagName("span")).size() >1) {
                                univs.add(unv.findElements(By.tagName("span")).get(1).getText());
                                System.out.println(unv.findElements(By.tagName("span")).get(1).getText());
                            }else {
                                univs.add(unv.findElement(By.tagName("span")).findElement(By.tagName("p")).getText());
                                System.out.println(unv.findElement(By.tagName("span")).findElement(By.tagName("p")).getText());
                            }
                        });
                        dts.setUniverseties(univs);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    dts.setAuthors(auths);

                    dts.setDate(date.getText());
                    dts.setJournal("ACM");
                    dets.add(dts);
                    repo.save(dts);
                }
        );

        driver.close();
        return dets;
    }


    /////////////////////////IEEE////////////////////////////
    @Override
    public List<ResponseSD> extreactDataFromIeee() {
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        loadPage(driver,"https://ieeexplore.ieee.org/search/searchresult.jsp?newsearch=true&queryText=blockchain","IEEE");

        List<WebElement> elements = driver.findElements(By.className("List-results-items"));
        List<ResponseSD> links = new ArrayList<ResponseSD>();
        elements.forEach(element -> {
                    ResponseSD res = new ResponseSD();
                    String cond=element.findElement(By.className("publisher-info-container")).findElements(By.tagName("span")).get(1).findElements(By.tagName("span")).get(1).getText();

                    if(cond.equals("Conference Paper")) {
                        res.setTitle(element.findElement(By.tagName("a")).getText());
                        res.setUrl(element.findElement(By.className("result-item-title")).findElement(By.tagName("a")).getAttribute("href"));
                        links.add(res);
                    }
                }
        );

        driver.close();
        return links;
    }

    @Override
    public List<DetailsSD> extreactDetailsFromIeee() {
        List<ResponseSD> links=this.extreactDataFromIeee();
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        List<String> urls=new ArrayList<String>();
        List<DetailsSD> dets=new ArrayList<DetailsSD>();

        links.forEach(link -> {
                    loadPages(driver,link.getUrl(),"IEEE");
                    urls.add(link.getUrl());
                    DetailsSD dts = new DetailsSD();
                    List<WebElement> dates =driver.findElements(By.className("u-pb-1"));
                    dts.setDate(dates.get(3).getText().split(":")[1]);
                    System.out.println(dates.get(3).getText().split(":")[1]);
                    WebElement elt =driver.findElement(By.className("document-title"));

                    dts.setTitle(elt.findElement(By.tagName("span")).getText());
                    try {
                        driver.findElement(By.id("authors")).sendKeys(Keys.ENTER);
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    List<WebElement> authors = driver.findElements(By.className("authors-accordion-container"));
                    List<String> auths=new ArrayList<String>();
                    List<String> univs=new ArrayList<String>();
                    List<String> keywords=new ArrayList<String>();

                    authors.forEach(author -> {

                        auths.add(author.findElement(By.tagName("a")).getText());
                    });
                    authors.forEach(unv -> {
                        univs.add(unv.findElement(By.className("author-card")).findElement(By.className("row")).findElements(By.tagName("div")).get(0).findElements(By.tagName("div")).get(1).findElement(By.tagName("div")).getText());

                    });

                    String newURL=driver.getCurrentUrl().replace("authors#authors","keywords#keywords");
                    driver.get(newURL);
                    List<WebElement> elms = driver.findElements(By.className("doc-keywords-list-item")).get(0).findElements(By.className("stats-keywords-list-item"));

                    elms.forEach(elm -> {

                        keywords.add(elm.getAttribute("data-tealium_data").split("\"keyword:\"")[0].replace("}","").replace("\"","").split(",")[1].split(":")[1]);

                    });

                    dts.setAuthors(auths);
                    dts.setUniverseties(univs);
                    dts.setKeywords(keywords);
                    dts.setJournal("IEEE");
                    dets.add(dts);
                    repo.save(dts);
                }
        );

        driver.close();
        return dets;
    }
}
