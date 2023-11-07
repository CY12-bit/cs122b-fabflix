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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class CastParser extends DefaultHandler {

    private Connection parser_conn = null;

    private ArrayList<String> idMappings;
    private String tempVal;

    public CastParser(ArrayList<String> idMappings) {
        this.idMappings = idMappings;
    }

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

    // Function checks if the movie already exists in the database
    // Checks the title, year, and director
    // E.g. Lost in Translation was already in the movie db
    // If there are multiple movies with the same triple, check their ids for most matching;
    private String getMovieIfExists(final String t, final int y, final String d) throws SQLException {
        String movieQuery = "SELECT id FROM movies WHERE title = ? and year = ? and director = ?";
        PreparedStatement movie_stmt = parser_conn.prepareStatement(movieQuery);
        movie_stmt.setString(1,t);
        movie_stmt.setInt(2,y);
        movie_stmt.setString(3,d);
        ResultSet movie_results = movie_stmt.executeQuery();
        movie_results.last();
        if (movie_results.getRow() > 1) {
            movie_results.first();
            while (movie_results.next()) {

            }
        }
        return "";
    }

    // Function checks if the star is already in the database
    // Checks their name and date of birth
    // If it returns multiple people (e.g. John Howard), then returns the first star mentioned?
    private String getStarIfExists(final String starName, final int dob_year) throws SQLException {
        String starQuery = "SELECT id FROM star WHERE name = ? AND year = ?";
        PreparedStatement star_stmt = parser_conn.prepareStatement(starQuery);
        star_stmt.setString(1,starName);
        star_stmt.setInt(2,dob_year);
        return star_stmt.executeQuery().getString("id");
    }

    private void insertBatch() {

    }

    public void runParser() {

    }

    private void parseDocument() {

    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

    }

    public void characters(char[] ch, int start, int length) throws SAXException {

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

    }

    public static void main(String[] args) {

    }
}
