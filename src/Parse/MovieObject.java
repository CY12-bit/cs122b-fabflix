package Parse;

import java.util.ArrayList;

public class MovieObject {

    private String title;

    private int year;

    private String mid;

    private String director;

    public MovieObject() {

    }
    public MovieObject(String mid, String title, String director, Integer year) {
        this.mid = mid;
        this.title = title;
        this.director = director;
        this.year = year;
    }

    // Getters
    public int getYear() { return year; }
    public String getId() { return mid; }
    public String getDirector() { return director; }
    public String getTitle() { return title; }

    // Setters
    public void setId(String id) { this.mid = id; }
    public void setDirector(String direct) {
        direct = direct.strip();
        if (direct.contains("Unknown")||direct.contains("unknown")||direct.contains("UnYear")) {
            direct = "[Unknown]";
        }
        else {
            direct = direct.replaceAll("~"," ");

            // Capitalization Process
            String[] split_words = direct.split(" ");
            ArrayList<String> capitalized_words = new ArrayList<String>();
            for (String c : split_words) {
                c = c.substring(0, 1).toUpperCase() + c.substring(1);
                capitalized_words.add(c);
            }
            direct = String.join(" ",capitalized_words);
        }
        this.director = direct;
    }
    public void setTitle(String title) {
        title = title.strip();
        title = title.replaceAll("~"," ");
        title = title.replaceAll("[\\\\][\\W]","");
        title = title.replaceAll("[\\\\]","");
        this.title = title;
    }
    public void setYear(String yr) {
        yr = yr.strip();
        yr = yr.replaceAll("\\D","0");
        while (yr.length() < 4) {
            yr += "0";
        }
        this.year = Integer.parseInt(yr);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + mid + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", director=" + director +
                '}';
    }
}
