package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {

    private final String mid;
    private final String title;
    private Integer year;
    private String director;

    private ArrayList<String[]> stars;
    private ArrayList<String[]> genres;


    public Movie(String mid, String title, Integer year) {
        this.stars = new ArrayList<String[]>();
        this.genres = new ArrayList<String[]>();
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

    public void addGenre(String gid, String gname) {
        final String[] tempGenre = {gid,gname};
        this.genres.add(tempGenre);
    }

    public void addStar(String sid, String sname) {
        final String[] tempStar = {sid,sname};
        this.stars.add(tempStar);
    }

    public void addStar(String sid, String sname, String bYear) {
        final String[] tempStar = {sid,sname,bYear};
        this.stars.add(tempStar);
    }

    public String getName() { return title; }

    public Integer getYear() {
        return year;
    }
    public String getDirector() {return director; }

    public String getId() {return this.mid;}

    public ArrayList<String[]> getStars() { return stars; }

    public ArrayList<String[]> getGenres() {return genres; }

}