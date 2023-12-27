package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;

public class Chat extends PacketObject {
    public Chat () {
        super.setType(PacketObject.Type.Chat.getValue());
    }

    public void setName (String name) {
        this.e = name;
    }
    public void setUser (int id) {
        this.a = id;
    }

    public static Chat toChat (int id, int user, String name) {
        Chat chat = new Chat();
        chat.id = id;
        chat.a = user;
        chat.e = name;

        return chat;
    }
    public static Chat toChat (int id, int user, Date date, String name) {
        Chat chat = new Chat();
        chat.id = id;
        chat.a = user;
        chat.e = name;
        chat.date = date;

        return chat;
    }
    public static Chat fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, Chat.class);
    }
}


