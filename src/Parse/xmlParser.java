package Parse;

public class xmlParser {
    public static void main(String[] args) {
        System.out.println("-- Start XML Parsing");
        MovieParser mp = new MovieParser();
        mp.runParser();
        ActorParser ap = new ActorParser();
        ap.runParser();
        CastParser cp = new CastParser();
        cp.runParser();
        System.out.println("-- Finished XML Parsing");
    }
}
