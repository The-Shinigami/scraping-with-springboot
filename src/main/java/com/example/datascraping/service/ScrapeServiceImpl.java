package com.example.datascraping.service;



import com.example.datascraping.dto.DetailsSD;
import com.example.datascraping.dto.ResponseSD;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
//import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapeServiceImpl implements ScrapeService{


    @Override
    public List<ResponseSD> extreactDataFromSD(){
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\i\\Downloads\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
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
    
    boolean loadPages(WebDriver driver,String url) {
    	driver.get(url);
    	try {
            Thread.sleep(3000);  // Let the user actually see something!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long l = 2000;

        while (!driver.findElement(By.className("title-text")).isDisplayed()){
            System.out.println("loading...");
        }
        return true;
    }
    
    boolean loadPage(WebDriver driver){
        driver.get("https://www.sciencedirect.com/search?qs=Blockchain");
        try {
            Thread.sleep(3000);  // Let the user actually see something!
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long l = 2000;

        while (!driver.findElement(By.className("result-list-title-link")).isDisplayed()){
            System.out.println("loading...");
        }
        return true;
    }

	@Override
	public List<DetailsSD> extreactDetailsFromSD() {
		List<ResponseSD> links=this.extreactDataFromSD();
		System.setProperty("webdriver.chrome.driver", "C:\\Users\\i\\Downloads\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
//       options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        WebDriver driver = new ChromeDriver(options);
		List<String> urls=new ArrayList<String>();
		List<DetailsSD> dets=new ArrayList<DetailsSD>();
		
        links.forEach(link -> {
        	loadPages(driver,link.getUrl());
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
                 dets.add(dts);
                              
        }
        );
        
        driver.close();
		return dets;
	}
}
