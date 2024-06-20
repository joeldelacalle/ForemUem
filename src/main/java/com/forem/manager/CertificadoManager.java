package com.forem.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificadoManager {
	// Método para cargar el certificado y extraer la clave pública
    public PublicKey cargarClavePublicaCertificado(X509Certificate certificate) {
        return certificate.getPublicKey();
    }

    // Método para verificar la validez del certificado
    public void validarCertificado(X509Certificate certificate) throws CertificateException{
        certificate.checkValidity();
    }
}
