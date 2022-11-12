package com.example.datascraping.service;

import com.example.datascraping.dto.DetailsSD;
import com.example.datascraping.dto.Quartile;
import com.example.datascraping.dto.ResponseSD;
import com.example.datascraping.repository.ScrapeRepository;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<ResponseSD> links = new ArrayList<ResponseSD>();
        List<String> urls= new ArrayList<String>();
        List<String> offsets=Arrays.asList("0","25","50","75","100","125","150");
        for(int i=0;i<offsets.size();i++) {
            urls.add("https://www.sciencedirect.com/search?qs=blockchain&articleTypes=FLA&lastSelectedFacet=articleTypes&offset="+offsets.get(i));
        }
        for(int j=0;j<urls.size();j++) {
            loadPage(driver,urls.get(j),"SD");

            List<WebElement> elements = driver.findElements(By.className("result-list-title-link"));

            elements.forEach(element -> {
                        ResponseSD res = new ResponseSD();
                        if(this.exists(element.getText())) {
                            System.out.println("Already exists in DB");
                        }else {
                            res.setTitle(element.getText());
                            res.setUrl(element.getAttribute("href"));
                            links.add(res);
                        }
                    }
            );
        }
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
        }else if(journal=="IEEE"){
            while (!driver.findElement(By.className("main-section")).isDisplayed()){
                System.out.println("loading...");
            }
        }else if(journal=="SJR"){
            while (!driver.findElement(By.className("journaldescription")).isDisplayed()){
                System.out.println("loading...");
            }
        }
        return true;
    }

    @Override
    public List<DetailsSD> extreactDetailsFromSD() {
        List<ResponseSD> links=this.extreactDataFromSD();
        if(links.isEmpty()) {
            System.out.println("There is no new data");
            return null;
        }else {
            System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
            ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
            WebDriver driver = new ChromeDriver(options);
            List<String> urls=new ArrayList<String>();
            List<DetailsSD> dets=new ArrayList<DetailsSD>();

            links.forEach(link -> {
                        loadPages(driver,link.getUrl(),"SD");
                        urls.add(link.getUrl());

                        WebElement issn =driver.findElement(By.className("publication-title-link"));
                        WebElement elt =driver.findElement(By.className("title-text"));

                        WebElement doi=driver.findElement(By.id("article-identifier-links")).findElement(By.className("doi"));
                        List<WebElement> abstractText=driver.findElement(By.id("abstracts")).findElements(By.className("abstract"));
                        WebElement text;
                        if(abstractText.size()==1) {
                            text=abstractText.get(0).findElement(By.tagName("div")).findElement(By.tagName("p"));
                        }else {
                            text=abstractText.get(1).findElement(By.tagName("div")).findElement(By.tagName("p"));
                        }

                        List<WebElement> elms = driver.findElements(By.className("keyword"));
                        List<WebElement> authors = driver.findElements(By.className("content"));
                        List<WebElement> tests= driver.findElements(By.className("content"));
                        List<String> auths=new ArrayList<String>();
                        authors.forEach(author -> {
                            System.out.println("siiiiiizeeee"+author.findElements(By.tagName("span")).size());
                            if(author.findElements(By.tagName("span")).size()==2) {
                                auths.add(author.findElement(By.className("surname")).getText());
                            }else{
                                auths.add(author.findElement(By.className("given-name")).getText() + " " + author.findElement(By.className("surname")).getText());
                            }
                        });
                        WebElement button=driver.findElement(By.id("show-more-btn"));
                        button.click();

//                        WebElement element =driver.findElement(By.id("show-more-btn"));
//                        JavascriptExecutor executor = (JavascriptExecutor)driver;
//                        executor.executeScript("arguments[0].click();", element);

                        List<WebElement> universeties = driver.findElements(By.className("affiliation"));
                        WebElement date =driver.findElement(By.tagName("p"));

                        DetailsSD dts = new DetailsSD();
                        dts.setTitle(elt.getText());

                        List<String> keywords=new ArrayList<String>();
                        List<String> univs=new ArrayList<String>();
                        elms.forEach(elm -> {
                            keywords.add(elm.getText());
                        });
                        dts.setKeywords(keywords);


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

                        dts.setIssn(issn.getText());
                        dts.setDate(dateTrue);
                        dts.setJournal("SD");
                        dts.setDoi(doi.getText());
                        dts.setAbstractText(text.getText());
                        dets.add(dts);
                        repo.save(dts);
                    }
            );

            driver.close();

            return dets;
        }
    }

    ////////////////////ACM//////////////////////////
    @Override
    public List<ResponseSD> extreactDataFromACM() {
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        List<ResponseSD> links = new ArrayList<ResponseSD>();
        List<String> urls= new ArrayList<String>();
        List<String> offsets=Arrays.asList("0","1","2","3","4","5","6");
        for(int i=0;i<offsets.size();i++) {
            urls.add("https://dl.acm.org/action/doSearch?AllField=Blockchain&startPage="+offsets.get(i)+"&pageSize=50");
        }
        for(int j=0;j<urls.size();j++) {
            loadPage(driver,urls.get(j),"ACM");

            List<WebElement> elements = driver.findElements(By.className("hlFld-Title"));

            elements.forEach(element -> {
                        ResponseSD res = new ResponseSD();
                        if(this.exists(element.getText())) {
                            System.out.println("Already exists in DB");
                        }else {
                            if(driver.findElement(By.className("issue-heading")).getText().contains("RESEARCH-ARTICLE")) {
                                res.setTitle(element.getText());
                                res.setUrl(element.findElement(By.tagName("a")).getAttribute("href"));
                                links.add(res);
                            }
                        }
                    }
            );
        }
        driver.close();
        return links;
    }

    @Override
    public List<DetailsSD> extreactDetailsFromACM() {
        List<ResponseSD> links=this.extreactDataFromACM();
        if(links.isEmpty()) {
            System.out.println("There is no new data");
            return null;
        }else {
            System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
            ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
            WebDriver driver = new ChromeDriver(options);
            List<String> urls=new ArrayList<String>();
            List<DetailsSD> dets=new ArrayList<DetailsSD>();

            links.forEach(link -> {
                        loadPages(driver,link.getUrl(),"ACM");
                        urls.add(link.getUrl());

                        List<WebElement> authors = driver.findElements(By.className("loa__author-name"));


                        WebElement date =driver.findElement(By.className("CitationCoverDate"));

                        DetailsSD dts = new DetailsSD();
                        dts.setTitle(link.getTitle());

                        List<String> auths=new ArrayList<String>();
                        List<String> univs=new ArrayList<String>();

                        authors.forEach(author -> {
                            auths.add(author.findElement(By.tagName("span")).getText());
                        });

                        WebElement abstractTxt=driver.findElement(By.className("abstractSection")).findElement(By.tagName("p"));

                        WebElement doi=driver.findElement(By.className("issue-item__doi"));
                        dts.setDoi(doi.getText());

                        try {
                            WebElement button=driver.findElement(By.className("loa__link"));
                            button.sendKeys(Keys.ENTER);
                            Thread.sleep(10000);
                            List<WebElement> universeties = driver.findElements(By.className("auth-info"));

                            universeties.forEach(unv -> {
                                if(unv.findElements(By.tagName("span")).size() >1) {
                                    univs.add(unv.findElements(By.tagName("span")).get(1).getText());
                                }else {
                                    univs.add(unv.findElement(By.tagName("span")).findElement(By.tagName("p")).getText());
                                }
                            });
                            dts.setUniverseties(univs);

                            WebElement button2=driver.findElement(By.id("pill-information__contentcon"));
                            button2.sendKeys(Keys.ENTER);
                            Thread.sleep(10000);
                            WebElement issn= driver.findElement(By.className("cover-image__details-extra")).findElements(By.className("flex-container")).get(0).findElement(By.className("space"));
                            List<String> keywords=new ArrayList<String>();
                            List<WebElement> elms = driver.findElements(By.className("badge-type"));
                            elms.forEach(elm -> {
                                keywords.add(elm.getText());
                            });

                            dts.setIssn(issn.getText());
                            dts.setKeywords(keywords);


                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        dts.setAuthors(auths);

                        dts.setDate(date.getText());
                        dts.setJournal("ACM");
                        dts.setAbstractText(abstractTxt.getText());
                        dets.add(dts);
                        repo.save(dts);
                    }
            );

            driver.close();
            return dets;
        }
    }


    /////////////////////////IEEE////////////////////////////
    @Override
    public List<ResponseSD> extreactDataFromIeee() {
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
        List<ResponseSD> links = new ArrayList<ResponseSD>();
        List<String> urls= new ArrayList<String>();
        List<String> offsets=Arrays.asList("1");
        for(int i=0;i<offsets.size();i++) {
            urls.add("https://ieeexplore.ieee.org/search/searchresult.jsp?queryText=Blockchain&highlight=true&returnType=SEARCH&matchPubs=true&refinements=ContentType:Journals&returnFacets=ALL&pageNumber="+offsets.get(i));
        }
        for(int j=0;j<urls.size();j++) {
            loadPage(driver,urls.get(j),"IEEE");

            List<WebElement> elements = driver.findElements(By.className("List-results-items"));

            elements.forEach(element -> {
                        ResponseSD res = new ResponseSD();

                        if(this.exists(element.findElement(By.tagName("a")).getText())) {
                            System.out.println("Already exists in DB");
                        }else {
                            res.setTitle(element.findElement(By.tagName("a")).getText());
                            res.setUrl(element.findElement(By.className("result-item-title")).findElement(By.tagName("a")).getAttribute("href"));
                            links.add(res);
                        }

                    }
            );
        }
        driver.close();
        return links;
    }

    @Override
    public List<DetailsSD> extreactDetailsFromIeee() {
        List<ResponseSD> links=this.extreactDataFromIeee();
        if(links.isEmpty()) {
            System.out.println("There is no new data");
            return null;
        }else {
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
                        dts.setDate(dates.get(3).getText().split(":")[1].trim());

                        WebElement elt =driver.findElement(By.className("document-title"));

                        dts.setTitle(elt.findElement(By.tagName("span")).getText());

                        WebElement abstractText=driver.findElement(By.className("abstract-text")).findElement(By.tagName("div")).findElement(By.tagName("div")).findElement(By.tagName("div"));

                        dts.setAbstractText(abstractText.getText());

                        WebElement doi=driver.findElement(By.className("stats-document-abstract-doi")).findElement(By.tagName("a"));
                        dts.setDoi(doi.getText());

                        try {
                            int condition=driver.findElements(By.className("col-6")).get(0).findElements(By.className("u-pb-1")).get(2).findElements(By.tagName("div")).size();

                            if(condition ==1) {
                                WebElement element =  driver.findElements(By.className("col-6")).get(0).findElements(By.className("u-pb-1")).get(2).findElement(By.tagName("div"));
                                JavascriptExecutor executor = (JavascriptExecutor)driver;
                                executor.executeScript("arguments[0].click();", element);

                                List<WebElement> issn=driver.findElement(By.className("abstract-metadata-indent")).findElements(By.tagName("div"));
                                for(int i=0;i<issn.size();i++){
                                    if(issn.get(i).getText().split(":")[0].contains("Electronic ISSN")){
                                        dts.setIssn(issn.get(i).getText().split(":")[1].trim());
                                        break;
                                    }
                                }

                            }else {
                                WebElement issn = driver.findElements(By.className("col-6")).get(0).findElements(By.className("u-pb-1")).get(2).findElement(By.tagName("div")).findElement(By.tagName("div"));
                                dts.setIssn(issn.getText().split(":")[1]);
                            }
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block

                        }


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
                            List<WebElement> data= unv.findElement(By.className("author-card")).findElement(By.className("row")).findElements(By.tagName("div"));

                            if(data.size()==4 || data.size()==6) {
                                univs.add(data.get(0).findElements(By.tagName("div")).get(1).findElement(By.tagName("div")).getText());
                            }else {
                                univs.add(data.get(1).findElements(By.tagName("div")).get(1).findElement(By.tagName("div")).getText());
                            }
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

    @Override
    public List<DetailsSD> extractDetailsFromSJR(){
        System.setProperty("webdriver.chrome.driver", env.getProperty("webdriver"));
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);


        List<DetailsSD> listDetailsTmp = repo.findAll();
        List<DetailsSD> listDetails;
        for(int i = 0 ; i<listDetailsTmp.size();i++) {

            loadPage(driver, "https://www.scimagojr.com/journalsearch.php?q=" + listDetailsTmp.get(i).getIssn().replace(" ", "+"), "SJR");
            if (driver.findElements(By.className("search_results")).get(0).findElements(By.tagName("a")).size()>0 && driver.findElements(By.className("search_results")).get(0).findElements(By.tagName("a")).get(0).isDisplayed()) {
                WebElement result = driver.findElements(By.className("search_results")).get(0).findElements(By.tagName("a")).get(0);
                result.click();

                String hIndex = driver.findElements(By.className("hindexnumber")).get(0).getText();
                listDetailsTmp.get(i).setHIndex(hIndex);


                try {
                    List<WebElement> quartiles = driver.findElements(By.className("cellslide")).get(1).findElements(By.tagName("tr"));
                    List<Quartile> quartileList = new ArrayList<>();

                    for (int j = 1; j < quartiles.size(); j++) {
                        Quartile q = new Quartile();
                        List<WebElement> columns = quartiles.get(j).findElements(By.tagName("td"));
                        String category = columns.get(0).getText();
                        String year = columns.get(1).getText();
                        String quartile = columns.get(2).getText();

                        q.setCategory(category);
                        q.setYear(year);
                        q.setQuartile(quartile);

                        quartileList.add(q);

                    }
                    listDetailsTmp.get(i).setQuartiles(quartileList);
                } catch (Exception e) {
                    System.out.println("quartiles does not exist");
                }
                repo.save(listDetailsTmp.get(i));
            }
        }
        listDetails =  repo.findAll();

        return listDetails;
    }

    @Override
    public boolean exists(String tiltle) {
        return repo.existsByTitle(tiltle);
    }
}
