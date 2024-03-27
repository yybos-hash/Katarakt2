package yybos.katarakt.Objects.Media;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yybos.katarakt.Objects.ObjectDateDeserializer;
import yybos.katarakt.Objects.PacketObject;
import yybos.katarakt.Objects.Message.User;

import java.io.File;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.Timestamp;

public class MediaFile extends PacketObject {
    private String path;
    private String filename;
    private long size;
    private MediaProcessType mediaProcessType;
    private String mime;

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
    public MediaProcessType getProcessType () {
        return this.mediaProcessType;
    }

    public File toFile () {
        return new File(this.path);
    }
    public static MediaFile toMediaFile (Type type, String path, long size, MediaProcessType mediaProcessType) {
        MediaFile from = new MediaFile();
        from.type = type.getValue();
        from.mediaProcessType = mediaProcessType;
        from.size = size;
        from.path = path.replace("\\", "/");
        from.filename = from.path.split("/")[from.path.split("/").length - 1];
        from.date = System.currentTimeMillis();

        return from;
    }
    public static MediaFile toMediaFile (String path, MediaProcessType mediaProcessType) {
        MediaFile from = new MediaFile();
        from.type = Type.File.getValue();
        from.mediaProcessType = mediaProcessType;
        // from.size = new File(path).length();
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

    public User getUser() {
        return this.user;
    }
    public void setUser (User user) {
        this.user = user;
    }

    public String getMimeType () {
        return URLConnection.guessContentTypeFromName(this.filename);
    }

    public void setProcessType (MediaProcessType type) {
        this.mediaProcessType = type;
    }

    public enum MediaProcessType {
        UPLOAD,
        DOWNLOAD,
        PREVIEW
    }
}
