package yybos.katarakt.Objects.Media;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yybos.katarakt.Objects.Login;
import yybos.katarakt.Objects.ObjectDateDeserializer;
import yybos.katarakt.Objects.PacketObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Directory extends PacketObject {
    public String path;
    public List<DirectoryObject> objects = new ArrayList<>();

    public static Directory fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, Directory.class);
    }
}
