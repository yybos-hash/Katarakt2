package yybos.katarakt.Objects;

import java.sql.Date;
import java.sql.Timestamp;

public class PacketObject {
    protected Type type;
    protected int id;
    protected long date;

    public int getId() {
        return this.id;
    }
    public Type getType () {
        return this.type;
    }
    public long getDate () {
        return this.date;
    }

    public void setId (int id) {
        this.id = id;
    }
    public void setType (int type) {
        this.type = Type.getEnumByValue(type);
    }
    public void setDate (long date) {
        this.date = date;
    }
    public enum Type {
        Message(0),
        Command(1),
        Version(2),
        User(3),
        Login(4),
        File(5),
        Chat(6),
        Directory(7),
        Anime(8);

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
}
