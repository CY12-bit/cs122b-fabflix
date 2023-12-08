package Parse;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/*
This is the light version of the Cast Parser used on AWS.
AWS can only handle this parser.
 */

public class CastParser2 extends DefaultHandler {
    private BufferedWriter errorWriter;
    private String tempVal;
    private StarInMovie tempPair;
    private ArrayList<StarInMovie> simArray;

    Connection connection;

    HashMap<String, String> actorNameIdMap;
    HashMap<String, MovieObject> movieIdMap;
    // HashMap<String, HashSet<String>> movieIdGroups;

    public CastParser2(HashMap<String, MovieObject> movieIdMap) {
        tempPair = null;
        simArray = new ArrayList<>();
        connection = null;
        this.actorNameIdMap = new HashMap<>();
        this.movieIdMap = movieIdMap;
        // this.movieIdGroups = movieIdGroups;
        startConnection();
        try {
            errorWriter = new BufferedWriter(new FileWriter("castLogs.txt"));
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    private void startConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://172.31.0.193:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);
            connection.setAutoCommit(false);
        } catch (Exception e) {
            connection = null;
        }
    }

    private void closeConnection() {
        try {
            if (connection != null)
                connection.close();
        } catch (Exception ignore) {}
    }

    private void populateStarNameIdMap() throws SQLException {
        if (connection != null) {
            String query = "SELECT id, name FROM stars";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                actorNameIdMap.put(resultSet.getString("name"), resultSet.getString("id"));
            }
            statement.close();
            resultSet.close();
        }
    }

    public void runParser() {
        try {
            populateStarNameIdMap();
            parseDocument();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void insertBatch() {
        String query = "INSERT IGNORE INTO stars_in_movies(starId, movieId) VALUE(?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (StarInMovie sim: simArray) {
                statement.setString(1, sim.getStarId());
                statement.setString(2, sim.getMovieId());
                statement.addBatch();
            }
            statement.executeLargeBatch();
            connection.commit();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void parseDocument() throws SQLException {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("casts124.xml", this);

            insertBatch();
            closeConnection();
            errorWriter.close();

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }


    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            tempPair = new StarInMovie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("m")) {
            if (tempPair.hasNull()) {
                try {
                    errorWriter.write("Cast is missing value: " + tempPair.toString());
                    errorWriter.newLine();
                } catch (Exception E) {
                    E.printStackTrace();
                }
                System.out.println("Cast is missing value: " + tempPair.toString());
                return;
            }
            if (movieIdMap.containsKey(tempPair.getMovieId())) {
                String movieTitle = movieIdMap.get(tempPair.getMovieId()).getTitle();
                if (movieTitle.equalsIgnoreCase(tempPair.getMovieName())) {
                    simArray.add(tempPair);
                }
            } else {
                try {
                    errorWriter.write("Unrecognized Movie Id: " + tempPair.getMovieId());
                    errorWriter.newLine();
                } catch (Exception E) {
                    E.printStackTrace();
                }
                System.out.println("Unrecognized Movie Id: " + tempPair.getMovieId());
            }

            if (simArray.size() >= 1500) {
                System.out.println("-- inserting stars in movie batch (size: " + simArray.size() + ") --");
                insertBatch();
                simArray.clear();
            }
        } else if (qName.equalsIgnoreCase("t")) {
            tempPair.setMovieName(tempVal);
        } else if (qName.equalsIgnoreCase("f")) {
            tempPair.setMovieId(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
            String starName = tempVal;
            tempPair.setStarName(starName);

            if (actorNameIdMap.containsKey(tempPair.getStarName())) {
                tempPair.setStarId(actorNameIdMap.get(starName));
            } else {
                try {
                    errorWriter.write("Unrecognized star: " + starName);
                    errorWriter.newLine();
                } catch (Exception E) {
                    E.printStackTrace();
                }
                System.out.println("Unrecognized star: " + starName);
            }
        }
    }

}
