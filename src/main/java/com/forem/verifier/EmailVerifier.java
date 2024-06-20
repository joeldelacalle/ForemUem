package com.forem.verifier;

import java.util.List;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.security.cert.X509Certificate;
import java.security.PublicKey;
import java.security.Signature;

import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;

public class EmailVerifier {
	public static void verifyEmailHeaders(Message message) {
        try {
        	List<MessagePartHeader> headers = message.getPayload().getHeaders();

            System.out.println("Message ID: " + message.getId());
            System.out.println("Snippet: " + message.getSnippet());
            System.out.println("Headers:");
            
            for (MessagePartHeader header : headers) {
            	cabeceras(header);
            }
           
        } catch (Exception e) {
            System.err.println("Error al verificar cabeceras del mensaje: " + e.getMessage());
        }
    
    }

	private static void cabeceras(MessagePartHeader header) {
		try {
            
            String from = null;
            String to = null;
            String subject = null;
            String date = null;
            String messageId = null;
            String dkimSignature = null;
            String spf = null;
            String replyTo = null;

            switch (header.getName()) {
            case "From":
            	from = header.getValue();
            	// From validation
            	if (from == null || !from.endsWith("@gmail.com")) {
                    throw new Exception("Invalid 'From' header.");
                }
            	break;
            case "To":
            	to = header.getValue();
            	 // To validation
                if (to == null || !to.endsWith("@gmail.com")) {
                    throw new Exception("Invalid 'To' header.");
                }
            	break;
            case "Subject":
            	subject = header.getValue();
            	// Subject validation
                if (subject == null || subject.isEmpty()) {
                    throw new Exception("Missing or empty 'Subject' header.");
                }
            	break;
            case "Date":
            	date = header.getValue();
            	 // Date validation
                if (date == null || !isValidDate(date)) {
                    throw new Exception("Invalid 'Date' header.");
                }
            	break;
            case "Message-ID":
            	messageId = header.getValue();
            	// Message-ID validation
                if (messageId == null || !isValidMessageId(messageId)) {
                    throw new Exception("Invalid 'Message-ID' header.");
                }
            	break;
            case "DKIM-Signature":
            	dkimSignature = header.getValue();
            	// DKIM-Signature validation
                if (dkimSignature == null || !isValidDkimSignature(dkimSignature)) {
                    throw new Exception("Invalid 'DKIM-Signature' header.");
                }
            	break;
            case "Received-SPF":
            	spf = header.getValue();
            	// SPF validation
                if (spf == null || !spf.contains("pass")) {
                    throw new Exception("SPF validation failed.");
                }
            	break;
            case "Reply-To":
            	replyTo = header.getValue();
            	// Reply-To validation
                if (replyTo != null && !isValidEmail(replyTo)) {
                    throw new Exception("Invalid 'Reply-To' header.");
                }
            	break;
            }

            System.out.println("Email headers verified successfully.");
        } catch (Exception e) {
            System.err.println("Header verification failed: " + e.getMessage());
        }
	}

    private static boolean isValidDate(String date) {
        // Implement date validation logic
        return true; // Placeholder
    }

    private static boolean isValidMessageId(String messageId) {
        // Implement Message-ID validation logic
        return true; // Placeholder
    }

    private static boolean isValidDkimSignature(String dkimSignature) {
        // Implement DKIM-Signature validation logic
        return true; // Placeholder
    }

    private static boolean isValidEmail(String email) {
        // Implement email validation logic
        return true; // Placeholder
    }

    public static void verifyEmailSignature(String encodedSignature, String publicKeyStr, String data) throws Exception {
        byte[] decodedSignature = Base64.decode(encodedSignature);
        PublicKey publicKey = getPublicKeyFromString(publicKeyStr);
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        
        boolean isVerified = signature.verify(decodedSignature);
        if (!isVerified) {
            throw new Exception("Signature verification failed.");
        }
        
        System.out.println("Email signature verified successfully.");
    }

    private static PublicKey getPublicKeyFromString(String key) throws Exception {
    	 byte[] byteKey = Base64.decode(key);
         
         // Convert byte array to X509Certificate
         CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
         X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(byteKey));
         
         // Return the public key from the certificate
         return certificate.getPublicKey();
    
    }

}
