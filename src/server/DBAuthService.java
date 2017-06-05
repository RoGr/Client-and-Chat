package server;

import java.sql.*;

/**
 * Created by roma on 27.05.17.
 */
public class DBAuthService implements AuthService {
    private Connection connection;//Подключение к базе данных
    private Statement stmt;//Запросы к базе

    @Override
    public void start() throws AuthServiceException {//При запуске сервиса авторизации
        try {
            //Подключаемся к БД
            Class.forName("org.sqlite.JDBC");//Регестрируем драйвер
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");//Открыть соединение по ссылке на БД
            stmt = connection.createStatement();//Отсылаем запросы к БД
        }
        catch (Exception e){
             throw new AuthServiceException();
        }
    }

    @Override
    public void stop() {//При осстановки сервиса автризации
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //Проверка существования логина и пароля в нашей бд
    @Override
    public String getNickLoginAndPass(String login, String password) {
        try {
            String sql = String.format("SELECT nick FROM users WHERE login = '%s' AND password = '%s'", login, password);
            //Получить ответ с БД
            ResultSet rs = stmt.executeQuery(sql);//Передаем наш запрос
            if(rs.next()){//Переходим на след строку в таблице
                System.out.println(rs.getString(1));
                return rs.getString(1);//Получаем значение из первого столбца таблицы
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean changeNick(ClientHandler user, String newNick) {
        try {
        String sql = String.format("UPDATE users SET nick = '%s' WHERE nick = '%s'",newNick,user.getNick());//Запрос смены ника
            stmt.executeUpdate(sql);//Запрос о смене ника
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getAllClients() {
        String sql = "SELECT nick FROM users";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                stringBuilder.append(" " +rs.getString(1));
            }
            return stringBuilder.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
