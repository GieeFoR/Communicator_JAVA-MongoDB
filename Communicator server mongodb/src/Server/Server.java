package Server;

import Common.Client;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class Server {
    private static Server instance = null;
    private ServerSocket serverSocket = null;
    private Socket dataReceiveSocket = null;
    private Socket dataSendSocket = null;
    private final List<ClientConnected> clientsOnline = new LinkedList<>();
    private final List<Client> clientsLogged = new LinkedList<>();
    private final List<Client> clientsAccounts = new LinkedList<>();

    private final MongoCollection userCollection;
    private final MongoCollection conversationCollection;
    private final MongoCollection messageCollection;

    private final int BASE_NUMBER = 1000;
    private int amountOfAccounts = 0;

    public static void main(String[] args) {
        Server server = getInstance();
        server.startServer(4999);

        while(true) {
            server.listenForClients();
        }
    }

    Server() {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase database = mongoClient.getDatabase("Communicator");

        userCollection = database.getCollection("User");
        conversationCollection = database.getCollection("Conversation");
        messageCollection = database.getCollection("Message");

        amountOfAccounts = (int) userCollection.count();
    }

    public static Server getInstance() {
        if(instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public List<ClientConnected> getClientsOnline() {
        return clientsOnline;
    }

    public List<Client> getClientsAccounts() {
        return clientsAccounts;
    }

    private void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listenForClients() {
        try {
            dataReceiveSocket = serverSocket.accept();
            dataSendSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("client connected");
        ClientConnected clientConnected = new ClientConnected(dataReceiveSocket, dataSendSocket);
        clientsOnline.add(clientConnected);
        clientConnected.start();
    }

    public MongoCollection getUserCollection() {
        return userCollection;
    }

    public MongoCollection getConversationCollection() {
        return conversationCollection;
    }

    public MongoCollection getMessageCollection() {
        return messageCollection;
    }

    public int getBASE_NUMBER() {
        return BASE_NUMBER;
    }

    public int getAmountOfAccounts() {
        return amountOfAccounts;
    }

    public void incrementAmountOfAccounts() {
        this.amountOfAccounts++;
    }

    public List<Client> getClientsLogged() {
        return clientsLogged;
    }
}