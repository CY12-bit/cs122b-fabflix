package Parse;

public class xmlParser {
    public static void main(String[] args) {
        ActorParser ap = new ActorParser();
        MovieParser mp = new MovieParser();


        mp.runParser();
        System.out.println("-- Finished Movie Parser");
        ap.runExample();
        System.out.println("-- Finished Actor Parser");
        //cp.runParser();
        System.out.println("-- Finished Cast Parser");

    }
}
