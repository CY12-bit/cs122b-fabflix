package Parse;

public class xmlParser {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.out.println("-- Start XML Parsing");
        MovieParser mp = new MovieParser();
        mp.runParser();
        ActorParser ap = new ActorParser();
        ap.runParser();
        CastParser cp = new CastParser();
        cp.runParser();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("-- Finished XML Parsing: " + (duration/1_000_000_000));
    }
}
