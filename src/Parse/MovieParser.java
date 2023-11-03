package Parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MovieParser extends DefaultHandler {

    HashMap<String,MovieObject> myMovies;

    private String tempVal;
    private String tempDirector;
    private MovieObject tempMovie;
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

            sp.parse("mains243.xml",this);

            // If we have any remaining movies in the set, we add them to the database
            insertMovies();
        }
        catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new MovieObject();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    // When the function reaches 500 movies, it will insert those movies into the database
    // and then clear the movie list for the next chunk
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        }
        if (qName.equalsIgnoreCase("dirname")) {
            tempDirector = tempVal;
        }
        else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        }
        else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempVal);
        }
        else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(tempDirector);

            // Make sure we don't add duplicates
            if (!myMovies.containsKey(tempMovie.getId())) {
                myMovies.put(tempMovie.getId(),tempMovie);
                System.out.println(tempMovie.toString());

                // If we reached 500 movies!
                if (myMovies.size() == 500) {
                    insertMovies();
                    myMovies.clear();
                }
            }

        }
    }

    private void insertMovies() {
        // Check if the movie already exists in the database
            // If it's a movie that was from the XML, then ignore
            // If it's a movie that was from the original db, then don't insert it
        // Insert the movies into the database
        // Print out that it was successful
    }

    public static void main(String[] args) {
        MovieParser spe = new MovieParser();
        spe.runParser();
    }
}