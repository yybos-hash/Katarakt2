package yybos.katarakt.Objects;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ObjectDateDeserializer implements JsonDeserializer<Date> {
    private final SimpleDateFormat customDateFormat;

    public ObjectDateDeserializer() {
        // Create SimpleDateFormat with the correct format and locale
        this.customDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateStr = json.getAsString();

        try {
            java.util.Date parsedDate = this.customDateFormat.parse(dateStr);
            return new Date(parsedDate.getTime());
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing date: " + dateStr, e);
        }
    }
}