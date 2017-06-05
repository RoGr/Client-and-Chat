package server;


import server.GUI.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler {
    Server server;//Сервер с клиентами
    private Socket socket;//Подключение
    private DataOutputStream out;//Отправка сообщений
    private DataInputStream in;//Входящее сообщение




    public String getNick() {
        return nick;
    }

    boolean checkReg;//Проверка на регистрацию
    String nick;


    public ClientHandler(Server server, Socket socket) {
        try {
            checkReg = false;
            this.server = server;
            this.socket = socket;
            timeBan();
            //Чтение потока сообщений
            this.in = new DataInputStream(socket.getInputStream());
            //Отправка потока сообщений
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    String msg = null;//Сообщение
                    //Читаем сообщения от незарегистрированного пользователя
                    while (true) {
                        msg = in.readUTF();//Читаем сообщение
                        //Если сообщение начинается с такого
                        if (msg.startsWith("/auth")) {// /auth login password
                            String[] data = msg.split("\\s");//Разбиваем сообщение по пробелу
                            if(data.length!=3)continue;
                            String newNick = server.getAuthService().getNickLoginAndPass(data[1], data[2]);
                            //Проверка существует ли такой пользователь
                            if (newNick != null) {
                                if (server.isNickBusy(newNick) == null) {
                                    sendMsg("asdasdqqwsdas");//Ответ о том, что пользователь авторизован
                                    sendMsg("Ваш ник: " + newNick);
                                    nick = newNick;
                                    server.subscribe(this);//Подписали клиента на рассылку
                                    checkReg = true;
                                    break;
                                } else sendMsg("Клиент с таким именем уже существует");
                            } else sendMsg("Неверный логин/пароль");
                        } else {
                            sendMsg("Необходимо авторизироваться");
                        }
                    }
                    sendMsg("Вы зарегистрированы");
                    //Если пользователь зарегестрировался читаем сообщения
                    while (true) {
                        msg = in.readUTF();
                        //Команды сервера
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                sendMsg("/disconnect");
                                break;
                            }
                            //Смена ника
                            if (msg.startsWith("/changenick ")) {
                                String oldNick = nick;
                                String newNick = msg.split("\\s")[1];

                                if (server.getAuthService().changeNick(this, newNick)) {
                                    sendMsg("/yournicks " + newNick);
                                    nick = newNick;
                                    server.broadCastMsg(oldNick + " cменил ник на " + newNick);
                                    server.broadcastClietnList();
                                } else sendMsg("Такой ник уже занят");
                            }
                            //Отправка определенному клиенту
                            if (msg.startsWith("/w ")) {
                                String[] tokens = msg.split("\\s", 3);//Рабить сообщение на три части
                                server.whispMsg(this, tokens[1], tokens[2]);
                            }
                            //Команда хелпа
                            if (msg.startsWith("/help")) {
                                sendMsg(server.COMAND_HELP_TEXT);
                            }
                        } else {
                            System.out.println("from client " + msg);
                            server.broadCastMsg(nick + ": " + msg);//Отправка всем клиентам
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Клиент вышел");
                    server.unsubscribe(this);//Отключаем клиента из спискачч
                    try {
                        socket.close();
                    } catch (IOException e) {
//                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Отправка сообщений
    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Бан по времени
    public boolean timeBan(){
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(12000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!checkReg){//Если клиент не зарегестрирован
                System.out.println("Клиент забанен по времени подключения");
                sendMsg("/timeban");
            }
            else {
                return;
            }
        });
        thread.start();
        return false;
    }
}
