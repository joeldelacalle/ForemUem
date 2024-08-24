package com.forem.swing;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.util.List;
import com.forem.manager.GmailOAuthManager;
import com.forem.verifier.EmailVerifier;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class ForemIU extends JFrame implements EmailVerifier.Logger{
    
	private static final long serialVersionUID = 1L;
	
	private JLabel titleLabel;
    private JTextPane logArea;
    private JButton startButton;
    private JPanel panel;
    private Gmail gmailService;
    
    public ForemIU() {
    	// Configuración básica de la ventana
        setTitle("Forem Email Verifier");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar la ventana

        // Configuración del panel principal
        panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10)); // Añadir espaciado entre componentes
        panel.setBackground(Color.WHITE); // Fondo blanco para una apariencia más limpia
        add(panel);

        // Etiqueta de título
        titleLabel = new JLabel("Forem Email Verifier");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Fuente grande y negrita
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centrar el título
        titleLabel.setForeground(new Color(0, 102, 204)); // Color azul suave para el título
        panel.add(titleLabel, BorderLayout.NORTH);

        // Área de texto para el registro/logs
        logArea = new JTextPane();
        logArea.setFont(new Font("Courier New", Font.PLAIN, 14)); // Fuente de fácil lectura
//        logArea.setForeground(Color.DARK_GRAY); // Color gris oscuro para el texto
        logArea.setBackground(Color.LIGHT_GRAY); // Fondo gris claro para mayor contraste
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Botón de inicio
        startButton = new JButton("Start Verification");
        startButton.setFont(new Font("Arial", Font.BOLD, 16)); // Botón con fuente más grande
        startButton.setBackground(new Color(0, 153, 76)); // Fondo verde
        startButton.setForeground(Color.WHITE); // Texto blanco en el botón
        panel.add(startButton, BorderLayout.SOUTH);
        startButton.addActionListener(e -> startVerification());     
        
    }

    @Override
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = logArea.getStyledDocument();
                Style style = logArea.addStyle("Default", null);
                StyleConstants.setForeground(style, Color.BLACK);

                doc.insertString(doc.getLength(), message + "\n", style);
                logArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void logSuccess(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = logArea.getStyledDocument();
                Style style = logArea.addStyle("Green", null);
                StyleConstants.setForeground(style, Color.BLUE);

                doc.insertString(doc.getLength(), message + "\n", style);
                logArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    private void startVerification() {
        // Clear the log area before starting
        logArea.setText("");
        // Inicializar el servicio de Gmail
        try {
            gmailService = GmailOAuthManager.getGmailService();
        } catch (Exception e) {
        	logArea.setForeground(Color.RED); // Optional: Set color for errors
//            logArea.add("Error: " + e.getMessage() + "\n");
         // Reset the color back to black for subsequent messages
            logArea.setForeground(Color.BLACK);
        }
        
        try {
        	String user = "me";
            ListMessagesResponse listResponse = gmailService.users().messages().list(user).execute();
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
            	logArea.setForeground(Color.RED); // Optional: Set color for errors
//            	logArea.append("No messages found.");
            	// Reset the color back to black for subsequent messages
                logArea.setForeground(Color.BLACK);
            } else {
                for (Message message : messages) {
                    Message fullMessage = gmailService.users().messages().get(user, message.getId()).execute();
                    EmailVerifier.verifyEmailHeaders(fullMessage);
                }
            }
        } catch (Exception e) {
        	logArea.setForeground(Color.RED); // Optional: Set color for errors
//        	logArea.append("Error verifying emails: " + e.getMessage());
        	// Reset the color back to black for subsequent messages
            logArea.setForeground(Color.BLACK);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ForemIU ui = new ForemIU();
            ui.setVisible(true);
         // Configurar el logger en EmailVerifier
            EmailVerifier.setLogger(ui);
        });
    }
    
}

