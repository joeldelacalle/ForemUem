package com.forem.swing;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

import com.forem.manager.GmailOAuthManager;
import com.forem.verifier.EmailVerifier;
import com.forem.verifier.EmlVerifier;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class ForemIU extends JFrame implements EmailVerifier.Logger{
    
	private static final long serialVersionUID = 1L;
	
	private JLabel titleLabel;
    private JTextPane logArea;
    private JButton startButton;
    private JButton loadEmailsButton; // Botón para cargar emails
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
        startButton = new JButton("Start Gmail email verification");
        startButton.setFont(new Font("Arial", Font.BOLD, 16)); // Botón con fuente más grande
        startButton.setBackground(new Color(0, 153, 76)); // Fondo verde
        startButton.setForeground(Color.WHITE); // Texto blanco en el botón
        
        // Inicializar el botón de carga de emails
        loadEmailsButton = new JButton("Verify selected email");
        loadEmailsButton.setFont(new Font("Arial", Font.BOLD, 16));
        loadEmailsButton.setBackground(new Color(0, 153, 76));
        loadEmailsButton.setForeground(Color.WHITE);
        
        // Crear un panel para los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2)); // Una fila, dos columnas
        buttonPanel.add(loadEmailsButton);
        buttonPanel.add(startButton);
        // Añadir el botón al panel
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Añadir ActionListener al botón de startButton
        startButton.addActionListener(e -> startVerification());     
        // Añadir ActionListener al botón de cargar emails
        loadEmailsButton.addActionListener(e -> cargarEmails());
        
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
                Style style = logArea.addStyle("Blue", null);
                StyleConstants.setForeground(style, Color.BLUE);

                doc.insertString(doc.getLength(), message + "\n", style);
                logArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void cargarEmails() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
				procesarArchivoEmails(selectedFile);
			} catch (FileNotFoundException | MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    private void procesarArchivoEmails(File file) throws FileNotFoundException, MessagingException {
    	// Crear una sesión sin propiedades (no se necesita conexión real)
    	Properties props = new Properties();
    	Session session = Session.getDefaultInstance(props, null);

    	// Leer el archivo .eml en un objeto MimeMessage
    	FileInputStream fis = new FileInputStream(file);
    	MimeMessage message = new MimeMessage(session, fis);
    	EmlVerifier.verifyEmailHeaders(message);
    	
    }
    
    private void startVerification() {
        // Clear the log area before starting
        logArea.setText("");
        // Inicializar el servicio de Gmail
        try {
            gmailService = GmailOAuthManager.getGmailService();
        } catch (Exception e) {
        	logArea.setForeground(Color.RED); // Optional: Set color for errors
         // Reset the color back to black for subsequent messages
            logArea.setForeground(Color.BLACK);
        }
        
        try {
        	String user = "me";
            ListMessagesResponse listResponse = gmailService.users().messages().list(user).execute();
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
            	logArea.setForeground(Color.RED); // Optional: Set color for errors
            	// Reset the color back to black for subsequent messages
                logArea.setForeground(Color.BLACK);
            } else {
                for (Message message : messages) {
                    Message fullMessage = gmailService.users().messages().get(user, message.getId()).execute();
                    EmailVerifier.verifyEmailHeaders(fullMessage);
                }
            }
        } catch (Exception e) {
        	//Se elimina el token para verificar que no es problema de la caducidad del mismo
        	GmailOAuthManager.deleteStoredTokens();
        	logArea.setForeground(Color.RED); // Optional: Set color for errors
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

