package Parse;

import java.util.ArrayList;
import java.util.Arrays;

public class Actor {
    String name;
    Integer birthYear;
    String id;
    public Actor() {
        this.name = "";
        this.birthYear = null;
        this.id = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = name.strip();
        name = name.replaceAll("~", " ");
        name = name.replaceAll("[\\\\][\\W]","");

        // Capitalization Process
        String[] split_words = name.split("[ ]+");
        ArrayList<String> capitalized_words = new ArrayList<String>();
        for (String c : split_words) {
            c = c.substring(0, 1).toUpperCase() + c.substring(1);
            capitalized_words.add(c);
        }
        name = String.join(" ",capitalized_words);

        this.name = name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYearStr) {
        Integer birthYear = null;
        birthYearStr = birthYearStr.strip().replaceAll("\\+","");
        // birthYearStr = birthYearStr.replaceAll("\\D","0");
        // while (birthYearStr.length() < 4) {
        //    birthYearStr += "0";
        // }
        try {
            birthYear = Integer.parseInt(birthYearStr);
        } catch (Exception ignored) {
            if (!birthYearStr.isEmpty()) {
                System.out.println("- Unable to Parse birthYear: " + birthYearStr);
            }
        }
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Actor{" +
                "name='" + name + '\'' +
                ", birthYear=" + birthYear +
                ", id='" + id + '\'' +
                '}';
    }
}
