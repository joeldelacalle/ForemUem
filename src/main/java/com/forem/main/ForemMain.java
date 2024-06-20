package com.forem.main;

import java.security.spec.InvalidKeySpecException;

import java.util.List;
import java.util.Map;


import com.forem.manager.GmailOAuthManager;
import com.forem.verifier.EmailVerifier;
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
//        try {
//        	Gmail service = GmailOAuthManager.getGmailService();
//
//            String user = "me";
//            ListMessagesResponse listResponse = service.users().messages().list(user).execute();
//            List<Message> messages = listResponse.getMessages();
//            EmailMensajeProcesador processor = new EmailMensajeProcesador();
//            
//            for (Message message : messages) {
//                if (message instanceof MimeMessage) {
//                    MimeMessage mimeMessage = (MimeMessage) message;
//                    
//                    // Verificar las cabeceras del mensaje
//                    processor.verifyHeaders(mimeMessage);
//
//                    // Extraer el certificado del mensaje
//                    X509Certificate certificate = processor.extractCertificateFromMessage(mimeMessage);
//                    if (certificate != null) {
//                        // Obtener la clave pública del certificado
//                        CertificadoManager certManager = new CertificadoManager();
//                        PublicKey publicKey = certManager.cargarClavePublicaCertificado(certificate);
//
//                        // Datos y firma a verificar (ejemplo)
//                        byte[] data = "datos a verificar".getBytes();
//                        byte[] signature = Base64.getDecoder().decode("firma en Base64");
//
//                        // Verificar la firma digital
//                        FirmaDigitalVerifier signatureVerifier = new FirmaDigitalVerifier();
//                        boolean isSignatureValid = signatureVerifier.verifySignature(data, signature, publicKey);
//                        System.out.println("¿Firma válida? " + isSignatureValid);
//
//                        // Validar el certificado
//                        certManager.validarCertificado(certificate);
//                        System.out.println("Certificado válido.");
//                    } else {
//                        System.out.println("No se pudo extraer el certificado del mensaje.");
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (inbox != null && inbox.isOpen()) {
//                    inbox.close(false);
//                }
//                if (store != null) {
//                    store.close();
//                }
//            } catch (MessagingException e) {
//                e.printStackTrace();
//            }
//        }
		
		
		
		
		try {
            Gmail service = GmailOAuthManager.getGmailService();

            String user = "me";
            ListMessagesResponse listResponse = service.users().messages().list(user).execute();
            List<Message> messages = listResponse.getMessages();

//            if (messages == null || messages.isEmpty()) {
//                System.out.println("No messages found.");
//            } else {
//                System.out.println("Messages:");
//                for (Message message : messages) {
//                    Message fullMessage = service.users().messages().get(user, message.getId()).execute();
//                    System.out.println("Message ID: " + message.getId());
//                    System.out.println("Snippet: " + fullMessage.getSnippet());
//                }
//            }
         // Verificar cada mensaje
            for (Message message : messages) {
            	 Message fullMessage = service.users().messages().get(user, message.getId()).execute();
            	 
            	 // Verificar cabeceras del mensaje
            	 EmailVerifier.verifyEmailHeaders(fullMessage);
            	 
                 // Verificar la firma digital (si el correo está firmado)
                 if (fullMessage.getPayload().getMimeType().equals("application/pkcs7-mime")) {
                     String encodedSignature = fullMessage.getPayload().getParts().get(0).getBody().getData();
                     byte[] signature = Base64.decode(encodedSignature);
                     System.out.println("Signature: " + signature);
                     // Implementa la verificación de la firma digital aquí
                     // Necesitarás usar una biblioteca adecuada de Java para verificar la firma digital
                     // Por ejemplo, BouncyCastle o Java Cryptography Architecture (JCA)
                 }
                try {
                    // Verificar cabeceras del mensaje
//                    EmailVerifier.verifyEmailHeaders(message);
                    
                    // Obtener y verificar firma digital del mensaje (requiere implementación adicional)
//                    String encodedSignature = ""; // Obtener la firma digital del mensaje
//                    String publicKeyStr = ""; // Obtener la clave pública asociada a la firma
//                    String data = ""; // Obtener los datos a verificar
                    
//                    EmailVerifier.verifyEmailSignature(encodedSignature, publicKeyStr, data);
                    
                    // Si no se lanza una excepción, la verificación fue exitosa
//                    System.out.println("Mensaje verificado correctamente.");
                } catch (Exception e) {
                    System.err.println("Error al verificar mensaje: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
