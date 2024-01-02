package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;

public class Command extends PacketObject {
    public Command () {
        super.setType(PacketObject.Type.Command.getValue());
    }

    public static Command errorToast(String s) {
        Command from = new Command();
        from.e = "errorToast";
        from.f = s;

        return from;
    }
    public static Command toCommand (String command, String args) {
        Command from = new Command();
        from.type = Type.Command;
        from.e = command;
        from.f = args;

        return from;
    }

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
    public String getF () {
        return this.f;
    }
    public String getG () {
        return this.g;
    }
    public String getH () {
        return this.h;
    }

    public static Command askForUsername () {
        Command from = new Command();
        from.e = "usernameRequest";

        return from;
    }

    public static Command fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

//        System.out.println(json);
        return parser.fromJson(json, Command.class);
    }
}
