package com.example.datascraping.controller;

import com.example.datascraping.dto.DetailsSD;
import com.example.datascraping.dto.ResponseSD;
import com.example.datascraping.service.ScrapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ScrapeController {

    @Autowired
    ScrapeService scrapeService;


    @GetMapping("/data")
    public List<ResponseSD> getData(){
        return scrapeService.extreactDataFromSD();
    }

    @GetMapping("/details")
    @Scheduled(cron = "0 0 12 * * 0")
    public List<DetailsSD> getDetails(){
        return scrapeService.extreactDetailsFromSD();
    }

    @GetMapping("/dataAcm")
    public List<ResponseSD> getDataAcm(){
        return scrapeService.extreactDataFromACM();
    }

    @GetMapping("/detailsAcm")
    @Scheduled(cron = "0 0 14 * * 0")
    public List<DetailsSD> getDetailsAcm(){
        return scrapeService.extreactDetailsFromACM();
    }

    @GetMapping("/dataIeee")
    public List<ResponseSD> getDataIeee(){
        return scrapeService.extreactDataFromIeee();
    }

    @GetMapping("/detailsIeee")
    @Scheduled(cron = "0 0 16 * * 0")
    public List<DetailsSD> getDetailsIeee(){
        return scrapeService.extreactDetailsFromIeee();
    }
}
