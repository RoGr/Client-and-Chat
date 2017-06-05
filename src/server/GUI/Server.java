package server.GUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import server.AuthService;
import server.AuthServiceException;
import server.ClientHandler;
import server.DBAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by roma on 24.05.17.
 */
public class Server implements Initializable {

    public ListView<String> clientsList;

    @FXML
    MenuItem startServer;
    @FXML
    MenuItem stopServer;

    ServerSocket server;
    Socket socket;
    public String out;//Список клиентов
    //Вектор это синхронизированный ArrayList
    public Vector<ClientHandler> clients;//Список клиентов

    public AuthService getAuthService() {
        return authService;
    }

    private AuthService authService;//База пароль,логин,ник

    public final String COMAND_HELP_TEXT = "Список служебных команд:\n" +
            "/end- отключится от сервера\n" +
            "/w nick - отослать личное сообщение клиенту\n" +
            "/changenick newnick - сменить ник\n" +
            "/help - список команд";

    //Запуск сервера
    public void start() {
        Thread thread = new Thread(() -> {
            stopServer.setVisible(true);
            clients = new Vector<>();
            authService = new DBAuthService();
            //Сервер с номером порта
            try {
                server = new ServerSocket(7777);
                authService.start();
                System.out.println("Ждем клиента");
                //Точка соединения с клиентом
                while (true) {//Проверяем подключение клиентов
                    socket = server.accept();//Переводит в режим ожидания
                    //Создать клиента и закиноть его в список
                    new ClientHandler(Server.this, socket);//Создаем нового клиента
                    System.out.println("Клиент подключился");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthServiceException e) {
                e.printStackTrace();
            } finally {
                authService.stop();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    //Осстановка серера
    public void stop() {
        try {
            startServer.setVisible(false);
            server.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        authService.stop();

    }

    //Сообщение сразу для всех клиентов
    public void broadCastMsg(String msg) {
        for (ClientHandler o : clients) {//Циклом отправляем всем клиентам сообщение
            o.sendMsg(msg);
        }
    }


    //Сообщение определенному пользователю
    public void whispMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                //Сообщение клиенту
                o.sendMsg("from " + from.getNick() + ": " + msg);
                //Уведомление о том что сообщение доставлено
                from.sendMsg("to " + nickTo + ": " + msg);
                break;
            }
        }
    }

    //Проверка существует ли уже такой ник
    public ClientHandler isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return o;
            }
        }
        return null;
    }

    //Отправить список клиентов
    public void broadcastClietnList() {
        StringBuilder sb = new StringBuilder("/clientslist ");
        for (ClientHandler o : clients) {//Циклом отправляем всем клиентам сообщение
            sb.append(o.getNick() + " ");
        }
        out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
        updateClientsList(out);
    }

    //Добавить клиента в список
    public void subscribe(ClientHandler clientHandler) {
        broadCastMsg("К чату присоеденился " + clientHandler.getNick());
        clients.add(clientHandler);
        broadcastClietnList();

    }

    //Удалить клиента из списка
    public void unsubscribe(ClientHandler clientHandler) {
        broadCastMsg("Из чата вышел " + clientHandler.getNick());
        clients.remove(clientHandler);
        broadcastClietnList();
    }

    //Получить всех клиентов
    public void getAllClients(){
        updateClientsList(authService.getAllClients());
    }

    //Обновить список клиентов
    private void updateClientsList(String out) {
        if(out!=null) {
            String clients[] = out.split("\\s");
            Platform.runLater(() -> {
                clientsList.getItems().clear();//Отчищаем ListView
                for (int i = 1; i < clients.length; i++) {//Добавляем клиентов
                    clientsList.getItems().add(clients[i]);
                }
            });
            clientsList.setVisible(true);

            clientsList.setManaged(true);
        }
        else {
            clientsList.getItems().clear();
            clientsList.getItems().add("Список клиенов пуст");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    public void exit(ActionEvent actionEvent) {

    }

    public void clientsListClick(MouseEvent mouseEvent) {
            String nickBan = clientsList.
                    getSelectionModel().//Получаем модель ListView
                    getSelectedItem();//Получаем элемент
            nickBan(nickBan);
    }

    private boolean nickBan(String nick) {
        for (ClientHandler o: clients) {
            if(o.getNick()==nick){
                o.sendMsg("/adminBan");
                return true;
            }
        }
        return false;
    }

}

