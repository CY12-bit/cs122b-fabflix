package edu.uci.ics.fabflixmobile.data.model;

import java.util.HashMap;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {

    private final String mid;
    private final String title;
    private Integer year;
    private String director;

    private HashMap<String,String> stars;

    private HashMap<Integer,String> genres;

    public Movie(String mid, String title, Integer year) {
        this.stars = new HashMap<String,String>();
        this.genres = new HashMap<Integer,String>();
        this.year = year;
        this.mid = mid;
        this.title = title;
    }

    public void setYear(Integer yr) {
        this.year = yr;
    }

    public void setDirector(String direct) {
        this.director = direct;
    }

    public void addGenre(Integer gid, String gname) {
        this.genres.put(gid,gname);
    }

    public void addStar(String sid, String sname) {
        stars.put(sid,sname);
    }

    public String getName() { return title; }

    public Integer getYear() {
        return year;
    }
    public String getDirector() {return director; }

    public HashMap<String,String> getStars() { return stars; }

    public HashMap<Integer,String> getGenres() {return genres; }

}