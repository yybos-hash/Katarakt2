package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;

public class User extends PacketObject {
    public User () {
        super.setType(PacketObject.Type.User.getValue());
    }

    public String getUsername () {
        return this.e;
    }
    public String getEmail () {
        return this.f;
    }
    public String getPassword () {
        return this.g;
    }

    public void setUsername(String username) {
        this.e = username;
    }
    public void setEmail (String email) {
        this.f = email;
    }
    public void setPassword (String password) {
        this.g = password;
    }

    public static User toUser (int id, String username, String email, String password) {
        User from = new User();
        from.id = id;

        from.e = username;
        from.f = email;
        from.g = password;

        return from;
    }
    public static User fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, User.class);
    }
}
