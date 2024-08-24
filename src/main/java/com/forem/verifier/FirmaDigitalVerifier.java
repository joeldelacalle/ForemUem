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
    	// Decodificar la clave pública
        byte[] decodedKey = decodeBase64(publicKey);
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
    
    
    //Obtener la clave pública DKIM a través de DNS
    public static String fetchPublicKey(String domain, String selector) {
        try {
            String txtRecordName = selector + "._domainkey." + domain;
            Lookup lookup = new Lookup(txtRecordName, Type.TXT);
            lookup.run();

            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                TXTRecord txtRecord = (TXTRecord) lookup.getAnswers()[0];
                return txtRecord.getStrings().stream().collect(Collectors.joining());
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }
        return null;
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
    	// Eliminar caracteres no válidos (por ejemplo, espacios)
        String cleanedBValue = bValue.replaceAll("\\s", "");

        // Reemplazar caracteres URL-safe Base64 si es necesario
        cleanedBValue = cleanedBValue.replace('-', '+').replace('_', '/');

        // Verificar que la longitud sea múltiplo de 4
        while (cleanedBValue.length() % 4 != 0) {
            cleanedBValue += "=";
        }

        // Intentar decodificar la cadena
        return Base64.getDecoder().decode(cleanedBValue);
    }
}
