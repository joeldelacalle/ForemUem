package com.forem.manager;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class GmailOAuthManager {

	

	    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	    private static final String TOKENS_DIRECTORY_PATH = "tokens";

	    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
	    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
	    	try {
	    	// Load client secrets.
	        InputStream in = GmailOAuthManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	        // Build flow and trigger user authorization request.
	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
	                .setAccessType("offline")
	                .build();

	        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
	        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	    	} catch (TokenResponseException e) {
	            System.err.println("Error during token request: " + e.getMessage());
	            if (e.getDetails() != null) {
	                System.err.println("Error details: " + e.getDetails().toPrettyString());
	            }
	            e.printStackTrace();
	            throw e;
	        }
	    }

	    public static Gmail getGmailService() throws Exception {
	        final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	        Credential credential = getCredentials(HTTP_TRANSPORT);
	        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
	                .setApplicationName(APPLICATION_NAME)
	                .build();
	    }
	    
	    /**
	     * Elimina todos los archivos en el directorio que contiene los tokens OAuth2 almacenados.
	     */
	    public static void deleteStoredTokens() {
	        File tokenDirectory = new File(TOKENS_DIRECTORY_PATH);
	        
	        // Verifica que el directorio exista y que sea un directorio válido
	        if (tokenDirectory.exists() && tokenDirectory.isDirectory()) {
	            // Itera sobre todos los archivos en el directorio y los elimina
	            for (File file : tokenDirectory.listFiles()) {
	                if (file.isFile()) {
	                    boolean deleted = file.delete();
	                    if (deleted) {
	                        System.out.println("Deleted file: " + file.getName());
	                    } else {
	                        System.err.println("Failed to delete file: " + file.getName());
	                    }
	                }
	            }
	        } else {
	            System.err.println("Directory does not exist or is not a directory: " + TOKENS_DIRECTORY_PATH);
	        }
	    }
}


