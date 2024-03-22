package yybos.katarakt.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;
import java.sql.Timestamp;

public class Anime extends PacketObject {
    String link;
    String name;
    String stopedAt;
    AnimeType animeType;
    long lastEntry;
    boolean isDone;
    int episode;

    public Anime () {
        super.setType(Type.Anime.getValue());
    }

    public String getLink () {
        return this.link;
    }
    public String getName () {
        return name;
    }
    public int getEpisode () {
        return episode;
    }
    public String getStopedAt () {
        return stopedAt;
    }
    public AnimeType getAnimeType () {
        return this.animeType;
    }
    public long getLastEntry () {
        return this.lastEntry;
    }
    public boolean isDone () {
        return this.isDone;
    }

    public void setLink (String link) {
        this.link = link;
    }
    public void setName (String name) {
        this.name = name;
    }
    public void setStopedAt (String stopedAt) {
        this.stopedAt = stopedAt;
    }
    public void setEpisode (int episode) {
        this.episode = episode;
    }
    public void setAnimeType (AnimeType animeType) {
        this.animeType = animeType;
    }
    public void setLastEntry (long lastEntry) {
        this.lastEntry = lastEntry;
    }
    public void setIsDone (boolean isDone) {
        this.isDone = isDone;
    }

    public enum AnimeType {
        Anime,
        Hentai
    }

    public static Anime fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, Anime.class);
    }
}
