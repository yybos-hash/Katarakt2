package yybos.katarakt.Database;

import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Objects.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private final String jdbcUrl = "jdbc:mysql://localhost:3306/katarakt2"; // jdbc:mysql://141.144.226.199:52000/blackness-db Kitsune
    private final String username = "root"; // blackness kitsune
    private final String password = ""; // 062GibRGp+2a kitsune
    private final int logLimit = 50;


    private Connection connection;

    public int getInt (String query) {
        this.connect();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                // Process each row of data
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                // ...process other columns
            }

            resultSet.close();
            statement.close();

            this.close();

            return 1;
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");

            return -1;
        }
    }

    public void pushMessage (Message message) {
        this.connect();

        try {
            String sql = "INSERT INTO messages (message, fk_type, fk_chat, fk_user) VALUES (?, ?, ?, ?)";

            // avoid sql injection
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, message.getMessage());
            preparedStatement.setInt(2, message.getType().ordinal());
            preparedStatement.setInt(3, message.getChat());
            preparedStatement.setInt(4, message.getUser());

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
            String sql = "SELECT * FROM messages WHERE fk_chat=? ORDER BY dat DESC LIMIT ?;";

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
//                int chat = resultSet.getInt("fk_chat");
                int user = resultSet.getInt("fk_user");

                Message message = new Message();
                message.setId(id);
                message.setType(type);
                message.setMessage(text);
                message.setDate(date);
                message.setUser(user);

                messages.add(message);
            }

            this.close();
            return messages;
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");

            this.close();
            return null;
        }
    }

    private void connect () {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
    private void close () {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
}
