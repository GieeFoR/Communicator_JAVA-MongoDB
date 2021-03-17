package Client;

import Common.Client;

public class ClientStatic {
    private static ClientStatic instance = null;
    private Client client = null;

    private ClientStatic() {}

    public static ClientStatic getInstance() {
        if(instance == null) {
            instance = new ClientStatic();
        }
        return instance;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return this.client;
    }

}
