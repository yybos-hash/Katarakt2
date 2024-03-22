package yybos.katarakt.Database;

import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Objects.Anime;
import yybos.katarakt.Objects.Message.Chat;
import yybos.katarakt.Objects.Message.Message;
import yybos.katarakt.Objects.Message.User;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static yybos.katarakt.Objects.PacketObject.Type.Chat;

public class DBConnection {
    private Connection connection;

    public Message pushMessage (Message message) {
        this.connect();

        try {
            String sql = "INSERT INTO messages (message, dat, fk_type, fk_chat, fk_user) VALUES (?, ?, ?, ?, ?)";

            // avoid sql injection
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, message.getMessage());
            preparedStatement.setTimestamp(2, new Timestamp(message.getDate()));
            preparedStatement.setInt(3, message.getType().ordinal());
            preparedStatement.setInt(4, message.getChat());
            preparedStatement.setInt(5, message.getUserId());

            // Execute the INSERT
            preparedStatement.executeUpdate();

            // Retrieve the generated keys (auto-incremented IDs)
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();

            int newMessageId = generatedKeys.getInt(1);
            message.setId(newMessageId);

            this.close();
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }

        this.close();
        return message;
    }
    public List<Message> getLog (int chat) {
        this.connect(); // connect to database
        List<Message> messages = new ArrayList<>();

        try {
            String sql = "SELECT messages.id, messages.fk_type, messages.message, messages.dat, messages.fk_user, users.nm FROM messages INNER JOIN users ON messages.fk_user = users.id WHERE messages.fk_chat = ? ORDER BY messages.dat LIMIT ?;";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, chat);
            // max number of messages that can be retrieved at once
            int logLimit = 50;
            preparedStatement.setInt(2, logLimit);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int type = resultSet.getInt("fk_type");
                String text = resultSet.getString("message");
                Timestamp date = resultSet.getTimestamp("dat");
                String user = resultSet.getString("nm");
                int userId = resultSet.getInt("fk_user");

                Message message = new Message();
                message.setId(id);
                message.setType(type);
                message.setMessage(text);
                message.setDate(date.getTime());
                message.setChat(chat);
                message.setUsername(user);
                message.setUserId(userId);

                messages.add(message);
            }

            this.close();
            return messages;
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());

            this.close();
            return null;
        }
    }

    public User getUser (int id) {
        this.connect();
        User user = new User();

        try {
            String sql = "SELECT nm, pass, email FROM users WHERE id = ?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            user.setId(id);
            user.setUsername(resultSet.getString("nm"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("pass"));
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return user;
    }
    public User getUser (String email) {
        this.connect();
        User user = new User();

        try {
            String sql = "SELECT id, pass, nm FROM users WHERE email = ?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return new User();

            user.setId(resultSet.getInt("id"));
            user.setUsername(resultSet.getString("nm"));
            user.setEmail(email);
            user.setPassword(resultSet.getString("pass"));
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return user;
    }

    public void registerUser (String email, String name, String password) {
        this.connect();

        try {
            // insert user
            String sql = "INSERT INTO users (email, nm, pass) VALUES (?, ?, ?)";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, password);

            preparedStatement.executeUpdate();

            // get the new user generated id
            sql = "SELECT id FROM users WHERE email=? AND pass=?";
            preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            int newUserId = resultSet.getInt("id");

            // create a initial chat for him
            sql = "INSERT INTO chats (fk_user, nm) VALUES (?, 'Main')";

            // prepare the sql and shit
            preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, newUserId);

            preparedStatement.executeUpdate();
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
    }

    public List<Chat> getChats (int user) {
        if (user == 0)
            return null;

        this.connect();
        List<Chat> chats = new ArrayList<>();
        Chat chat;

        try {
            String sql = "SELECT id, nm, created_at FROM chats WHERE fk_user = ?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, user);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                chat = new Chat();
                chat.setId(resultSet.getInt("id"));
                chat.setName(resultSet.getString("nm"));
                chat.setUser(user);
                chat.setDate(resultSet.getTimestamp("created_at").getTime());

                chats.add(chat);
            }
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return chats;
    }
    public Chat createChat (String name, int user) {
        this.connect();

        try {
            String sql;
            PreparedStatement preparedStatement;

            sql = "INSERT INTO chats (nm, fk_user) VALUES (?, ?)";

            // prepare the sql and shit
            preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, user);
            preparedStatement.executeUpdate();

            // Retrieve the generated keys (auto-incremented IDs)
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();

            int newChatId = generatedKeys.getInt(1);

            sql = "SELECT * FROM chats WHERE id=?";

            // prepare the sql and shit
            preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, newChatId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            Chat chat = new Chat();
            chat.setId(resultSet.getInt("id"));
            chat.setName(name);
            chat.setUser(user);
            chat.setDate(resultSet.getTimestamp("created_at").getTime());

            return chat;
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();

        return null;
    }

    public void updateUsername (int id, String newUsername) {
        this.connect();

        try {
            // insert user
            String sql = "UPDATE users SET nm=? WHERE id=?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, newUsername);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
    }
    public void deleteChat(int id) {
        this.connect();

        try {
            // insert user
            String sql = "DELETE FROM chats WHERE id=?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
    }

    // otaku

    public List<Anime> getAnimeList () {
        List<Anime> animes = new ArrayList<>();

        this.connect();
        Anime anime;

        try {
            String sql;

            sql = "SELECT\n" +
                    "    animes.id AS anime_id,\n" +
                    "    animes.anime_name AS anime_name,\n" +
                    "    animes.anime_type AS anime_type,\n" +
                    "    animes.isdone AS anime_isdone,\n" +
                    "    anime_entry.id AS entry_id,\n" +
                    "    anime_entry.link AS link,\n" +
                    "    anime_entry.dt AS last_entry_timestamp,\n" +
                    "    anime_entry.episode AS entry_episode,\n" +
                    "    anime_entry.stoped_at AS entry_stoped_at\n" +
                    "FROM\n" +
                    "    animes\n" +
                    "JOIN\n" +
                    "    anime_entry ON animes.id = anime_entry.anime_id\n" +
                    "WHERE\n" +
                    "    (anime_entry.anime_id, anime_entry.dt, anime_entry.id) = \n" +
                    "    (SELECT anime_id, MAX(dt), MAX(id)\n" +
                    "     FROM anime_entry\n" +
                    "     WHERE anime_id = animes.id\n" +
                    "     GROUP BY anime_id);\n";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                anime = new Anime();
                anime.setId(resultSet.getInt("anime_id"));
                anime.setLink(resultSet.getString("link"));
                anime.setName(resultSet.getString("anime_name"));
                anime.setLastEntry(resultSet.getTimestamp("last_entry_timestamp").getTime());
                anime.setEpisode(resultSet.getInt("entry_episode"));
                anime.setStopedAt(resultSet.getString("entry_stoped_at"));
                anime.setIsDone(resultSet.getBoolean("anime_isdone"));

                String typeStr = resultSet.getString("anime_type");
                if (typeStr.equalsIgnoreCase("anime"))
                    anime.setAnimeType(Anime.AnimeType.Anime);
                else
                    anime.setAnimeType(Anime.AnimeType.Hentai);

                animes.add(anime);
            }
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return animes;
    }

    private void connect () {
        try {
            // jdbc:mysql://141.144.226.199:52000/blackness-db Kitsune
            String jdbcUrl = "jdbc:mysql://localhost:3306/katarakt2";
            // blackness kitsune
            String username = "root";
            // 062GibRGp+2a kitsune
            String password = "";
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }
    }
    private void close () {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());
        }
    }
}
