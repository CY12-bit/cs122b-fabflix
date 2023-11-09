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

    Connection connection;
    int nextId;

    public ActorParser() {
        tempActor = null;
        maxRows = 500;
        actorList = new ArrayList<>(maxRows);
        connection = null;
        startConnection();
        nextId = 0;
    }

    private void startConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:" + DBInfo.dbtype + "://localhost:3306/" + DBInfo.dbname,
                    DBInfo.username, DBInfo.password);
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

    public void runExample() {
        try {
            nextId = getHighestId() + 1;
            parseDocument();
        } catch (SQLException e) {
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
            sp.parse("actors.xml", this);

            // insert remaining
            insertBatch();
            closeConnection();

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

    private int getHighestId() throws SQLException {
        String highestIdQuery = "SELECT id FROM stars " +
                "ORDER BY SUBSTRING(id, 3) DESC " +
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
        String beginning = "INSERT INTO stars (id, name, birthYear) " +
                "SELECT * FROM ( VALUES ";
        String rows = "ROW(?, ?, ?),".repeat(actorList.size());
        rows = rows.substring(0, rows.length()-1);
        String end = ") as newStars (id, name, birthYear) " +
                "WHERE NOT EXISTS (" +
                "SELECT 1 FROM stars AS s " +
                "WHERE s.name = newStars.name AND " +
                "((s.birthYear is null and newStars.birthYear is null) or s.birthYear = newStars.birthYear))";
        return beginning + rows + end;
    }

    private void insertBatch() throws SQLException{
        if (connection != null) {
            String insertQuery = buildQuery();
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            int index = 1;
            for (Actor actor: actorList) {
                insertStatement.setString(index, actor.getId());
                insertStatement.setString(index+1, actor.getName());
                if (actor.getBirthYear() != null)
                    insertStatement.setInt(index+2, actor.getBirthYear());
                else
                    insertStatement.setString(index+2, null);

                index += 3;
            }
            insertStatement.executeUpdate();
            insertStatement.close();

        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            if (actorList.size() < maxRows) {
                tempActor.setId("nm" + nextId++);
                actorList.add(tempActor);
            } else {
                try {
                    insertBatch();
                } catch (Exception e) { System.out.println(e.getMessage()); }
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
