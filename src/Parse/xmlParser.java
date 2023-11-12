package Parse;

import java.util.HashMap;

public class xmlParser {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.out.println("-- Start XML Parsing");
        MovieParser mp = new MovieParser();
        mp.runParser();

        HashMap<String, MovieObject> movieMap = mp.getMovieMap();
        // HashMap<String, HashSet<String>> moveIdGroups = mp.getMovieIdGroups();
        ActorParser ap = new ActorParser();
        ap.runParser();

        CastParser2 cp = new CastParser2(movieMap);
        cp.runParser();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("-- Finished XML Parsing: " + (duration/1_000_000_000));
    }
}
