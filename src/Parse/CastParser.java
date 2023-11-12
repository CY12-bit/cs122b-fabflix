package Parse;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/*
This is the original version of the Cast Parser. Very comprehensive and works well on local machines.
 */

public class CastParser extends DefaultHandler {
    private Connection parser_conn = null;

    private HashMap<String,HashMap<String,Integer>> currentActors = new HashMap<String,HashMap<String,Integer>>();

    private ArrayList<String[]> movieStarPairs = new ArrayList<String[]>();
    private boolean validateMovie = false;
    private String[] existingMovieInfo = null;

    int nextId;
    private int cast_counter = 0;
    private HashMap<String,String> tempCast = new HashMap<String,String>();
    private String tempVal;
    private MovieObject tempMovie;
    private String tempDirector;
    private Actor tempActor;

    // Database Connection Methods
    // Function establishes a database connection
    private void establishConnection() throws Exception {
        if (parser_conn == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            parser_conn = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://localhost:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);
            parser_conn.setAutoCommit(false);
        }
    }
    // Function closes the database connection
    private void closeConnection() throws SQLException {
        if (parser_conn != null) {
            parser_conn.close();
            parser_conn = null;
        }
    }

    private void getCurrentActors() throws SQLException {
        ResultSet actor_results = parser_conn.createStatement().executeQuery(
                "SELECT DISTINCT name,id,birthYear FROM stars;"
        );
        while (actor_results.next()) {
            if (!currentActors.containsKey(actor_results.getString("name"))) {
                currentActors.put(actor_results.getString("name"),new HashMap<String,Integer>());
            }
            Integer tempYear = actor_results.getInt("birthYear");
            if (tempYear == 0) {tempYear = null; }
            currentActors.get(actor_results.getString("name")).put(
                    actor_results.getString("id"), tempYear
            );
        }
        actor_results.close();
    }

    private int getHighestId() throws SQLException {
        String highestIdQuery = "SELECT id FROM stars " +
                "ORDER BY SUBSTRING(id, 3) DESC " +
                "LIMIT 1";
        PreparedStatement highestIdStatement = parser_conn.prepareStatement(highestIdQuery);
        ResultSet highestId = highestIdStatement.executeQuery();
        String idStr = "";
        while (highestId.next()) {
            idStr = highestId.getString("id");
        }
        highestIdStatement.close();
        highestId.close();
        return Integer.parseInt(idStr.substring(2));
    }
    private String[] getMovieIfExists(final String id, final String t, final String d) throws SQLException {
        PreparedStatement movie_stmt;
        String[] queries = {
                "SELECT id, year FROM movies WHERE title = ? AND director = ? AND (LENGTH(id) = 9 OR id LIKE ?) LIMIT 1",
                "SELECT id, year, director FROM movies WHERE title = ? AND (LENGTH(id) = 9 OR id LIKE ?)",
                "SELECT id, year FROM movies WHERE title = ? AND director = ? LIMIT 1",
                "SELECT id, year FROM movies WHERE title = ? AND (LENGTH(id) = 9 OR id LIKE ?) LIMIT 1"
        };
        int queryCounter = 1;
        ResultSet movie_results = null;
        String[] movie_info = null;
        for (String q : queries) {
            movie_stmt = parser_conn.prepareStatement(q);
            if (queryCounter == 1) {
                movie_stmt.setString(1,t);
                movie_stmt.setString(2,d);
                movie_stmt.setString(3,id+"%");
                movie_results = movie_stmt.executeQuery();
                while (movie_results.next()) {
                    movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
                }
                if (movie_info != null) {break; }
            }
            else if (queryCounter == 2) {
                movie_stmt.setString(1,t);
                movie_stmt.setString(2,id+"%");
                movie_results = movie_stmt.executeQuery();
                String[] tempDirector = d.split("[ ]|[\\.]");
                while (movie_results.next()) {
                    String[] mDirector = movie_results.getString("director").split("[ ]|[\\.]|(?=\\p{Lu})");
                    if (tempDirector.length == 1 || mDirector.length == 1) {
                        if (tempDirector[0].equalsIgnoreCase(mDirector[mDirector.length-1])) {
                            movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
                        }
                    }
                    else {
                        if (tempDirector[0].substring(0,1).equalsIgnoreCase(mDirector[0].substring(0,1))) {
                            if (tempDirector[tempDirector.length-1].equalsIgnoreCase(mDirector[mDirector.length-1])) {
                                movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
                            }
                        }
                    }
                }
                if (movie_info != null) {break; }
            }
            else if (queryCounter == 3) {
                movie_stmt.setString(1,t);
                movie_stmt.setString(2,d);
                movie_results = movie_stmt.executeQuery();
                while (movie_results.next()) {
                    movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
                }
                if (movie_info != null) {break; }
            }
            else {
                movie_stmt.setString(1,t);
                movie_stmt.setString(2,id+"%");
                movie_results = movie_stmt.executeQuery();
                while (movie_results.next()) {
                    movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
                }
                break;
            }
            movie_results.close();
            queryCounter++;
        }
        movie_results.close();
        return movie_info;
    }
    private String getStarIfExists(final Integer movieYear) {
        String starId = null;
        if (currentActors.containsKey(tempActor.getName())) {
            Integer year = null;
            // We want to grab the star with an actual birthYear and who is farthest away from the movie year
            for (Map.Entry<String, Integer> entry : currentActors.get(tempActor.getName()).entrySet()) {
                if (entry.getValue() != null) {
                    if (movieYear == null || entry.getValue() <= movieYear) {
                        if (year == null || (entry.getValue() <= year)) {
                            starId = entry.getKey();
                            year = entry.getValue();
                        }
                    }
                }
                else if (year == null) { // Hopefully this is good...
                    starId = entry.getKey();
                }
            }
        }
        else if (tempCast.containsKey(tempActor.getName())) {
            starId = tempCast.get(tempActor.getName());
        }
        return starId;
    }
    private String newStarId() {
        String new_id = "nm" + nextId;
        nextId++;
        return new_id;
    }
    // Parsing Methods
    public void runParser() {
        parseDocument();
    }
    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();

            establishConnection();
            nextId = getHighestId() + 1;
            getCurrentActors();

            // Parse and import XML data into moviedb
            sp.parse("casts124.xml",this);

            insertBatch();
            tempCast.clear();
            movieStarPairs.clear();
            cast_counter = 0;

            closeConnection();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        catch (Exception E) {
            E.printStackTrace();
        }
    }
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("dirfilms")) {
            if (cast_counter >= 600) {
                System.out.println("- Batch Size: " + cast_counter);
                insertBatch();
                tempCast.clear();
                movieStarPairs.clear();
                cast_counter = 0;
                try {
                    getCurrentActors();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tempDirector = "[Unknown]";
        }
        else if (qName.equalsIgnoreCase("filmc")) {
            tempMovie = new MovieObject();
            tempMovie.setDirector(tempDirector);
        }
        else if (qName.equalsIgnoreCase(("a"))) {
            tempActor = null;
        }
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("filmc")) {
            if (existingMovieInfo == null) {
                System.out.println("Movie Not Found: " + tempMovie.getTitle());
            }
            validateMovie = false;
            existingMovieInfo = null;
        }
        else if (qName.equalsIgnoreCase("m")) {
            // Check if the movie is already in the database
            try {
                if (!validateMovie) {
                    existingMovieInfo = getMovieIfExists(tempMovie.getId(),tempMovie.getTitle(),tempMovie.getDirector());
                    validateMovie = true;
                }
                else if (existingMovieInfo != null) {
                    tempMovie.setId(existingMovieInfo[0]);
                    tempMovie.setYear(existingMovieInfo[1]);
                    if (tempActor != null) {
                        if (!tempActor.getName().matches("^[a|s|S|A][ ]*[a|s|S|A]([ ]*[a|s]?)*$")
                                && !tempActor.getName().equals(" ")
                                && !tempActor.getName().matches("[\\d]+")) {
                            String starId = getStarIfExists(tempMovie.getYear());
                            if (starId == null) {
                                starId = newStarId();
                                tempCast.put(tempActor.getName(),starId);
                            }
                            tempActor.setId(starId);
                            final String[] tempPair = {tempActor.getId(),tempMovie.getId()};
                            movieStarPairs.add(tempPair);
                            cast_counter++;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        else if (qName.equalsIgnoreCase("is")) {
            tempDirector = tempVal;
        }
        else if (qName.equalsIgnoreCase("f")) {
            tempMovie.setId(tempVal);
        }
        else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("a")) {
            if (tempVal != "") {
                tempActor = new Actor();
                tempActor.setName(tempVal);
            }
        }
    }
    public static void main(String[] args) {
        CastParser cp = new CastParser();
        cp.runParser();
    }

    // Batch Insertion Methods
    private void insertBatch() {
        try {
            String stars_in_movies_query = "INSERT INTO stars_in_movies (starId,movieId) SELECT * FROM ( VALUES " +
                    "ROW(?,?),\n".repeat(movieStarPairs.size());
            stars_in_movies_query = stars_in_movies_query.substring(0,stars_in_movies_query.length()-2);
            stars_in_movies_query += ") as newPairs(starId,movieId) " +
                    "WHERE NOT EXISTS (" + "SELECT 1 FROM stars_in_movies AS sm " + "WHERE sm.starId = newPairs.starId AND "
                    + "sm.movieId=newPairs.movieId);";
            String stars_query = "INSERT INTO stars (id,name) VALUES " + "(?,?),\n".repeat(tempCast.size());
            stars_query = stars_query.substring(0,stars_query.length()-2)+";";

            PreparedStatement stars_in_movies_prep = parser_conn.prepareStatement(stars_in_movies_query);
            PreparedStatement stars_prep = parser_conn.prepareStatement(stars_query);

            int index = 1;

            for (Map.Entry<String, String> entry : tempCast.entrySet()) {
                stars_prep.setString(index,entry.getValue());
                stars_prep.setString(index+1,entry.getKey());
                index+=2;
            }
            if (!stars_prep.toString().contains("INSERT INTO stars (id,name) VALUE;")) {
                stars_prep.executeUpdate();
                parser_conn.commit();
            }
            index=1;
            for (String[] p : movieStarPairs) {
                stars_in_movies_prep.setString(index,p[0]);
                stars_in_movies_prep.setString(index+1,p[1]);
                index+=2;
            }

            stars_in_movies_prep.executeUpdate();
            parser_conn.commit();
            System.out.println("-- Successfully Exported Batch");
        } catch (Exception E) {
            System.out.println("-- batch failed: " + E.getMessage());
            E.printStackTrace();
        }

    }
}
