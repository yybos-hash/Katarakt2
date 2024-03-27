package yybos.katarakt.Objects.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import yybos.katarakt.Objects.ObjectDateDeserializer;
import yybos.katarakt.Objects.PacketObject;

import java.sql.Date;

public class Command extends PacketObject {
    private String command;
    private JsonObject args = new JsonObject();

    public Command () {
        super.setType(PacketObject.Type.Command.getValue());
    }

    public static Command errorToast(String s) {
        Command from = new Command();
        from.command = "errorToast";
        from.args.addProperty("message", s);

        return from;
    }
    public static Command output (String output) {
        Command from = new Command();
        from.command = "output";
        from.args.addProperty("output", output);

        return from;
    }

    public static Command toCommand (String command, JsonObject args) {
        Command from = new Command();
        from.type = Type.Command.getValue();
        from.command = command;
        from.args = args;

        return from;
    }

    public String getCommand () {
        return this.command;
    }
    public JsonObject getArgs () {
        return this.args;
    }

    public static Command askForUsername () {
        Command from = new Command();
        from.command = "usernameRequest";

        return from;
    }

    public static Command fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, Command.class);
    }
}
