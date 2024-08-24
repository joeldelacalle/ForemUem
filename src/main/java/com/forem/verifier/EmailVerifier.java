package com.forem.verifier;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;

public class EmailVerifier {

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
    private static final String[] SUSPICIOUS_PATTERNS = {"free", "credit", "urgent", "win", "winner", "prize", "100% free", "guarantee"};
    private static final String ASCII_PATTERN = "[\\x00-\\x7F]+";
    private static final String MESSAGE_ID_PATTERN = "<.+@.+\\..+>";
    private static Set<String> uniqueMessageIDs = new HashSet<>();

    // Define an interface for logging
    public interface Logger {
        void log(String message);
        void logSuccess(String message);
    }

    private static Logger logger;

    public static void setLogger(Logger logger) {
        EmailVerifier.logger = logger;
    }

    public static void verifyEmailHeaders(Message message) {
        boolean allHeadersValid = true;

        try {
            List<MessagePartHeader> headers = message.getPayload().getHeaders();
            logMessage("Message ID: " + message.getId());
            logMessage("Message Content: " + message.getSnippet());
            logMessage("Starting headers validation:");

            for (MessagePartHeader header : headers) {
                boolean headerValid = processHeader(header, headers);
                if (!headerValid) {
                    allHeadersValid = false;
                }
            }

            if (allHeadersValid) {
                logSuccess("All headers are valid of the Message ID: "+ message.getId()+  ".");
            }

        } catch (Exception e) {
            logMessage("Error verifying message headers: " + e.getMessage());
        }
    }

    private static boolean processHeader(MessagePartHeader header, List<MessagePartHeader> headers) {
        try {
            String headerName = header.getName();
            String headerValue = header.getValue();
            boolean isValid = true;

            switch (headerName) {
                case "From":
                    isValid = validateEmail("From", headerValue, true);
                    break;
                case "To":
                    isValid = validateEmail("To", headerValue, false);
                    break;
                case "Subject":
                    isValid = isValidSubject(headerValue);
                    break;
                case "Date":
                    isValid = isValidDate(headerValue);
                    break;
                case "Message-ID":
                    isValid = isValidMessageId(headerValue);
                    break;
                case "DKIM-Signature":
                    isValid = FirmaDigitalVerifier.isValidDKIMSignature(headerValue, headers);
                    break;
                case "Received-SPF":
                    isValid = headerValue != null && headerValue.contains("pass");
                    break;
                case "Reply-To":
                    isValid = headerValue == null || headerValue.isEmpty() || isValidEmail(headerValue);
                    break;
                default:
//                    logMessage(headerName + ": " + headerValue);
                    break;
            }

            if (isValid) {
                logSuccess("Valid '" + headerName + "' header. ");
            } else {
                logMessage("Invalid '" + headerName + "' header.");
            }

            return isValid;
        } catch (Exception e) {
            logMessage("Header verification failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean validateEmail(String headerName, String email, boolean isSingle) {
        if (isSingle) {
            if (email == null || !isValidEmail(email)) {
                logMessage("Invalid '" + headerName + "' header.");
                return false;
            }
        } else {
            String[] emailAddresses = email.split(",");
            for (String emailAddr : emailAddresses) {
                if (!isValidEmail(emailAddr.trim())) {
                    logMessage("Invalid '" + headerName + "' header.");
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

    private static boolean isValidDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        format.setLenient(false);
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private static boolean isValidSubject(String subject) {
        if (StringUtils.isBlank(subject)) {
            return false;
        }

        for (String pattern : SUSPICIOUS_PATTERNS) {
            if (subject.toLowerCase().contains(pattern)) {
                return false;
            }
        }

        return Pattern.matches(ASCII_PATTERN, subject);
    }

    private static boolean isValidMessageId(String messageId) {
        if (StringUtils.isBlank(messageId)) {
            return false;
        }

        if (uniqueMessageIDs.contains(messageId)) {
            return false;
        } else {
            uniqueMessageIDs.add(messageId);
        }

        return Pattern.matches(MESSAGE_ID_PATTERN, messageId);
    }

    public static void verifyEmailSignature(String encodedSignature, String publicKeyStr, String data) throws Exception {
        byte[] decodedSignature = Base64.decode(encodedSignature);
        PublicKey publicKey = getPublicKeyFromString(publicKeyStr);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());

        if (!signature.verify(decodedSignature)) {
            logMessage("Signature verification failed.");
        } else {
            logSuccess("Email signature verified successfully.");
        }
    }

    private static PublicKey getPublicKeyFromString(String key) throws Exception {
        byte[] byteKey = Base64.decode(key);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(byteKey));

        return certificate.getPublicKey();
    }

    private static void logMessage(String message) {
        if (logger != null) {
            logger.log(message);
        }
    }

    private static void logSuccess(String message) {
        if (logger != null) {
            logger.logSuccess(message);
        }
    }
}
