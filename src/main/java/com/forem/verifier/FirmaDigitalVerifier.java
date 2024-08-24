package com.forem.verifier;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.forem.model.DKIMSignature;
import com.google.api.services.gmail.model.MessagePartHeader;

import org.xbill.DNS.*;

public class FirmaDigitalVerifier {

	public static boolean isValidDKIMSignature(String dkimSignature, List<MessagePartHeader> headers) {
        if (StringUtils.isBlank(dkimSignature)) {
            return false;
        }

        try {
            DKIMSignature parsedDKIM = DKIMSignature.parse(dkimSignature);
            String dValue = parsedDKIM.getDValue();
            String selector = parsedDKIM.getSValue();
            String publicKey = fetchPublicKey(dValue, selector);

            if (publicKey == null) {
                return false;
            }

            String headerFields = parsedDKIM.getHValue();
            String bodyHash = parsedDKIM.getBHValue();
            String signedHeaders = getSignedHeaders(headers, headerFields);
            byte[] signature = decodeBase64(parsedDKIM.getBValue());

            return verifyDKIMSignature(publicKey, signedHeaders, bodyHash, signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean verifyDKIMSignature(String publicKey, String signedHeaders, String bodyHash, byte[] signature) throws Exception {
    	// Extraer la parte de la clave pública después de 'p='
        String base64PublicKey = extractPublicKey(publicKey);
    	// Prepara la clave pública
        String preparedPublicKey = prepareBase64String(base64PublicKey);

        // Decodificar la clave pública
        byte[] decodedKey = decodeBase64(preparedPublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA","BC");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        // Configurar el objeto Signature para verificar la firma
        Signature sig = Signature.getInstance("SHA256withRSA", "BC");
        sig.initVerify(pubKey);
        sig.update(signedHeaders.getBytes());
        sig.update(bodyHash.getBytes());

        // Verificar la firma
        return sig.verify(signature);
    }
    
    
    public static String fetchPublicKey(String domain, String selector) {
        try {
            String txtRecordName = selector + "._domainkey." + domain;
            Lookup lookup = new Lookup(txtRecordName, Type.TXT);
            lookup.run();

            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                TXTRecord txtRecord = (TXTRecord) lookup.getAnswers()[0];
                // Obtener la cadena completa
                String publicKeyString = txtRecord.getStrings().stream().collect(Collectors.joining());

                // Limpiar y preparar la cadena Base64
                return prepareBase64String(publicKeyString);
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String prepareBase64String(String base64) {
        // Eliminar cualquier espacio o salto de línea
        String cleanedBase64 = base64.replaceAll("\\s", "");

        // Reemplazar caracteres URL-safe con caracteres Base64 estándar
        cleanedBase64 = cleanedBase64.replace('-', '+').replace('_', '/');

     // Asegurarse de que la longitud sea múltiplo de 4
        int padding = 4 - (cleanedBase64.length() % 4);
        if (padding != 4) {
            cleanedBase64 = cleanedBase64 + "=".repeat(padding);
        }

        return cleanedBase64;
    }
    private static String extractPublicKey(String dkimPublicKeyRecord) {
        String[] parts = dkimPublicKeyRecord.split(";");
        for (String part : parts) {
            if (part.startsWith("p=")) {
                return part.substring(2).trim();
            }
        }
        throw new IllegalArgumentException("Public key part 'p=' not found in DKIM record");
    }
    
    private static String getSignedHeaders(List<MessagePartHeader> headers, String headerFields) {
        // Reconstruir los headers firmados de acuerdo a `h` en DKIM-Signature
    	List<String> signedHeaderNames = Arrays.asList(headerFields.split(":"));
        StringBuilder signedHeaders = new StringBuilder();

        for (String headerName : signedHeaderNames) {
            for (MessagePartHeader header : headers) {
                if (header.getName().equalsIgnoreCase(headerName.trim())) {
                    signedHeaders.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
                }
            }
        }

        return signedHeaders.toString();
    }
    
    public static byte[] decodeBase64(String bValue) {
//        System.out.println("Original string: " + bValue);
        
        if (bValue == null || bValue.isEmpty()) {
            throw new IllegalArgumentException("The input string is null or empty.");
        }

        String cleanedBValue = bValue.replaceAll("\\s", "");
//        System.out.println("Cleaned string: " + cleanedBValue);

        int length = cleanedBValue.length();
        if (length % 4 != 0) {
            int paddingLength = 4 - (length % 4);
            cleanedBValue += "=".repeat(paddingLength);
        }

//        System.out.println("Padded string: " + cleanedBValue);

        try {
            return Base64.getDecoder().decode(cleanedBValue);
        } catch (IllegalArgumentException e) {
            System.err.println("Error during Base64 decoding: " + e.getMessage());
            throw e;
        }
    }
}
