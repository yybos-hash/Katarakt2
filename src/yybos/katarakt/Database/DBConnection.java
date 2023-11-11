package yybos.katarakt.Database;

import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Objects.Chat;
import yybos.katarakt.Objects.Message;
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
                message.setUser(user);
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
            String sql = "SELECT nm, pass FROM users WHERE id = ?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            user.setId(id);
            user.setName(resultSet.getString("nm"));
            user.setPass(resultSet.getString("pass"));
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return user;
    }
    public User getUser (String username) {
        this.connect();
        User user = new User();

        try {
            String sql = "SELECT id, pass FROM users WHERE nm = ?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return new User();

            user.setId(resultSet.getInt("id"));
            user.setName(username);
            user.setPass(resultSet.getString("pass"));
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
        }

        this.close();
        return user;
    }

    public List<Chat> getChats (User user) {
        if (user.getId() == 0)
            return null;

        this.connect();
        List<Chat> chats = new ArrayList<>();
        Chat chat;

        try {
            String sql = "SELECT id, nm, dt FROM chats WHERE fk_user = ?";

            // prepare the sql and shit
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, user.getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                chat = new Chat();

                chat.setId(resultSet.getInt("id"));
                chat.setNm(resultSet.getString("nm"));
                chat.setDt(resultSet.getDate("dt"));

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
