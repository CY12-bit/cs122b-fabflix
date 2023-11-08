package Parse;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
import javax.xml.transform.Result;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastParser extends DefaultHandler {

    private Connection parser_conn = null;
    private PreparedStatement movie_stmt;
    private PreparedStatement star_stmt;
    int nextId;

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
            movie_stmt = parser_conn.prepareStatement("INSERT INTO stars_in_movies(starId,movieId) VALUES (?,?);");
            star_stmt = parser_conn.prepareStatement("INSERT INTO stars(id,name) VALUES (?,?)");
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
        String movieQuery = "SELECT id,year FROM movies WHERE title = ? AND director = ?" +
                " AND (LENGTH(id) == 9 OR id LIKE ?) LIMIT 1";
        PreparedStatement movie_stmt = parser_conn.prepareStatement(movieQuery);
        movie_stmt.setString(1,t);
        movie_stmt.setString(2,d);
        movie_stmt.setString(3,id+"%");
        ResultSet movie_results = movie_stmt.executeQuery();
        String[] movie_info = null;
        if (movie_results.isBeforeFirst()) {
            movie_results.last();
            if (movie_results.getRow() > 1) {
                System.out.println("More than One Result for: " + id + ", " + t);
            }
            movie_info = new String[]{movie_results.getString("id"), Integer.toString(movie_results.getInt("year"))};
        }

        return movie_info;
    }

    // Function checks if the star is already in the database
    // Checks their name and date of birth
    // If it returns multiple people (e.g. John Howard), then returns the first star mentioned?
    private String getStarIfExists(final String starName, final Integer movie_year) throws SQLException {
        String starQuery;
        PreparedStatement select_star_stmt;
        if (movie_year != null) {
            starQuery = "SELECT id FROM star WHERE name = ? AND birthYear <= ?";
            select_star_stmt = parser_conn.prepareStatement(starQuery);
            select_star_stmt.setString(1,starName);
            select_star_stmt.setInt(2,movie_year);
        }
        else {
            starQuery = "SELECT id FROM star WHERE name = ?";
            select_star_stmt = parser_conn.prepareStatement(starQuery);
            select_star_stmt.setString(1,starName);
        }

        return select_star_stmt.executeQuery().getString("id");
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
        return new_id;
    }

    private void insertBatch() {
        try {
            movie_stmt.executeLargeBatch();
            star_stmt.executeLargeBatch();
            parser_conn.commit();
        } catch (Exception E) {

        }


    }

    public void runParser() {
        try {
            nextId = getHighestId() + 1;
            parseDocument();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();

            establishConnection();

            // Parse and import XML data into moviedb
            sp.parse("cast243.xml",this);

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
            System.out.println("Error: " + E.getMessage());
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
            tempActor = new Actor();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            // Check if the movie is already in the database
            try {
                final String[] existingMovieInfo = getMovieIfExists(tempMovie.getId(),tempMovie.getTitle(),tempMovie.getDirector());
                if (existingMovieInfo != null) {
                    tempMovie.setId(existingMovieInfo[0]);
                    String starId = getStarIfExists(tempActor.getName(),Integer.parseInt(existingMovieInfo[1]));
                    if (starId == null) {
                        starId = insertStar();
                    }
                    tempActor.setId(starId);

                    movie_stmt.setString(1,tempMovie.getId());
                    movie_stmt.setString(2,tempActor.getId());
                    movie_stmt.addBatch();
                }
                else {
                    System.out.println("Can't Find Movie: " + tempMovie.getTitle());
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
            tempActor.setName(tempVal);
        }
    }

    public static void main(String[] args) {
        CastParser cp = new CastParser();
        cp.runParser();
    }
}
