package com.forem.verifier;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.security.PublicKey;
import java.security.Signature;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;


import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;

public class EmailVerifier1 {
	
	private static Set<String> uniqueMessageIDs = new HashSet<>();
	// Definir el formato de fecha
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

		
	public static void verifyEmailHeaders(Message message) {
        try {
        	List<MessagePartHeader> headers = message.getPayload().getHeaders();

            System.out.println("Message ID: " + message.getId());
            System.out.println("Snippet: " + message.getSnippet());
            System.out.println("Headers:");
            
            for (MessagePartHeader header : headers) {
            	cabeceras(header, headers);
            }
           
        } catch (Exception e) {
            System.err.println("Error al verificar cabeceras del mensaje: " + e.getMessage());
        }
    
    }

	private static void cabeceras(MessagePartHeader header, List<MessagePartHeader> headers) {
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
            	if (from == null || !isValidEmail(from)) {
                    throw new Exception("Invalid 'From' header.");
                }
            	break;
            case "To":
            	to = header.getValue();
            	 // To validation
                if (to == null || !isValidEmail(to)) {
                    throw new Exception("Invalid 'To' header.");
                }
            	break;
            case "Subject":
            	subject = header.getValue();
            	// Subject validation
                if (subject == null || !isValidSubject(subject)) {
                    throw new Exception("Invalid 'Subject' header.");
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
                if (dkimSignature == null || !FirmaDigitalVerifier.isValidDKIMSignature(dkimSignature,headers)) {
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
                if (replyTo != null && !replyTo.isEmpty() && !isValidEmail(replyTo)) {
                    throw new Exception("Invalid 'Reply-To' header.");
                }
            	break;
            }
        } catch (Exception e) {
            System.err.println("Header verification failed: " + e.getMessage());
        }
	}

	//From: Debe ser una dirección de correo electrónico válida.
	//To: Debe contener una o más direcciones de correo electrónico válidas.
	//Reply-To: Si está presente, debe ser una dirección de correo electrónico válida.
	private static boolean isValidEmail(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
	 
	//Date: Debe ser una fecha válida y estar en el formato correcto.
    private static boolean isValidDate(String date) {
    	SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        format.setLenient(false);  // Establecer en false para una verificación estricta

        try {
        	// Intentar analizar la fecha
        	format.parse(date);
        	return true;
        } catch (ParseException e) {
        	// Si ocurre una excepción, la fecha no es válida
        	return false;
        }
    }
    private static boolean isValidSubject(String subject) {
        if (StringUtils.isBlank(subject)) {
            return false;
        }

        // Detección de patrones sospechosos en el Subject
        String[] suspiciousPatterns = {"free", "credit", "urgent", "win", "winner", "prize", "100% free", "guarantee"};
        for (String pattern : suspiciousPatterns) {
            if (subject.toLowerCase().contains(pattern)) {
                return false;
            }
        }

        // Verificar si el Subject contiene caracteres extraños o codificación inusual
        String asciiPattern = "[\\x00-\\x7F]+";
        if (!Pattern.matches(asciiPattern, subject)) {
            return false;
        }

        return true;
    }

    private static boolean isValidMessageId(String messageId) {
        if (StringUtils.isBlank(messageId)) {
            return false;
        }

        // Verificar la unicidad del Message-ID
        if (uniqueMessageIDs.contains(messageId)) {
            return false;
        } else {
            uniqueMessageIDs.add(messageId);
        }

        // Verificar el formato del Message-ID
        String messageIdPattern = "<.+@.+\\..+>";
        if (!Pattern.matches(messageIdPattern, messageId)) {
            return false;
        }

        return true;
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
