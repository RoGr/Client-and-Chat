package server;

/**
 * Created by roma on 27.05.17.
 */
public class AuthServiceException extends Exception {//Ошибка при БД

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        System.out.println("Ошибка БД");
    }
}
