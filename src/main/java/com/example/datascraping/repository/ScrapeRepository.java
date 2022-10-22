package com.example.datascraping.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.datascraping.dto.DetailsSD;

public interface ScrapeRepository extends MongoRepository<DetailsSD, String>{

}
