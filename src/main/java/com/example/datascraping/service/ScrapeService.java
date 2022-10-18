package com.example.datascraping.service;

import com.example.datascraping.dto.DetailsSD;
import com.example.datascraping.dto.ResponseSD;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScrapeService {
   List<ResponseSD> extreactDataFromSD();
   List<DetailsSD> extreactDetailsFromSD();
}
