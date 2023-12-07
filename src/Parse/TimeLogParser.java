package Parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TimeLogParser {
    // Before running, make sure the timeLog.txt is not readonly
    // sudo chmod 744 /var/lib/tomcat10/timeLog.txt
    public static void main(String[] args) {
        try {
            Scanner userInput = new Scanner(System.in);
            System.out.print("enter path to file: ");
            String filePath = userInput.nextLine();
            userInput.close();

            File file = new File(filePath);
            Scanner myReader = new Scanner(file);
            long totalServletTime = 0;
            long totalJDBCTime = 0;
            int count = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] time = data.split(";");
                long servletTime = Long.parseLong(time[0]);
                long jdbcTime = Long.parseLong(time[1]);
                totalServletTime += servletTime;
                totalJDBCTime += jdbcTime;
                count++;
            }
            System.out.println("Average Servlet Time: " + totalServletTime/count/1_000_000.0 + "ms");
            System.out.println("Average JDBC Time: " + totalJDBCTime/count/1_000_000.0 + "ms");

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

