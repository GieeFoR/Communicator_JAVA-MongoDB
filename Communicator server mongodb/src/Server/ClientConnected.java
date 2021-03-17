package Server;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Common.*;
import org.bson.Document;
import org.bson.types.ObjectId;

@SuppressWarnings("unchecked")
public class ClientConnected extends Thread {
    private final Server server;

    ExecutorService executorService;

    private final Socket dataReceiveSocket;
    private ObjectOutputStream objectOutputReceiveStream = null;
    private ObjectInputStream objectInputReceiveStream = null;

    private final Socket dataSendSocket;
    private ObjectOutputStream objectOutputSendStream = null;

    private Client client = null;

    ClientConnected(Socket socketReceive, Socket socketSend) {
        server = Server.getInstance();
        this.dataReceiveSocket = socketReceive;
        this.dataSendSocket = socketSend;
        executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public void run() {
        OutputStream outputReceiveStream;
        InputStream inputReceiveStream;
        OutputStream outputSendStream;

        InformationType informationType;

        try {
            outputReceiveStream = dataReceiveSocket.getOutputStream();
            inputReceiveStream = dataReceiveSocket.getInputStream();
            objectOutputReceiveStream = new ObjectOutputStream(outputReceiveStream);
            objectInputReceiveStream = new ObjectInputStream(inputReceiveStream);

            outputSendStream = dataSendSocket.getOutputStream();
            objectOutputSendStream = new ObjectOutputStream(outputSendStream);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        while(true) {
            try {
                informationType = (InformationType) objectInputReceiveStream.readObject();
                if(informationType.checkType(InformationType.LOGIN)) {
                    String username = (String) objectInputReceiveStream.readObject();
                    String password = (String) objectInputReceiveStream.readObject();

                    login(username, password);
                }
                else if(informationType.checkType(InformationType.REGISTER)) {
                    Account account = (Account) objectInputReceiveStream.readObject();
                    String name = (String) objectInputReceiveStream.readObject();
                    String surname = (String) objectInputReceiveStream.readObject();

                    signup(account, name, surname);
                }
                else if(informationType.checkType(InformationType.MESSAGE)) {
                    Message message = (Message) objectInputReceiveStream.readObject();

                    ObjectsAdaptor.insertMessage(message);

                    objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);

                    List<ObjectId> clients = null;
                    Iterator<Document> iteratorConversation = Server.getInstance().getConversationCollection().find(new Document("_id", message.getConversation())).iterator();

                    while(iteratorConversation.hasNext()) {
                        clients = (List<ObjectId>) iteratorConversation.next().get("clients");
                    }

                    List<ObjectId> finalClients = clients;
                    executorService.submit(() -> {
                        for(ObjectId cl : finalClients) {
                            for(ClientConnected cc : server.getClientsOnline()) {
                                if(cc.client == null || cc.equals(this)) {
                                    continue;
                                }
                                if(cc.client.getId().equals(cl)) {
                                    try {
                                        cc.sendMessage(message);
                                    } catch (IOException e) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            }
                        }
                    });
                }
                else if(informationType.checkType(InformationType.FIND_USER_BY_NICKNAME)) {
                    String nickname = (String) objectInputReceiveStream.readObject();

                    Client c = findNickname(nickname);
                    if(c == null) {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    }
                    else {
                        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                        objectOutputReceiveStream.writeObject(c.getUsername());
                        objectOutputReceiveStream.writeObject(c.getNumber().toString());
                        objectOutputReceiveStream.writeObject(c.getName());
                        objectOutputReceiveStream.writeObject(c.getSurname());
                    }
                }
                else if(informationType.checkType(InformationType.FIND_USER_BY_NUMBER)) {
                    String number = (String) objectInputReceiveStream.readObject();

                    Integer numberInt = Integer.parseInt(number);

                    Client c = findNumber(numberInt);
                    if(c == null) {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    }
                    else {
                        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                        objectOutputReceiveStream.writeObject(c.getUsername());
                        objectOutputReceiveStream.writeObject(c.getNumber().toString());
                        objectOutputReceiveStream.writeObject(c.getName());
                        objectOutputReceiveStream.writeObject(c.getSurname());
                    }
                }
                else if(informationType.checkType(InformationType.NEW_CONVERSATION)) {
                    List <String> usernames = (List<String>) objectInputReceiveStream.readObject();
                    String convname = (String) objectInputReceiveStream.readObject();

                    List<ObjectId> clientsIdList = new LinkedList<>();

                    clientsIdList.add(client.getId());

                    if(client == null) {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                        continue;
                    }

                    for(String username : usernames) {
                        Client c = findNickname(username);

                        clientsIdList.add(c.getId());
                    }

                    Conversation conversation = new Conversation(new ObjectId(), convname, "brak", client.getId(), clientsIdList);

                    ObjectsAdaptor.insertConversation(conversation, clientsIdList);
                    client.addConversation(conversation);

                    //send new conversation to all online clients
                    executorService.submit(() -> {
                        for(ObjectId cl : conversation.getClients()) {
                            for(ClientConnected cc : server.getClientsOnline()) {
                                if(cc.client == null || cc.equals(this)) {
                                    continue;
                                }

                                if(cc.client.getId().equals(cl)) {
                                    try {
                                        cc.objectOutputSendStream.writeObject(InformationType.NEW_CONVERSATION);
                                        cc.objectOutputSendStream.writeObject(conversation);
                                    } catch (IOException e) {
                                        System.out.println(e.getMessage());
                                    }
                                    break;
                                }
                            }
                        }
                    });

                    objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
                    objectOutputReceiveStream.writeObject(conversation);
                }
                else if(informationType.checkType(InformationType.DB_GET_CLIENTS)) {
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClients());
                }
                else if(informationType.checkType(InformationType.DB_GET_CONVS)) {
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getConversations());
                }
                else if(informationType.checkType(InformationType.DB_GET_MESS)) {
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getMessages());
                }
                else if(informationType.checkType(InformationType.DB_GET_CLIENT)) {
                    Integer number = (Integer) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClient(number));
                }
                else if(informationType.checkType(InformationType.DB_GET_CLIENT_BY_NAME)) {
                    String name = (String) objectInputReceiveStream.readObject();
                    String surname = (String) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClient_ByName(name, surname));
                }
                else if(informationType.checkType(InformationType.DB_GET_CONV_CLIENT)) {
                    Integer number = (Integer) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClientConversations(number));
                }
                else if(informationType.checkType(InformationType.DB_GET_MESS_CONV)) {
                    ObjectId id = (ObjectId)  objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getConversationMessages(id));
                }
                else if(informationType.checkType(InformationType.DB_GET_CONV_CLIENT_FOUNDED)) {
                    Integer number = (Integer) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getConversationsFoundedByUser_withNumber(number));
                }
                else if(informationType.checkType(InformationType.DB_GET_MESS_BY_CLIENT_WITH_REGEX)) {
                    Integer number = (Integer) objectInputReceiveStream.readObject();
                    String regex = (String) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClientMessages_withRegex(number, regex));
                }
                else if(informationType.checkType(InformationType.DB_GET_CLIENTS_CONV)) {
                    ObjectId id = (ObjectId) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClientsConversation(id));
                }
                else if(informationType.checkType(InformationType.DB_GET_CLIENT_LAST)) {
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getLastClient());
                }
                else if(informationType.checkType(InformationType.DB_GET_CLIENT_BY_DATE)) {
                    Date date = (Date) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClientByCreationDate(date));
                }
                else if(informationType.checkType(InformationType.DB_GET_MESS_BY_CLIENT_AND_DATE)) {
                    Integer number = (Integer) objectInputReceiveStream.readObject();
                    Date date = (Date) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClientMessagesSendOn(number, date));
                }
                else if(informationType.checkType(InformationType.DB_GET_MESS_LAST)) {
                    Integer number = (Integer) objectInputReceiveStream.readObject();
                    objectOutputReceiveStream.writeObject(ObjectsAdaptor.getClientLastMessage(number));
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
                    try {
                        objectOutputReceiveStream.writeObject(ResponseType.FAILURE);
                    } catch (IOException ioException) {
                        System.out.println(ioException.getMessage());
                    }
                if(client != null) {
                    server.getClientsLogged().remove(client);
                    client = null;
                }
                server.getClientsOnline().remove(this);
                return;
            }
        }
    }

    private void sendMessage(Message message) throws IOException {
        objectOutputSendStream.writeObject(InformationType.MESSAGE);
        objectOutputSendStream.writeObject(message);
    }

    private void login(String username, String password) throws IOException {
        client = ObjectsAdaptor.getClient(username, password);

        if(client == null) {
            objectOutputReceiveStream.writeObject(ResponseType.WRONG_USERNAME_PASSWORD);
        }
        else {
            client.getConversations().addAll(ObjectsAdaptor.getClientConversations(client.getId()));

            for (Client c : server.getClientsLogged()) {
                if (c.getId().equals(this.client.getId())) {
                    objectOutputReceiveStream.writeObject(ResponseType.ALREADY_LOGGED_IN);
                    client = null;
                    return;
                }
            }
            objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
            objectOutputReceiveStream.writeObject(client);
            Server.getInstance().getClientsLogged().add(client);
        }
    }


    private void signup(Account newAccount, String name, String surname) throws IOException {
        if(ObjectsAdaptor.checkUsernameIsTaken(newAccount.getUsername())) {
            objectOutputReceiveStream.writeObject(ResponseType.LOGIN_TAKEN);
        }

        if(ObjectsAdaptor.checkEmailIsTaken(newAccount.geteMail())) {
            objectOutputReceiveStream.writeObject(ResponseType.EMAIL_TAKEN);
        }

        int number = Server.getInstance().getBASE_NUMBER()+Server.getInstance().getAmountOfAccounts();
        Server.getInstance().incrementAmountOfAccounts();
        Client clientToInsert = new Client(new ObjectId(), number, newAccount.getUsername(), newAccount.getPassword(), name, surname, newAccount.getCreationDate(), newAccount.geteMail());

        ObjectsAdaptor.insertClient(clientToInsert);
        objectOutputReceiveStream.writeObject(ResponseType.CONFIRMATION);
    }

    private Client findNickname(String nickname) {
        return ObjectsAdaptor.getClient(nickname);
    }

    private Client findNumber(Integer number) {
        return ObjectsAdaptor.getClient(number);
    }
}