package Parse;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieObject {

    private final static HashMap<String, String> genre_map = new HashMap<String, String>() {
        {
            put("susp", "Thriller"); put("ctcxx","Uncategorized"); put("dramn","Drama");
            put("cnr", "Cops and Robbers"); put("cnrb", "Cops and Robbers"); put("camp", "Camp");
            put("actn","Action"); put("cart","Animation"); put("comdx","Comedy");
            put("sports","Sports"); put("sram>","Drama");put("drama","Drama"); put("draam","Drama"); put("dram>","Drama");
            put("faml", "Family"); put("hist", "History"); put("fant","Fantasy");
            put("dram","Drama"); put("west","Western"); put("myst","Mystery"); put("sxfi","Sci-Fi");
            put("s.f.", "Sci-Fi"); put("advt","Adventure"); put("horr","Horror"); put("hor","Horror"); put("mystp","Mystery");
            put("romt","Romance"); put("romt.","Romance"); put("comd","Comedy"); put("musc","Musical");
            put("docu","Documentary"); put("porn", "Adult"); put("noir","Black");
            put("biop","Biological Picture"); put("biopp","Biological Picture"); put("tv","TV Show"); put("tvs","TV series");
            put("tvm","TV Miniseries"); put("ctxx","Uncategorized"); put("disa","Disaster");
            put("epic","Epic"); put("scfi","Sci-Fi"); put("surl","Surreal"); put("avga", "Avant Garde");
            put("crim","Crime"); put("surr","Surreal"); put("scif","Sci-Fi");
            put("dramd","Drama"); put("muscl","Musical"); put("surreal","Surreal"); put("muusc","Musical");
            put("tvmini","TV Miniseries"); put("musical","Musical");put("ctxxx","Uncategorized"); put("cnrbb","Cops and Robbers");
        }
    };

    private String title;

    private int year;

    private String mid;

    private String director;

    private ArrayList<String> genres;

    public MovieObject() { genres = new ArrayList<String>(); }

    // Getters
    public int getYear() { return year; }
    public String getId() { return mid; }
    public String getDirector() { return director; }
    public String getTitle() { return title; }
    public ArrayList<String> getGenres() { return genres; }

    // Setters
    public void setId(String id) {
        id = id.strip();
        id = id.replaceAll("[\\W]","");
        this.mid = id;
    }
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

    public void addGenre(String g) {
        g = g.strip();
        if (genre_map.containsKey(g.toLowerCase())) {
            genres.add(genre_map.get(g.toLowerCase()));
        }
        else {
            System.out.println("Unknown genre: "+ g);
        }
    }
    public void addGenres(final ArrayList<String> tempGenres) {
        for (String g : tempGenres) {
            addGenre(g);
        }
    }


    @Override
    public String toString() {
        return "Movie{" +
                "id='" + mid + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", director=" + director +
                ", genres=" + genres.toString() +
                '}';
    }
}
