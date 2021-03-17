package Common;

import java.io.Serializable;
import java.util.Date;

public class Account implements Serializable {
    private static final long serialVersionUID = 1234567L;

    private final String username;
    private final String password;
    private final String eMail;
    private final Date creationDate;

    public Account(String login, String password, String eMail, Date creationDate) {
        this.username = login;
        this.password = password;
        this.eMail = eMail;
        this.creationDate = creationDate;
    }
    public String geteMail() {
        return eMail;
    }

    public String getPassword() { return password; }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getUsername() {
        return username;
    }
}
