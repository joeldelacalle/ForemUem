package com.forem.main;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

public class EmlReader {

	public static void main(String[] args) {
		try {
			// Ruta al archivo .eml
			File emlFile = new File("C:/Users/layeg/forem/forem/src/main/resources/PRUEBA.eml");

			// Crear una sesión sin propiedades (no se necesita conexión real)
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);

			// Leer el archivo .eml en un objeto MimeMessage
			FileInputStream fis = new FileInputStream(emlFile);
			MimeMessage message = new MimeMessage(session, fis);

			// Imprimir cabeceras del mensaje
			Enumeration<?> headers = message.getAllHeaders();
			while (headers.hasMoreElements()) {
				Header header = (Header) headers.nextElement();
				System.out.println(header.getName() + ": " + header.getValue());
			}

			// Imprimir contenido del mensaje
			Object content = message.getContent();
			if (content instanceof String) {
				System.out.println("Contenido: " + content);
			} else if (content instanceof Multipart) {
				Multipart multipart = (Multipart) content;
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					System.out.println("Parte " + (i + 1) + ":");
					System.out.println(bodyPart.getContent());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


