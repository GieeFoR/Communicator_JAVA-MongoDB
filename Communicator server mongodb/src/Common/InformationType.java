package Common;

public enum InformationType {
    LOGIN(),
    REGISTER(),
    MESSAGE(),
    FIND_USER_BY_NUMBER(),
    FIND_USER_BY_NICKNAME(),
    NEW_CONVERSATION(),
    DB_GET_CLIENTS(),                   //wybieranie istniejących użytkowników w bazie danych
    DB_GET_CONVS(),                     //wybieranie istniejących konwersacji w bazie danych
    DB_GET_MESS(),                      //wybieranie istniejących wiadomości w bazie danych
    DB_GET_CLIENT(),                    //wybieranie ID użytkownika, który ma nadany okreslony numer
    DB_GET_CLIENT_BY_NAME(),            //wybieranie użytkowników o konkretnym imieniu i nazwisku
    DB_GET_CONV_CLIENT(),               //wybieranie konwersacji do których należy użytkownik o danym numerze
    DB_GET_MESS_CONV(),                 //wybieranie wiadomości w danej konwersacji
    DB_GET_CONV_CLIENT_FOUNDED(),       //wybieranie konwersacji założonych przez użytkownika o danym numerze
    DB_GET_MESS_BY_CLIENT_WITH_REGEX(), //szukanie wiadomości danego użytkownika zawierających XYZ
    DB_GET_CLIENTS_CONV(),              //wybieranie członków danej konwersacji
    DB_GET_CLIENT_LAST(),               //wybieranie najnowszego użytkownika
    DB_GET_CLIENT_BY_DATE(),            //wybieranie użytkowników, którzy założyli konto danego dnia
    DB_GET_MESS_BY_CLIENT_AND_DATE(),   //wybieranie wiadomości użytkownika o danym numerze wysłanych w danym dniu
    DB_GET_MESS_LAST();                 //wybieranie najnowszej wiadomości użytkownika o danym numerze


    public boolean checkType(InformationType type) {
        if(this.equals(type)) {
            return true;
        }
        return false;
    }
}
