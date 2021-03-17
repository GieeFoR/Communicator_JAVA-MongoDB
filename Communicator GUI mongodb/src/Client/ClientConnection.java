package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.*;

public class ClientConnection extends Thread {
    private static ClientConnection instance = null;

    private ObjectOutputStream objectOutputSendStream = null;
    private ObjectInputStream objectInputSendStream = null;

    private ObjectOutputStream objectOutputReceiveStream = null;
    private ObjectInputStream objectInputReceiveStream = null;

    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;

    private ClientConnection(){
        ScheduledExecutorService threadPoolExecutor = Executors.newScheduledThreadPool(2);
        executorService = threadPoolExecutor;
        scheduledExecutorService = threadPoolExecutor;
    }

    public void connect(String address, int port) throws IOException {
        InetAddress ip = InetAddress.getByName(address);
        Socket dataSendSocket = new Socket(ip, port);
        Socket dataReceiveSocket = new Socket(ip, port);

        objectOutputSendStream = new ObjectOutputStream(dataSendSocket.getOutputStream());
        objectInputSendStream = new ObjectInputStream(dataSendSocket.getInputStream());

        objectOutputReceiveStream = new ObjectOutputStream(dataReceiveSocket.getOutputStream());
        objectInputReceiveStream = new ObjectInputStream(dataReceiveSocket.getInputStream());
    }

    public static ClientConnection getInstance() {
        if(instance == null) {
            instance = new ClientConnection();
        }
        return instance;
    }

    public ObjectOutputStream getObjectOutputSendStream() {
        return objectOutputSendStream;
    }

    public ObjectInputStream getObjectInputSendStream() {
        return objectInputSendStream;
    }

    public ObjectOutputStream getObjectOutputReceiveStream() {
        return objectOutputReceiveStream;
    }

    public ObjectInputStream getObjectInputReceiveStream() {
        return objectInputReceiveStream;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }
}
