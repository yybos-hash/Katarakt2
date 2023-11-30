package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;

public class PacketObject {
    protected Type type;
    protected int id;

    // these are like 'arguments' (Should work, right?)
    protected int a;
    protected int b;
    protected int c;
    protected int d;
    protected String e;
    protected String f;
    protected String g;
    protected String h;
    protected Date date;

    public int getId() {
        return this.id;
    }
    public Type getType () {
        return this.type;
    }
    public Date getDate () {
        return this.date;
    }

    public void setId (int id) {
        this.id = id;
    }
    public void setType (int type) {
        this.type = Type.getEnumByValue(type);
    }
    public void setDate (Date date) {
        this.date = date;
    }

    public enum Type {
        Message(0),
        Command(1),
        Version(2),
        User(3),
        Chat(4);

        private final int value;

        Type (int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }
        public static Type getEnumByValue (int value) {
            for (Type enumValue : Type.values()) {
                if (enumValue.getValue() == value) {
                    return enumValue;
                }
            }
            throw new IllegalArgumentException("No type with value " + value);
        }
    }

    public static PacketObject fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.create();

        return parser.fromJson(json, PacketObject.class);
    }
}
