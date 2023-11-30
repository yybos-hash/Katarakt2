package yybos.katarakt.Database;

import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Objects.Chat;
import yybos.katarakt.Objects.Message;
import yybos.katarakt.Objects.PacketObject;
import yybos.katarakt.Objects.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private final String jdbcUrl = "jdbc:mysql://localhost:3306/katarakt2"; // jdbc:mysql://141.144.226.199:52000/blackness-db Kitsune
    private final String username = "root"; // blackness kitsune
    private final String password = ""; // 062GibRGp+2a kitsune
    private final int logLimit = 50; // max number of messages that can be retrieved at once

    private Connection connection;

    public void pushMessage (Message message) {
        this.connect();

        try {
            String sql = "INSERT INTO messages (message, fk_type, fk_chat, fk_user) VALUES (?, ?, ?, ?)";

            // avoid sql injection
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, message.getMessage());
            preparedStatement.setInt(2, message.getType().ordinal());
            preparedStatement.setInt(3, message.getChat());
            preparedStatement.setInt(4, message.getUserId());

            // Execute the INSERT
            preparedStatement.executeUpdate();

            this.close();
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }

        this.close();
    }
    public List<Message> getLog (int chat) {
        this.connect(); // connect to database
        List<Message> messages = new ArrayList<>();

        try {
            String sql = "SELECT messages.id, messages.fk_type, messages.message, messages.dat, messages.fk_user, users.nm FROM messages INNER JOIN users ON messages.fk_user = users.id WHERE messages.fk_chat = ? ORDER BY messages.dat LIMIT ?;";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, chat);
            preparedStatement.setInt(2, this.logLimit);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int type = resultSet.getInt("fk_type");
                String text = resultSet.getString("message");
                Date date = resultSet.getDate("dat");
                String user = resultSet.getString("nm");
                int userId = resultSet.getInt("fk_user");

                Message message = new Message();
                message.setId(id);
                message.setType(type);
                message.setMessage(text);
                message.setDate(date);
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
            sql = "SELECT id FROM users WHERE email=?, pass=?";
            preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
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

                chat.setType(PacketObject.Type.Chat.getValue());
                chat.setId(resultSet.getInt("id"));
                chat.setName(resultSet.getString("nm"));
                chat.setUser(user);
                chat.setDate(resultSet.getDate("created_at"));

                chats.add(chat);
            }
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return chats;
    }

    private void connect () {
        try {
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
