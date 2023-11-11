package yybos.katarakt.Objects;

import java.sql.Date;

public class Chat {
    private int id;
    private int user;
    private Date dt;
    private String nm;

    public int getId () {
        return this.id;
    }
    public int getUser () {
        return this.user;
    }
    public Date getDate () {
        return this.dt;
    }
    public String getNm () {
        return this.nm;
    }

    public void setId (int id) {
        this.id = id;
    }
    public void setUser (int id) {
        this.user = id;
    }
    public void setDt (Date date) {
        this.dt = date;
    }
    public void setNm (String name) {
        this.nm = name;
    }
}
