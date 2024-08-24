package com.forem.verifier;

import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.internet.MimeMessage;




public class EmlVerifier {

	 public static void verifyEmailHeaders(MimeMessage message) {
	        boolean allHeadersValid = true;

	        try {
	            EmailVerifier.logMessage("Message ID: " + message.getMessageID());
	            EmailVerifier.logMessage("Starting headers validation:");

	            Enumeration<Header> headers = message.getAllHeaders();
	        	while (headers.hasMoreElements()) {
	        		Header header = (Header) headers.nextElement();
	        		boolean headerValid = emlProcessHeaders(header);
	        		if (!headerValid) {
	                    allHeadersValid = false;
	                }
	        	}

	            if (allHeadersValid) {
	            	EmailVerifier.logSuccess("All headers are valid of the Message ID: "+ message.getMessageID()+  ".");
	            }

	        } catch (Exception e) {
	        	EmailVerifier.logMessage("Error verifying message headers: " + e.getMessage());
	        }
	    }
	
	 private static boolean emlProcessHeaders(Header header) {
	        try {
	            String headerName = header.getName();
	            String headerValue = header.getValue();
	            boolean isValid = true;

	            switch (headerName) {
	                case "From":
	                    isValid = EmailVerifier.validateEmail("From", headerValue, true);
	                    break;
	                case "To":
	                    isValid = EmailVerifier.validateEmail("To", headerValue, false);
	                    break;
	                case "Subject":
	                    isValid = EmailVerifier.isValidSubject(headerValue);
	                    break;
	                case "Date":
	                    isValid = EmailVerifier.isValidDate(headerValue);
	                    break;
	                case "Message-ID":
	                    isValid = EmailVerifier.isValidMessageId(headerValue);
	                    break;
	                case "Received-SPF":
	                    isValid = headerValue != null && headerValue.contains("pass");
	                    break;
	                case "Reply-To":
	                    isValid = headerValue == null || headerValue.isEmpty() || EmailVerifier.isValidEmail(headerValue);
	                    break;
	                default:
//	                    logMessage(headerName + ": " + headerValue);
	                    break;
	            }

	            if (isValid) {
	            	EmailVerifier.logSuccess("Valid '" + headerName + "' header. ");
	            } else {
	            	EmailVerifier.logMessage("Invalid '" + headerName + "' header.");
	            }

	            return isValid;
	        } catch (Exception e) {
	        	EmailVerifier.logMessage("Header verification failed: " + e.getMessage());
	            return false;
	        }
	    }
}
