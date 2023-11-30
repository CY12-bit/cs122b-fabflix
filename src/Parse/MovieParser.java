package Parse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.*;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Objects;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieParser extends DefaultHandler {
    private BufferedWriter errorWriter;

    // Database Connection
    private Connection parser_conn;

    /*
    == Movie-related Data Structures ==
     */
    // Structure is used to identify movies with duplicate Ids
    private HashMap<String, HashSet<String>> movieIdGroups;
    // Structure houses cleaned Movie Objects that will be turned into insert statements
    private ArrayList<MovieObject> myMovies;
    // Structure counts how many movies were processed
    private int movie_counter;

    /*
    Genre-related Data Structures
     */
    // Variable indicates whether the genre should be parsed or not
    int nextGenreId;
    // Variable indicates how many total genres are we assigning to each movie
    private int genre_movie_counter;
    private boolean parseGenre;
    // Structure contains all current genres in the database
    private HashMap<String,Integer> current_genres;
    // Structure contains all new genres that'll be added to the database
    private HashSet<String> new_genres;

    /*
    Insertion Related Data Structures
     */
    private ArrayList<ArrayList<PreparedStatement>> batchStatements;

    /*
    Parsing Related
     */
    private String tempVal;
    private String tempDirector;
    private MovieObject tempMovie;
    private ArrayList<String> tempGenres = new ArrayList<String>();
    private HashMap<String, MovieObject> movieMap;

    // Initializer
    public MovieParser() {
        parser_conn = null;
        movieIdGroups = new HashMap<String, HashSet<String>>();
        myMovies = new ArrayList<MovieObject>();
        current_genres = new HashMap<String, Integer>();
        new_genres = new HashSet<String>();
        parseGenre = false;
        genre_movie_counter = 0;
        batchStatements = new ArrayList<ArrayList<PreparedStatement>>();
        movieMap = new HashMap<>();
        try {
            errorWriter = new BufferedWriter(new FileWriter("movieLogs.txt"));
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    // SAX Parsing Methods
    public void runParser() {parseDocument();}
    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();

            // Establishes Database Connection
            establishConnection();

            // Get all Genres currently in the database
            getAllGenresInDB();
            nextGenreId = getHighestGenreID() + 1;

            // Parse and import XML data into moviedb
            sp.parse("mains243.xml",this);

            // If we have any remaining movies in the set, we add them to the database
            loadBatch();
            exportBatches();
            clearBatches();

            errorWriter.close();

            // Closes database connection
            closeConnection();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (Exception E) {
            System.out.println("Error: " + E.getMessage());
        }
    }
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("directorfilms")) {
            tempDirector = null;
        }
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new MovieObject();
        }
        else if (qName.equalsIgnoreCase("cattext")) {
            tempVal = "[NO PARSE]";
        }
        else if (qName.equalsIgnoreCase("cats")) {
            parseGenre = true;
        }
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (parseGenre && !Objects.equals(tempVal, "[NO PARSE]")) {
            final String[] genres = new String(ch, start, length).split(" ");
            for (final String g: genres) {
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
        else if (qName.equalsIgnoreCase("dirname") ||
                (qName.equalsIgnoreCase("dirn") && tempDirector == null)) {
            tempDirector = tempVal;
        }
        else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempVal);
        }
        else if (qName.equalsIgnoreCase("cats")) {
            parseGenre = false;
            tempMovie.addGenres(tempGenres);
            tempGenres.clear();
        }
        else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(tempDirector);
            // Check if there are genres. Otherwise, mark it as Uncategorized
            if (tempMovie.getGenres().isEmpty()) {
                if (!checkIfGenreParsed("Uncategorized")) {
                    new_genres.add("Uncategorized");
                }
                tempMovie.addGenre("ctcxx");
            }
            else {
                for (String g : tempMovie.getGenres()) {
                    if (!checkIfGenreParsed(g)) {
                        new_genres.add(g);
                    }
                }
            }
            try {
                boolean checkIfMovieNotExists = checkMovie();
                if (checkIfMovieNotExists) { // If the movie doesn't already exist in the database
                    genre_movie_counter += tempMovie.getGenres().size();
                }
            } catch (Exception E) {
                System.out.println(E.getMessage());
            }
        }
        else if (qName.equalsIgnoreCase("directorfilms")) {
            // If we reached 600 movies
            if (movie_counter >= 600) {
                loadBatch();
                if (batchStatements.size() == 3) {
                    exportBatches();
                    clearBatches();
                }
            }
        }
    }

    // Database Connection Methods
    private void establishConnection() throws Exception {
        if (parser_conn == null) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            parser_conn = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://172.31.0.193:3306/" + DBInfo.dbname,
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

    // Movie Check Methods
    private boolean checkMovie() {
        // Check if there was a movie with the same id. If not, add the new movie to the list
        final String movieIdentifier = tempMovie.getTitle()+"|"+tempMovie.getDirector()+"|"+tempMovie.getYear();

        // If the movie id hasn't been processed already
        if(!movieIdGroups.containsKey(tempMovie.getId())) {
            myMovies.add(tempMovie);
            movieIdGroups.put(tempMovie.getId(),new HashSet<String>());
            movieIdGroups.get(tempMovie.getId()).add(movieIdentifier);
            movieMap.put(tempMovie.getId(), tempMovie);
            movie_counter++;
            return true;
        }
        // If we have came across the movie before but they have different movie information, then we still insert it
        // with a new movie ID we generate
        else if (!movieIdGroups.get(tempMovie.getId()).contains(movieIdentifier)) {
            try {
                errorWriter.write("Same movie ID but diff. movie info. Generating new id for "+tempMovie.getId());
                errorWriter.newLine();
            } catch (Exception E) {
                E.printStackTrace();
            }
            System.out.println("Same movie ID but diff. movie info. Generating new id for "+tempMovie.getId());
            movieIdGroups.get(tempMovie.getId()).add(movieIdentifier);
            tempMovie.setId(tempMovie.getId()+"_"+(movieIdGroups.get(tempMovie.getId()).size()-1));
            myMovies.add(tempMovie);
            movieMap.put(tempMovie.getId(), tempMovie);
            movie_counter++;
            return true;
        }
        else {
            System.out.println("Duplicate Movie in XML: " + tempMovie.getTitle());
            return false;
        }
    }

    // Genre Checking Methods
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

    private void getAllGenresInDB() throws SQLException {
        String allGenresQuery = "SELECT DISTINCT name,id FROM genres";
        ResultSet genre_results = parser_conn.createStatement().executeQuery(allGenresQuery);
        while (genre_results.next()) {
            current_genres.put(genre_results.getString("name"),genre_results.getInt("id"));
        }
        genre_results.close();
    }

    public HashMap<String, MovieObject> getMovieMap() {
        return movieMap;
    }

    public HashMap<String, HashSet<String>> getMovieIdGroups() {
        return movieIdGroups;
    }

    private boolean checkIfGenreParsed(final String g) { return new_genres.contains(g) || current_genres.containsKey(g); }

    // Batch Insertion Methods
    private void loadBatch() {
        try {
            System.out.println("-- Load Batch");
            // Create 3 new prepared statements
            System.out.println("Batch Size: " + myMovies.size());
            String movie_query = "INSERT INTO movies (id, title, year, director) " +
                    "SELECT id,title,year,director FROM ( VALUES ";
            String rows = "ROW(?,?,?,?,?,?),\n".repeat(myMovies.size());
            rows = rows.substring(0, rows.length() - 2);
            String end = ") as newMovies (id, title, year, director, year_str, base_id)" +
                    " WHERE NOT EXISTS (" +
                    "SELECT 1 FROM movies as m " +
                    "WHERE m.title = newMovies.title AND m.director = newMovies.director AND " +
                    "(m.year = newMovies.year OR CAST(m.year AS CHAR) LIKE CONCAT(newMovies.year_str,'%')) AND " +
                    "(LENGTH(m.id) = 9 OR m.id LIKE CONCAT(newMovies.base_id,'%')));";
            movie_query += rows + end;
            String genre_query = "INSERT INTO genres (id,name) VALUES " +
                    "(?,?),".repeat(new_genres.size());
            genre_query = genre_query.substring(0, genre_query.length() - 1) + ";";
            String genre_in_movies_query = "INSERT INTO genres_in_movies (genreId,movieId) " +
                    "SELECT * FROM ( VALUES " + "ROW(?,?),".repeat(genre_movie_counter);
            genre_in_movies_query = genre_in_movies_query.substring(0, genre_in_movies_query.length() - 1);
            genre_in_movies_query += ") AS newPairs(genreId,movieId) WHERE NOT EXISTS (" +
                    "SELECT 1 FROM genres_in_movies as gm WHERE gm.genreId = newPairs.genreId AND " +
                    "gm.movieId = newPairs.movieId) AND EXISTS (SELECT id FROM movies WHERE id = newPairs.movieId);";

            PreparedStatement movie_prep = parser_conn.prepareStatement(movie_query);
            PreparedStatement genre_prep = parser_conn.prepareStatement(genre_query);
            PreparedStatement genre_in_movie_prep = parser_conn.prepareStatement(genre_in_movies_query);

            int index = 1;
            for (String g : new_genres) {
                genre_prep.setInt(index, nextGenreId);
                genre_prep.setString(index + 1, g);
                current_genres.put(g, nextGenreId);
                nextGenreId++;
                index += 2;
            }

            new_genres.clear();

            index = 1;
            int genre_in_movies_index = 1;
            for (final MovieObject movie : myMovies) {
                String base_id = movie.getId().split("_")[0];
                movie_prep.setString(index, movie.getId());
                movie_prep.setString(index + 1, movie.getTitle());
                movie_prep.setInt(index + 2, movie.getYear());
                movie_prep.setString(index + 3, movie.getDirector());
                movie_prep.setString(index + 4, movie.getYear_str());
                movie_prep.setString(index + 5, base_id);
                for (String g : movie.getGenres()) {
                    genre_in_movie_prep.setInt(genre_in_movies_index, current_genres.get(g));
                    genre_in_movie_prep.setString(genre_in_movies_index+1, movie.getId());
                    genre_in_movies_index+=2;
                }
                index += 6;
            }

            ArrayList<PreparedStatement> tempBatch = new ArrayList<PreparedStatement>();
            tempBatch.add(movie_prep);
            tempBatch.add(genre_prep);
            tempBatch.add(genre_in_movie_prep);
            batchStatements.add(tempBatch);
            myMovies.clear();
            movie_counter = 0;
            genre_movie_counter = 0;
        } catch (Exception E) {
            E.printStackTrace();
        }
    }
    private void exportBatches() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < batchStatements.size(); i++) {
            try {
                QueryWorker worker = new QueryWorker(batchStatements.get(i), parser_conn,i);
                executor.execute(worker);
            } catch (Exception E) {
                E.printStackTrace();
            }
        }
        executor.shutdown();
        while(!executor.isTerminated()) {}
    }
    private void clearBatches() {
        try {
            batchStatements.clear();
            getAllGenresInDB();
            nextGenreId = getHighestGenreID() + 1;
        } catch (Exception E) {
            System.out.println("Unable to Grab Current Genres");
        }
    }


    // Multi-Threading Methods
    static class QueryWorker implements Runnable {

        Random random;

        QueryWorker(final ArrayList<PreparedStatement> p_list, final Connection c, int i)
        throws Exception {
            random = new Random(i);
            p_list.get(0).executeUpdate();
            if(!p_list.get(1).toString().contains("INSERT INTO genres (id,name) VALUES;"))
            {
                p_list.get(1).executeUpdate();
            }
            p_list.get(2).executeUpdate();

            c.commit();
            System.out.println("-- Batch Successfully Committed");
            p_list.get(0).close();
            p_list.get(1).close();
            p_list.get(2).close();
        }

        @Override
        public void run() {
            try {
                Thread.sleep(random.nextInt(5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        MovieParser spe = new MovieParser();
        spe.runParser();
    }
}
