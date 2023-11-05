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

            sp.parse("mains243 (Light).xml",this);

            // If we have any remaining movies in the set, we add them to the database
            insertBatch();
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

    // When the function reaches 500 movies, it will insert those movies into the database
    // and then clear the movie list for the next chunk
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
    }

    private void establishConnection() throws Exception {
        if (parser_conn == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            parser_conn = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://localhost:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);
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
        String movieQuery = "SELECT id FROM movies WHERE LENGTH(id) = 9 AND title = ? AND year = ? AND director = ?";
        PreparedStatement movie_stmt = parser_conn.prepareStatement(movieQuery);
        movie_stmt.setString(1,mo.getTitle());
        movie_stmt.setInt(2,mo.getYear());
        movie_stmt.setString(3,mo.getDirector());
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
            establishConnection();
            if (parser_conn != null) {
                int nextGenreId = getHighestGenreID() + 1;
                // I should consider the case where the movie is already in the database but the actors need to be assigned to it
                // I should keep the XML's movieIds

                String movieQuery = "INSERT INTO movies (id,title,year,director) VALUES ";

                Statement insertStatement = parser_conn.createStatement();

                // Retrieve all current genres in the moviedb
                HashMap<String,Integer> all_genres = getAllGenres();

                String genre_query = "INSERT INTO genres (id,name) VALUES ";
                String genre_in_movies_query = "INSERT INTO genres_in_movies (genreId,movieId) VALUES ";

                for (Map.Entry<String, MovieObject> movie : myMovies.entrySet()) {
                    // Need to check if the movie is already in the original database
                    if (!checkMovie(movie.getValue())) {
                        movieQuery += String.format("(%1$s,%2$s,%3$d,%4$s), ",movie.getKey(),movie.getValue().getTitle(),
                                movie.getValue().getYear(),movie.getValue().getDirector());

                        // Generate queries to insert values into genre and genre_in_movies
                        for (String g : movie.getValue().getGenres()) {
                            if (!all_genres.containsKey(g)) {
                                genre_query += String.format("(%1$d,%2$s), ",nextGenreId, g);
                                all_genres.put(g,nextGenreId);
                            }
                            genre_in_movies_query += String.format("(%1$d,%2$s), ", all_genres.get(g), movie.getKey());
                            nextGenreId++;
                        }
                    }

                }

                // Execute import into movies
                if (!movieQuery.equals("INSERT INTO movies (id,title,year,director) VALUES ")) {
                    movieQuery = movieQuery.substring(0, movieQuery.length()-2);
                    insertStatement.addBatch(movieQuery);
                    if (!genre_query.equals("INSERT INTO genres (id,name) VALUES ")) {
                        genre_query = genre_query.substring(0, genre_query.length()-2);
                        insertStatement.addBatch(genre_query);
                    }
                    genre_in_movies_query = genre_in_movies_query.substring(0, genre_in_movies_query.length()-2);
                    insertStatement.addBatch(genre_in_movies_query);
                }
                insertStatement.executeLargeBatch();

                parser_conn.commit();

                insertStatement.close();

                System.out.println("Successfully Inserted Batch");
            }
        } catch (Exception e) {
            System.out.println("batch failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MovieParser spe = new MovieParser();
        spe.runParser();
    }
}