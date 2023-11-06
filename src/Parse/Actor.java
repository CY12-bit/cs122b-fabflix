package Parse;

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
        name = name.replaceAll("~", " ");
        this.name = name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYearStr) {
        Integer birthYear = null;
        try {
            birthYear = Integer.parseInt(birthYearStr);
        } catch (Exception ignored) {}
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
