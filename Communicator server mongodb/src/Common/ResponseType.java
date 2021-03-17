package Common;

public enum ResponseType {
    LOGIN_TAKEN(),
    EMAIL_TAKEN(),
    ALREADY_LOGGED_IN(),
    WRONG_USERNAME_PASSWORD(),
    CONFIRMATION(),
    FAILURE();

    public boolean checkType(ResponseType type) {
        if(this.equals(type)) {
            return true;
        }
        return false;
    }
}
