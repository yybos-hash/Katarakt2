package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;

public class Command extends PacketObject {
    public String getCommand () {
        return this.e;
    }
    public int getA () {
        return this.a;
    }
    public int getB () {
        return this.b;
    }
    public int getC () {
        return this.c;
    }
    public int getD () {
        return this.d;
    }
    public String getE () {
        return this.e;
    }
    public String getF () {
        return this.f;
    }
    public String getG () {
        return this.g;
    }
    public String getH () {
        return this.h;
    }

    public static Command getChatHistory (int chatId) {
        Command from = new Command();
        from.type = Type.Command;
        from.e = "getChatHistory";
        from.a = chatId;

        return from;
    }
    public static Command fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

//        System.out.println(json);
        return (Command) parser.fromJson(json, PacketObject.class);
    }
}
