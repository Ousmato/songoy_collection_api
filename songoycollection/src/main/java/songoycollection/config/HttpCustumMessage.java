package songoycollection.config;

import lombok.Data;

@Data
public class HttpCustumMessage {

    private  String message;
    public  static  HttpCustumMessage addMessage() {
        HttpCustumMessage res = new HttpCustumMessage();
        res.setMessage("Ajout effectuée avec succés ");
        return res;
    }

    public static HttpCustumMessage updateMessage() {
        HttpCustumMessage res = new HttpCustumMessage();
        res.setMessage("Mise à jour effectuée avec succés");
        return res;
    }

    public static HttpCustumMessage deleteMessage() {
        HttpCustumMessage res = new HttpCustumMessage();
        res.setMessage("Suppression effectuee avec succes");
        return res;
    }

}
