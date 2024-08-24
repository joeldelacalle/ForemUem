package com.forem.main;

import java.security.spec.InvalidKeySpecException;

import java.util.List;
import java.util.Map;


import com.forem.manager.GmailOAuthManager;
import com.forem.verifier.EmailVerifier1;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.bouncycastle.util.encoders.Base64;

public class ForemMain {
	public static void main(String[] args) throws InvalidKeySpecException {
		/**
		 * Se crea la conexión con el servidor de correo, seleccionamos la carpeta de la que queremos analizar los correos
		 * verificamos las cabecerasa de los distintos correos electrónicos de la carpeta
		 */
		try {
            Gmail service = GmailOAuthManager.getGmailService();

            String user = "me";
            ListMessagesResponse listResponse = service.users().messages().list(user).execute();
            List<Message> messages = listResponse.getMessages();

            if (messages == null || messages.isEmpty()) {
                System.out.println("No messages found.");
            } else {
                System.out.println("Messages:");
                for (Message message : messages) {
                    Message fullMessage = service.users().messages().get(user, message.getId()).execute();
                    System.out.println("Message ID: " + message.getId());
                    System.out.println("Snippet: " + fullMessage.getSnippet());
                }
            }
            
         // Verificar cada mensaje
            for (Message message : messages) {
            	 Message fullMessage = service.users().messages().get(user, message.getId()).execute();
                try {
                	// Verificar cabeceras del mensaje
               	 	EmailVerifier1.verifyEmailHeaders(fullMessage);
                    // Si no se lanza una excepción, la verificación fue exitosa
                    System.out.println("Mensaje verificado correctamente.");
                } catch (Exception e) {
                    System.err.println("Error al verificar mensaje: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
