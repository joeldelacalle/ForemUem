package com.forem.manager;

import javax.mail.*;
import java.util.Properties;

public class EmailConexionManager {
	private String username;
    private String password;
    private Properties properties;

    public EmailConexionManager(String username, String password) {
        this.username = username;
        this.password = password;
        this.properties = new Properties();
        configureProperties();
    }

    private void configureProperties() {
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com"); // Cambia esto según tu proveedor de correo
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");
        properties.put("mail.imaps.auth", "true");
    }

    public Session createSession() {
        return Session.getDefaultInstance(properties);
    }

    public Store connectToStore(Session session) throws MessagingException {
        Store store = session.getStore("imaps");
        store.connect(username, password);
        return store;
    }
}
