package Parse;

import java.util.HashMap;

public class xmlParser {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.out.println("-- Start XML Parsing");
        MovieParser mp = new MovieParser();
        mp.runParser();

        HashMap<String, MovieObject> movieMap = mp.getMovieMap();
        ActorParser ap = new ActorParser();
        ap.runParser();
        HashMap<String, String> actorMap = ap.getActorNameId();

        CastParser2 cp = new CastParser2(movieMap, actorMap);
        cp.runParser();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("-- Finished XML Parsing: " + (duration/1_000_000_000));
    }
}
