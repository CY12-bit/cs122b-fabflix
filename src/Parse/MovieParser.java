package Parse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieParser extends DefaultHandler {

    private Connection parser_conn = null;

    HashMap<String,MovieObject> myMovies;

    private String tempVal;
    private String tempDirector;
    private MovieObject tempMovie;

    private boolean parsingGenres = false;
    private ArrayList<String> tempGenres = new ArrayList<String>();

    public MovieParser() {
        myMovies = new HashMap<String,MovieObject>();
    }

    public void runParser() {
        parseDocument();
    }

    // Function parses main243.xml
    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();

            // Establishes Database Connection
            establishConnection();

            // Parse and import XML data into moviedb
            sp.parse("mains243.xml",this);

            // If we have any remaining movies in the set, we add them to the database
            insertBatch();

            // Closes database connection
            closeConnection();
        }
        catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception E) {
            System.out.println("Error: " + E.getMessage());
        }

    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new MovieObject();
        }
        else if (qName.equalsIgnoreCase("cattext")) {
            tempVal = "[NO PARSE]";
        }
        else if (qName.equalsIgnoreCase("cats")) {
            parsingGenres = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (parsingGenres && tempVal != "[NO PARSE]") {
            final String[] genres = new String(ch, start, length).split(" ");
            for (String g: genres){
                tempGenres.add(g);
            }
        }
        else {
            tempVal = new String(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        }
        else if (qName.equalsIgnoreCase("dirname")) {
            tempDirector = tempVal;
        }
        else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempVal);
        }
        else if (qName.equalsIgnoreCase("cats")) {
            parsingGenres = false;
            tempMovie.addGenres(tempGenres);
            tempGenres.clear();
        }
        else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(tempDirector);
            // Check if there are genres. Otherwise, mark it as Uncategorized
            if (tempMovie.getGenres().isEmpty()) {
                tempMovie.addGenre("ctcxx");
            }
            // Make sure we don't add duplicates
            if (!myMovies.containsKey(tempMovie.getId())) {
                myMovies.put(tempMovie.getId(),tempMovie);
                System.out.println(tempMovie.toString());

                // If we reached 500 movies!
                if (myMovies.size() == 500) {
                    insertBatch();
                    myMovies.clear();
                }

            }
        }
        else if (qName.equalsIgnoreCase("directorfilms")) {
            tempDirector = "Unknown"; // Hopefully, no one is named Unknown in real life
        }
    }

    private void establishConnection() throws Exception {
        if (parser_conn == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            parser_conn = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://localhost:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);
            parser_conn.setAutoCommit(false);
        }

    }
    private void closeConnection() throws SQLException {
        if (parser_conn != null) {
            parser_conn.close();
            parser_conn = null;
        }
    }

    private int getHighestGenreID() throws SQLException {
        String highestIDQuery = "SELECT MAX(id) as id FROM genres";

        Statement highestIdStatement = parser_conn.createStatement();
        ResultSet highestId = highestIdStatement.executeQuery(highestIDQuery);
        int idInt = -1;
        while (highestId.next()) {
            idInt = highestId.getInt("id");
        }
        highestId.close();
        highestIdStatement.close();

        return idInt;

    }

    private HashMap<String,Integer> getAllGenres() throws SQLException {
        HashMap<String,Integer> all_genres = new HashMap<String,Integer>();
        String allGenresQuery = "SELECT DISTINCT name,id FROM genres";
        ResultSet genre_results = parser_conn.createStatement().executeQuery(allGenresQuery);
        while (genre_results.next()) {
            all_genres.put(genre_results.getString("name"),genre_results.getInt("id"));
        }
        return all_genres;
    }

    private boolean checkMovie(final MovieObject mo) throws SQLException {
        // I indicate whether a movie was from the original database by checking their id length
        String movieQuery = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ? AND (LENGTH(id) = 9 OR id = ?)";
        PreparedStatement movie_stmt = parser_conn.prepareStatement(movieQuery);
        movie_stmt.setString(1,mo.getTitle());
        movie_stmt.setInt(2,mo.getYear());
        movie_stmt.setString(3,mo.getDirector());
        movie_stmt.setString(4,mo.getId());
        return movie_stmt.executeQuery().next();
    }

    private void insertBatch() {
        // Check if the movie already exists in the database
            // If it's a movie that was from the XML, then ignore
            // If it's a movie that was from the original db, then don't insert it
        // Insert the movies into the database
        // Insert into genres and
        // Print out that it was successful
        // Since it's in batches, don't want to repeat connecting to the db over and over again
        // Or running the same queries again and again
        try {
            int nextGenreId = getHighestGenreID() + 1;
            // I should consider the case where the movie is already in the database but the actors need to be assigned to it
            // I should keep the XML's movieIds

            String movieQuery = "INSERT INTO movies (id,title,year,director) VALUES ";
            PreparedStatement movie_prep = parser_conn.prepareStatement("INSERT INTO movies (id,title,year,director) VALUES (?,?,?,?);");
            PreparedStatement genre_in_movie_prep = parser_conn.prepareStatement("INSERT INTO genres_in_movies (genreId,movieId) VALUES (?,?);");
            PreparedStatement genre_prep = parser_conn.prepareStatement("INSERT INTO genres (id,name) VALUES (?,?);");

            // Retrieve all current genres in the moviedb
            HashMap<String,Integer> all_genres = getAllGenres();

            for (final Map.Entry<String, MovieObject> movie : myMovies.entrySet()) {
                // Need to check if the movie is already in the original database
                if (!checkMovie(movie.getValue())) {
                    movie_prep.setString(1,movie.getKey());
                    movie_prep.setString(2,movie.getValue().getTitle());
                    movie_prep.setInt(3,movie.getValue().getYear());
                    movie_prep.setString(4,movie.getValue().getDirector());
                    movie_prep.addBatch();

                    // Generate queries to insert values into genre and genre_in_movies
                    for (String g : movie.getValue().getGenres()) {
                        if (!all_genres.containsKey(g)) {
                            genre_prep.setInt(1,nextGenreId);
                            genre_prep.setString(2,g);
                            genre_prep.addBatch();
                            all_genres.put(g,nextGenreId);
                        }
                        genre_in_movie_prep.setInt(1,all_genres.get(g));
                        genre_in_movie_prep.setString(2,movie.getKey());
                        genre_in_movie_prep.addBatch();
                        nextGenreId++;
                    }
                }
            }

            movie_prep.executeLargeBatch();
            genre_prep.executeLargeBatch();
            genre_in_movie_prep.executeLargeBatch();

            parser_conn.commit();

            movie_prep.close();
            genre_prep.close();
            genre_in_movie_prep.close();

            System.out.println("Successfully Inserted Batch");

        } catch (Exception e) {
            System.out.println("batch failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MovieParser spe = new MovieParser();
        spe.runParser();
    }
}