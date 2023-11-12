package Parse;

import java.util.HashMap;
import java.util.HashSet;

public class xmlParser {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.out.println("-- Start XML Parsing");
        MovieParser mp = new MovieParser();
        mp.runParser();
        HashMap<String, HashSet<MovieObject>> title_movie_map = mp.getTitleMovieMap();
        ActorParser ap = new ActorParser();
        ap.runParser();
        CastParser cp = new CastParser(title_movie_map);
        cp.runParser();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("-- Finished XML Parsing: " + (duration/1_000_000_000));
    }
}
