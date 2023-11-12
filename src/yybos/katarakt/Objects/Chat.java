package yybos.katarakt.Objects;

import java.sql.Date;

public class Chat {
    private int id;
    private int user;
    private String nm;

    public int getId () {
        return this.id;
    }
    public int getUser () {
        return this.user;
    }
    public String getName() {
        return this.nm;
    }

    public void setId (int id) {
        this.id = id;
    }
    public void setUser (int id) {
        this.user = id;
    }
    public void setName(String name) {
        this.nm = name;
    }

    public static Chat toChat (int id, int user, String name) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUser(user);
        chat.setName(name);

        return chat;
    }
}
