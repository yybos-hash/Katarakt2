package yybos.katarakt.Objects.Media;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yybos.katarakt.Objects.ObjectDateDeserializer;
import yybos.katarakt.Objects.PacketObject;
import yybos.katarakt.Objects.Message.User;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;

public class MediaFile extends PacketObject {
    private String path;
    private String filename;
    private long size;
    private MediaFileType mediaFileType;

    private User user = new User();

    public MediaFile () {
        super.setType(PacketObject.Type.File.getValue());
    }

    public String getPath () {
        return this.path;
    }
    public String getFilename () {
        return this.filename.split("/")[this.filename.split("/").length - 1];
    }
    public String getFileExtension () {
        return this.getFilename().split("\\.")[this.getFilename().split("\\.").length - 1];
    }
    public MediaFileType getFileType () {
        return this.mediaFileType;
    }

    public File toFile () {
        return new File(this.path);
    }
    public static MediaFile toMediaFile (String path, MediaFileType mediaFileType) {
        MediaFile from = new MediaFile();
        from.type = Type.File;
        from.mediaFileType = mediaFileType;
        from.size = new File(path).length();
        from.path = path.replace("\\", "/");
        from.filename = from.path.split("/")[from.path.split("/").length - 1];
        from.date = System.currentTimeMillis();

        return from;
    }
    public static MediaFile fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, MediaFile.class);
    }

    public long getSize() {
        return this.size;
    }
    public void setSize(long length) {
        this.size = length;
    }

    public User getUser () {
        return this.user;
    }
    public void setUser (User user) {
        this.user = user;
    }

    public enum MediaFileType {
        UPLOAD,
        DOWNLOAD,
        PREVIEW
    }
}
