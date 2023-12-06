package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.sql.Date;

public class MediaFile extends PacketObject {
    public String getPath () {
        return this.e;
    }
    public String getFilename () {
        return this.e.split("/")[this.e.split("/").length - 1];
    }
    public String getFileExtension () {
        return this.getFilename().split("\\.")[this.getFilename().split("\\.").length - 1];
    }

    public static File toFile (MediaFile mediaFile) {
        return new File(mediaFile.getPath());
    }
    public static MediaFile toMediaFile (Type type, String path) {
        MediaFile from = new MediaFile();
        from.type = type;
        from.e = path.replace("\\", "/");
        from.date = new Date(System.currentTimeMillis());

        return from;
    }
    public static MediaFile fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, MediaFile.class);
    }
}
