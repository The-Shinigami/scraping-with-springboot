package com.example.datascraping.dto;

import java.util.List;

import lombok.Data;

@Data
public class DetailsSD {
	String title;
	List<String> Keywords;
	List<String> authors;
	List<String> universeties;
	String date;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<String> getKeywords() {
		return Keywords;
	}
	public void setKeywords(List<String> keywords) {
		Keywords = keywords;
	}
	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	public List<String> getUniverseties() {
		return universeties;
	}
	public void setUniverseties(List<String> universeties) {
		this.universeties = universeties;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	
	

}
