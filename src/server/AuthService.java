package server;

/**
 * Created by roma on 25.05.17.
 */
public interface AuthService {
    void start() throws  AuthServiceException;//При старте работы
    void stop();//При закрытии работы
    String getNickLoginAndPass(String login, String password);//Получить логин и пароль
    boolean changeNick(ClientHandler user, String newNick);//существует ли ник
    String getAllClients();//Получить все ники
}
