package Parse;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import Parse.MovieObject;
import Parse.Actor;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastParser extends DefaultHandler {

    private Connection parser_conn = null;
    private PreparedStatement movie_stmt;
    private PreparedStatement star_stmt;
    int nextId;

    private int cast_counter = 0;
    private HashMap<String,String> tempCast = new HashMap<String,String>();

    private String tempVal;

    private MovieObject tempMovie;
    private String tempDirector;

    private Actor tempActor;

    // Function establishes a database connection
    private void establishConnection() throws Exception {
        if (parser_conn == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            parser_conn = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://localhost:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);
            parser_conn.setAutoCommit(false);
            movie_stmt = parser_conn.prepareStatement("INSERT INTO stars_in_movies(movieId,starId) VALUES (?,?);");
            star_stmt = parser_conn.prepareStatement("INSERT INTO stars(id,name) VALUES (?,?);");
        }
    }

    // Function closes the database connection
    private void closeConnection() throws SQLException {
        if (parser_conn != null) {
            movie_stmt.close();
            star_stmt.close();
            parser_conn.close();
            parser_conn = null;
        }
    }

    // Function checks if the movie already exists in the database
    // Checks the title, year, and director
    // E.g. Lost in Translation was already in the movie db
    // If there are multiple movies with the same triple, check their ids for most matching;
    private String[] getMovieIfExists(final String id, final String t, final String d) throws SQLException {
        String movieQuery = "SELECT id, year FROM movies WHERE title = ? AND director = ?" +
                " AND (LENGTH(id) = 9 OR id LIKE ?) LIMIT 1";
        PreparedStatement movie_stmt = parser_conn.prepareStatement(movieQuery);
        movie_stmt.setString(1,t);
        movie_stmt.setString(2,d);
        movie_stmt.setString(3,id+"%");
        ResultSet movie_results = movie_stmt.executeQuery();
        String[] movie_info = null;
        int counter = 1;
        while (movie_results.next()) {
            if (counter > 1) {
                System.out.println("More than One Result for: " + id + ", " + t);
            }
            else if (counter == 1) {
                movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
            }
            counter++;
        }

        return movie_info;
    }

    // Function checks if the star is already in the database
    // Checks their name and date of birth
    // If it returns multiple people (e.g. John Howard), then returns the first star mentioned?
    private String getStarIfExists() throws SQLException {
        String starQuery;
        PreparedStatement select_star_stmt;
        if (tempMovie.getYear() != 0) {
            starQuery = "SELECT * FROM stars WHERE name = ? AND (birthYear <= ? OR birthYear IS NULL) ORDER BY -birthYear DESC";
            select_star_stmt = parser_conn.prepareStatement(starQuery);
            select_star_stmt.setString(1,tempActor.getName());
            select_star_stmt.setInt(2,tempMovie.getYear());
        }
        else {
            starQuery = "SELECT id FROM star WHERE name = ?";
            select_star_stmt = parser_conn.prepareStatement(starQuery);
            select_star_stmt.setString(1,tempActor.getName());
        }

        ResultSet select_star_result = select_star_stmt.executeQuery();
        String starId = null;
        int counter = 1;
        while (select_star_result.next()) {
            if (counter > 1) {
                System.out.println("More than One Star: ("+
                        select_star_result.getString("id")+","+
                        select_star_result.getString("name")+","+
                        select_star_result.getString("birthYear")+")"
                );
            }
            else {
                starId = select_star_result.getString("id");
            }
            counter++;
        }

        if (starId == null && tempCast.containsKey(tempActor.getName())) {
            starId = tempCast.get(tempActor.getName());
        }

        return starId;
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
        return Integer.parseInt(idStr.substring(2));
    }

    // Function creates a new star in the movie database
    // birthYear is always null though
    private String insertStar() throws SQLException {
        String new_id = "nm" + nextId;
        star_stmt.setString(1,new_id);
        star_stmt.setString(2,tempActor.getName());
        star_stmt.addBatch();
        nextId++;
        return new_id;
    }

    private void insertBatch() {
        try {
            star_stmt.executeLargeBatch();
            parser_conn.commit();
            movie_stmt.executeLargeBatch();
            parser_conn.commit();
        } catch (SQLException e) {
            System.out.println("-- batch failed: " + e.getMessage());
        }
    }

    public void runParser() {
        parseDocument();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();

            establishConnection();
            nextId = getHighestId() + 1;
            // Parse and import XML data into moviedb
            sp.parse("casts124 - Light.xml",this);
            insertBatch();

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
        if (qName.equalsIgnoreCase(" dirfilms")) {
            if (cast_counter >= 500) {
                System.out.println("- Batch Size: " + cast_counter);
                insertBatch();
                tempCast.clear();
                System.out.println("-- Successfully inserted batch.");
            }
        }
        if (qName.equalsIgnoreCase("m")) {
            // Check if the movie is already in the database
            try {
                final String[] existingMovieInfo = getMovieIfExists(tempMovie.getId(),tempMovie.getTitle(),tempMovie.getDirector());
                if (existingMovieInfo != null) {
                    tempMovie.setId(existingMovieInfo[0]);
                    tempMovie.setYear(existingMovieInfo[1]);
                    if (tempActor != null) {
                        if (!tempActor.getName().matches("^[a|s][ ]*[a|s]([ ]*[a|s]?)*$")  && !tempActor.getName().equals(" ")
                                && !tempActor.getName().matches("[\\d]+")) {
                            String starId = getStarIfExists();
                            if (starId == null) {
                                System.out.println("- Inserting New Star: "  + tempActor.getName());
                                starId = insertStar();
                                tempCast.put(tempActor.getName(),starId);
                            }
                            tempActor.setId(starId);
                        }
                        movie_stmt.setString(1,tempMovie.getId());
                        movie_stmt.setString(2,tempActor.getId());
                        movie_stmt.addBatch();
                        cast_counter++;
                    }
                }
                else {
                    System.out.println("-- Can't Find Movie: " + tempMovie.getTitle());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if (qName.equalsIgnoreCase("is")) {
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
}
