package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> clientsList;

    public TextArea textArea;
    public TextField textField;

    public TextField passField;
    public TextField loginField;
    public Button btnLogin;
    public Button btnClientsListToggle;


    public HBox loginPanel;//Панель с авторизацией
    public HBox msgPanel;//С чатом

    private boolean isAuthorized;//Проверка на авторизацию
    private boolean isClientsListVisible;//Проверка свернутый ли лист вью
    private String myNick;


    //Меняем цвет в ListView
    static class MyListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            if (!empty) {
                if (item.length() < 5)
                    setTextFill(Paint.valueOf("Green"));
                else
                    setTextFill(Paint.valueOf("Green"));
            }
            setText(item);
            super.updateItem(item, empty);
        }
    }


    //Проверка авторизирован ли пользователь, если да,
    // то показываем панель сообщения иначе показываем авторизацию
    public void setIsAuthorized(boolean value) {
        isAuthorized = value;
        if (value) {//Авторизованы
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);

        } else {//Не авторизованы
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            myNick = "";
        }

    }


    private DataOutputStream out;//Отправка сообщений
    private DataInputStream in;//Входящее сообщение
    Socket socket;

    //Вызывается при первом запуске
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setIsAuthorized(false);//Делаем не видимой
        isClientsListVisible = false;
        try {
            socket = new Socket("localhost", 7777);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out = new DataOutputStream(socket.getOutputStream());//Отправляем сообщение
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in = new DataInputStream(socket.getInputStream());//Чтение сообщений
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(() -> {
            String msg = null;
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                msg = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                if (msg.startsWith("/timeban")) {
                    showAlert("/timeban");
                    break;
                }
                else break;
            }
        });
        thread.start();
    }

    //Подключаемся к серверу
    public void connect() {
        //Поток для того что-бы не прерывать действие UI интерфейса
        Thread inputTread = new Thread(() -> {
            try {
                String msg = null;//Сообщение c Сервера
                //Проверка на регистрацию
                while (true) {
                    msg = in.readUTF();
                    if (msg.equals("asdasdqqwsdas")) {//Если пришел код авторизации
                        setIsAuthorized(true);
                        break;
                    } else {
                        textArea.appendText(msg + "\n");//Записываем сообщение о ошибке
                    }
                }
                //Читаем сообщение
                while (true) {
                    msg = in.readUTF();
                    //Системное сообение
                    if (msg.startsWith("/")) {
                        if(msg.startsWith("/adminBan")){
                           showAlert("/adminBan");
                        }
                        if (msg.equals("/disconnect")) break;
                        //Если список клиентов
                        if (msg.startsWith("/clientslist")) {
                            String clients[] = msg.split("\\s");

                            Platform.runLater(() -> {
                                clientsList.getItems().clear();//Отчищаем ListView

                                for (int i = 1; i < clients.length; i++) {//Добавляем клиентов
                                    clientsList.getItems().add(clients[i]);
                                }
                            });
                        }
                        if (msg.startsWith("/yournick")) {
                            myNick = msg.split("\\s")[1];
                            textArea.appendText("Вы вошли под ником: " + myNick + "\n");
                            System.out.println(myNick);
                        }
                    } else textArea.appendText(msg + "\n");

                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Проблемы при обращении к серверу");

            } finally {
                setIsAuthorized(false);
                try {
                    socket.close();//Закрыть соединение
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        inputTread.setDaemon(true);//Поток демон завершает работы вместе с программой
        inputTread.start();
    }

    //Отправка данных о авторизации на сервер
    public void sendAuth() {
        try {
            if (socket == null || socket.isConnected()) {
                connect();
            }
            String str = "/auth" + " " + loginField.getText() + " " + passField.getText();
            out.writeUTF(str);//Отправка на сервер
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    //Метод отправки сообщения
    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
        } catch (IOException e) {
            showAlert("Проверьте подключение");
            e.printStackTrace();
        }
        textField.clear();
    }

    //Сообщение об ошибке
    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if(msg.startsWith("/adminBan")){
                alert.setTitle("Вы были забанены администратором");
                alert.showAndWait();
                Platform.exit();
                return;
            }
            if(msg.startsWith("/timeban")){
                alert.setTitle("Вышло время подключения, презайдите позднее");
                alert.showAndWait();
                Platform.exit();
                return;
            }
        });
    }

    //Нажатия на элемент вы лист вьию
    public void clientsListClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {//При двойном щелчке
            textField.setText("/w " + clientsList.
                    getSelectionModel().//Получаем модель ListView
                    getSelectedItem() + " ");//Получаем элемент
            textField.requestFocus();
            textField.selectEnd();
        }
    }

    //Команда помощи из меню
    public void menuSendHelpRequest() {
        try {
            out.writeUTF("/help");
        } catch (IOException e) {
            showAlert("Проверьте подключение");
            e.printStackTrace();
        }
        textField.clear();
    }

    //Выход из программы
    public void exit() {
        try {
            if (socket != null && !socket.isClosed()) {
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            showAlert("Не получается отослать сообщение");
            e.printStackTrace();
        } finally {
            Platform.exit();//Остановили поток
        }
    }

    //Свернуть лист вью
    public void clientsListToggle(ActionEvent actionEvent) {
        isClientsListVisible = !isClientsListVisible;
        refrashClientsListVisible();
    }

    //Обновить лист вью
    public void refrashClientsListVisible() {
        clientsList.setVisible(false);
        clientsList.setManaged(false);
        if (isClientsListVisible && isAuthorized) {
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        }
    }
}

