package Parse;

import java.util.ArrayList;

public class StarInMovie {
    String starId;
    String movieId;
    String movieName;
    String starName;
    public StarInMovie() {
        this.starId = null;
        this.movieId = null;
        this.movieName = null;
        this.starName = null;
    }

    public String getStarId() {
        return starId;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        movieId = movieId.strip();
        movieId = movieId.replaceAll("[\\W]","");
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        movieName = movieName.strip();
        movieName = movieName.replaceAll("~"," ");
        movieName = movieName.replaceAll("[\\\\][\\W]","");
        movieName = movieName.replaceAll("[\\\\]","");
        this.movieName = movieName;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String name) {
        if (name != "") {
            name = name.strip();
            name = name.replaceAll("~", " ");
            name = name.replaceAll("[\\\\][\\W]","");

            // Capitalization Process
            String[] split_words = name.split("[ ]+");
            ArrayList<String> capitalized_words = new ArrayList<String>();
            for (String c : split_words) {
                c = c.substring(0, 1).toUpperCase() + c.substring(1);
                capitalized_words.add(c);
            }
            name = String.join(" ",capitalized_words);
        }

        this.starName = name;
    }

    public boolean hasNull() {
        return movieName == null || starId == null || movieId == null;
    }

    @Override
    public String toString() {
        return "StarInMovie{" +
                "starId='" + starId + '\'' +
                ", movieId='" + movieId + '\'' +
                ", movieName='" + movieName + '\'' +
                ", starName='" + starName + '\'' +
                '}';
    }
}
