package server;

import java.util.ArrayList;

/**
 * Created by roma on 25.05.17.
 */
public class BaseAuthService implements AuthService {
    private class Entry{
        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {

            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (login != null ? !login.equals(entry.login) : entry.login != null) return false;
            if (pass != null ? !pass.equals(entry.pass) : entry.pass != null) return false;
            return nick != null ? nick.equals(entry.nick) : entry.nick == null;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    private ArrayList<Entry> list;//Хранит ники и пароли

    @Override
    public void start() throws AuthServiceException {
        for (int i=1;i<30;i++){
                list.add(new Entry("login" + i,"pass"+i,"nick"+i));
        }
    }

    @Override
    public void stop() {

    }

    public BaseAuthService(){
        //Создадим тестовые данные паролей
        list = new ArrayList<>();
    }
    @Override
    public String getNickLoginAndPass(String login, String password) {
        //Есть ли учетная запись с передаваемыми паролямич
        for(Entry o: list){
            if(o.login.equals(login)&&o.pass.equals(password)){
                return o.nick;
            }
        }
        return null;
    }

    @Override
    public boolean changeNick(ClientHandler user, String newNick) {
        return false;
    }

    @Override
    public String getAllClients() {
        return null;
    }
}
