package Parse;

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
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
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
