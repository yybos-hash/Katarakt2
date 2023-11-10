package yybos.katarakt.Objects;

public class User {
    private int id;
    private String name;
    private String pass;

    public int getId () {
        return id;
    }
    public String getName () {
        return name;
    }
    public String getPass () {
        return this.pass;
    }

    public void setId (int id) {
        this.id = id;
    }
    public void setName (String name) {
        this.name = name;
    }
    public void setPass (String pass) {
        this.pass = pass;
    }
}
