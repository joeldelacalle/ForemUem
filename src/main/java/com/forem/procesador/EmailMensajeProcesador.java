package com.forem.procesador;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.InputStream;
import java.util.Enumeration;
public class EmailMensajeProcesador {

	public void processMessages(Folder folder) throws MessagingException {
		Message[] messages = folder.getMessages();

		for (Message message : messages) {
			if (message instanceof MimeMessage) {
				MimeMessage mimeMessage = (MimeMessage) message;
				printMessageHeaders(mimeMessage);
			}
		}
	}

	private void printMessageHeaders(MimeMessage message) throws MessagingException {
		try {
			System.out.println("Subject: " + message.getSubject());
			System.out.println("From: " + message.getFrom()[0]);
			System.out.println("To: " + message.getAllRecipients()[0]);
			System.out.println("Sent Date: " + message.getSentDate());

			// Verificar otras cabeceras
			String[] headers = message.getHeader("X-Your-Header");
			if (headers != null) {
				for (String header : headers) {
					System.out.println("X-Your-Header: " + header);
				}
			} else {
				System.out.println("X-Your-Header: No encontrado");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void verifyHeaders(MimeMessage message) throws MessagingException {
        System.out.println("Verificando cabeceras del mensaje:");
        Enumeration<Header> headers = message.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header header = headers.nextElement();
            System.out.println(header.getName() + ": " + header.getValue());
        }
    }

    public X509Certificate extractCertificateFromMessage(MimeMessage message) throws Exception {
        if (message.isMimeType("multipart/signed")) {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContentType().startsWith("application/pkcs7-signature") ||
                    bodyPart.getContentType().startsWith("application/x-pkcs7-signature")) {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    try (InputStream is = bodyPart.getInputStream()) {
                        return (X509Certificate) certFactory.generateCertificate(is);
                    }
                }
            }
        }
        return null;
    }
	
}
