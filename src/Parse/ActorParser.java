package Parse;


import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class ActorParser extends DefaultHandler {
    private String tempVal;

    //to maintain context
    private Actor tempActor;

    int maxRows;
    List<Actor> actorList;

    public ActorParser() {
        tempActor = null;
        maxRows = 500;
        actorList = new ArrayList<>(maxRows);
    }

    public void runExample() {
        parseDocument();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("actors63.xml", this);

            // insert remaining
            insertBatch();

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
        if (qName.equalsIgnoreCase("actor")) {
            tempActor = new Actor();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    private int getHighestId(Connection connection) throws SQLException {
        String highestIdQuery = "SELECT id FROM stars " +
                "ORDER BY SUBSTRING(id, 3) DESC " + // Colin: Doesn't this sort the strings by highest to lowest?
                "LIMIT 1";
        PreparedStatement highestIdStatement = connection.prepareStatement(highestIdQuery);
        ResultSet highestId = highestIdStatement.executeQuery();
        String idStr = "";
        while (highestId.next()) {
            idStr = highestId.getString("id");
        }
        // Colin: Does the prepared statement need to be closed?
        return Integer.parseInt(idStr.substring(2));
    }

    private String buildQuery() {
        String query = "INSERT INTO stars (id, name, birthYear) VALUES " + "(?, ?, ?), ".repeat(actorList.size());
        return query.substring(0, query.length()-2);
    }

    private void insertBatch() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://localhost:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);

            if (connection != null) {
                int nextId = getHighestId(connection) + 1;
                String insertQuery = buildQuery();
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                int index = 1;
                for (Actor actor: actorList) {
                    insertStatement.setString(index, "nm"+nextId);
                    insertStatement.setString(index+1, actor.getName());
                    insertStatement.setInt(index+2, actor.getBirthYear());

                    nextId++;
                    index += 3;
                }
                insertStatement.executeUpdate();

            }
        } catch (Exception e) {
            System.out.println("batch failed: " + e.getMessage());
        }
        //get the current max id

        // query = insert into stars(id, name, birthyear) values
        // for each actor in actorList
        //    maxid++;
        //    query += (maxid, actor.name, actor.birthYear),
        // remove extra comma
        // execute update
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            if (actorList.size() < maxRows) {
                actorList.add(tempActor);
            } else {
                // insert into db
                insertBatch();
                // clear arraylist
                actorList.clear();
            }
            System.out.println(tempActor.toString());
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempActor.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            tempActor.setBirthYear(tempVal);
        }

    }

    public static void main(String[] args) {
        ActorParser ap = new ActorParser();
        ap.runExample();
    }

}
