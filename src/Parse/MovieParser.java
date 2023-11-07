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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieParser extends DefaultHandler {

    private Connection parser_conn = null;

    private HashMap<String, HashSet<String>> movieIdGroups;

    private ArrayList<MovieObject> myMovies;

    private int movie_counter = 0;

    private String tempVal;
    private String tempDirector;
    private MovieObject tempMovie;

    private boolean parsingGenres = false;
    private ArrayList<String> tempGenres = new ArrayList<String>();

    public MovieParser() {
        myMovies = new ArrayList<MovieObject>();
        movieIdGroups = new HashMap<String, HashSet<String>>();
        // What happens if it's the same tag with the same name but under different directors
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
        if (qName.equalsIgnoreCase("fid")||qName.equalsIgnoreCase("filmed")) {
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

            // Check if there was a movie with the same id. If not, add the new movie to the list
            final String movieIdentifier = tempMovie.getTitle()+"|"+tempMovie.getDirector()+"|"+tempMovie.getYear();

            if(!movieIdGroups.containsKey(tempMovie.getId())) {
                myMovies.add(tempMovie);
                movieIdGroups.put(tempMovie.getId(),new HashSet<String>());
                movieIdGroups.get(tempMovie.getId()).add(movieIdentifier);
                movie_counter++;
            }
            else if (!movieIdGroups.get(tempMovie.getId()).contains(movieIdentifier)) {
                movieIdGroups.get(tempMovie.getId()).add(movieIdentifier);
                tempMovie.setId(tempMovie.getId()+"_"+(movieIdGroups.get(tempMovie.getId()).size()-1));
                myMovies.add(tempMovie);
                movie_counter++;
            }
            else {
                System.out.println("Duplicate Movie in XML: " + tempMovie.getTitle());
            }

        }
        else if (qName.equalsIgnoreCase("directorfilms")) {
            // If we reached 600 movies
            if (movie_counter >= 600) {
                System.out.println("- Batch Size: " + movie_counter);
                insertBatch();
                myMovies.clear();
                movie_counter = 0;
            }
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

    private boolean checkMovie(final MovieObject mo, final String baseId) throws SQLException {
        // I indicate whether a movie was from the original database by checking their id length
        String movieQuery = "SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?" +
                " AND (LENGTH(id) = 9 OR id LIKE ?)";
        PreparedStatement movie_stmt = parser_conn.prepareStatement(movieQuery);
        movie_stmt.setString(1,mo.getTitle());
        movie_stmt.setInt(2,mo.getYear());
        movie_stmt.setString(3,mo.getDirector());
        movie_stmt.setString(4,baseId+"%");
        return movie_stmt.executeQuery().next();
    }



    private void insertBatch() {
        try {
            int nextGenreId = getHighestGenreID() + 1;

            PreparedStatement movie_prep = parser_conn.prepareStatement("INSERT INTO movies (id,title,year,director) VALUES (?,?,?,?);");
            PreparedStatement genre_in_movie_prep = parser_conn.prepareStatement("INSERT INTO genres_in_movies (genreId,movieId) VALUES (?,?);");
            PreparedStatement genre_prep = parser_conn.prepareStatement("INSERT INTO genres (id,name) VALUES (?,?);");

            // Retrieve all current genres in the moviedb
            HashMap<String,Integer> all_genres = getAllGenres();
            for (final MovieObject movie : myMovies) {
                String base_id = movie.getId().split("_")[0];
                if (!checkMovie(movie,base_id)) {
                    movie_prep.setString(1,movie.getId());
                    movie_prep.setString(2,movie.getTitle());
                    movie_prep.setInt(3,movie.getYear());
                    movie_prep.setString(4,movie.getDirector());
                    movie_prep.addBatch();

                    // Generate queries to insert values into genre and genre_in_movies
                    for (String g : movie.getGenres()) {
                        // If the genre doesn't exist, we add an insert query with the appropriate id
                        if (!all_genres.containsKey(g)) {
                            genre_prep.setInt(1,nextGenreId);
                            genre_prep.setString(2,g);
                            genre_prep.addBatch();
                            all_genres.put(g,nextGenreId);
                            nextGenreId++;
                        }
                        genre_in_movie_prep.setInt(1,all_genres.get(g));
                        genre_in_movie_prep.setString(2,movie.getId());
                        genre_in_movie_prep.addBatch();

                    }
                }
                else {
                    System.out.println("Movie Already in DB: " + movie.getTitle());
                }
            }

            movie_prep.executeLargeBatch();
            genre_prep.executeLargeBatch();
            genre_in_movie_prep.executeLargeBatch();

            parser_conn.commit();

            movie_prep.close();
            genre_prep.close();
            genre_in_movie_prep.close();

            System.out.println("-- Successfully Inserted Batch");

        } catch (Exception e) {
            System.out.println("-- batch failed: " + e.getMessage());
            return;
        }
    }

    public HashMap<String,String[]> getMovieCollection() {
        HashMap<String, String[]> movie_id_pairs = new HashMap<String, String[]>();
        for (final MovieObject m : myMovies) {
            movie_id_pairs.put(m.getId(), new String[]{m.getTitle(), m.getDirector()});
        }
        return movie_id_pairs;
    }

    public static void main(String[] args) {
        MovieParser spe = new MovieParser();
        spe.runParser();
    }
}