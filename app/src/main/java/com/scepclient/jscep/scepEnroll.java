package com.scepclient.jscep;

import android.util.Log;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.jscep.client.Client;
import org.jscep.client.DefaultCallbackHandler;
import org.jscep.client.EnrollmentResponse;
import org.jscep.client.verification.CertificateVerifier;
import org.jscep.client.verification.OptimisticCertificateVerifier;
import org.jscep.transport.response.Capabilities;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.x500.X500Principal;

public class scepEnroll {

    public void startEnroll(String iscep_url, String icommon_name, String icountry_name, String istate_name, String iorg_name, String iorg_unit_name) {
        CertificateVerifier verifier = new OptimisticCertificateVerifier();
        CallbackHandler handler = new DefaultCallbackHandler(verifier);
        try {
            URL url = new URL(iscep_url);
            Client client = new Client(url, handler);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            Capabilities caps = client.getCaCapabilities();
            String sigAlg = caps.getStrongestSignatureAlgorithm();


            CertStore certStore = client.getCaCertificate();
            Collection<? extends Certificate> ca_chain = certStore.getCertificates(null);
            // create array
            X509Certificate[] certificatesArray = new X509Certificate[3];

            // add certificates in allCerts (the 2 that were in certStore)
            int i = 0;
            for (Certificate c : ca_chain) {
                certificatesArray[i] = (X509Certificate) c;
                i++;
            }


            Log.v("uSec", "sample");

            KeyPair requesterKeyPair = keyPairGenerator.genKeyPair();
            X500Principal requesterIssuer = new X500Principal(certificatesArray[1].getSubjectDN().getName());
            BigInteger serial = BigInteger.ONE;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1); // yesterday
            Date notBefore = calendar.getTime();
            calendar.add(Calendar.DATE, +100);
            Date notAfter = calendar.getTime();
            X500Principal requesterSubject = new X500Principal(
                    "CN=" + icommon_name + "OU=" + iorg_unit_name + "O=" + iorg_name + "L= ST=" + istate_name + "C=" + icountry_name
            );

            PublicKey requesterPubKey = requesterKeyPair.getPublic();
            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    requesterIssuer, serial, notBefore,
                    notAfter, requesterSubject, requesterPubKey
            );


            // Signing
            PrivateKey requesterPrivKey = requesterKeyPair.getPrivate(); // from generated key pair
            JcaContentSignerBuilder certSignerBuilder = new JcaContentSignerBuilder(sigAlg);
            ContentSigner certSigner = certSignerBuilder.build(requesterPrivKey);

            X509CertificateHolder certHolder = certBuilder.build(certSigner);

            JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
            X509Certificate requesterCert = converter.getCertificate(certHolder);
            KeyPair entityKeyPair = keyPairGenerator.genKeyPair();
            PublicKey entityPubKey = entityKeyPair.getPublic();
            PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(requesterSubject, entityPubKey);

            PrivateKey entityPrivKey = entityKeyPair.getPrivate();
            JcaContentSignerBuilder csrSignerBuilder = new JcaContentSignerBuilder(sigAlg);
            ContentSigner csrSigner = csrSignerBuilder.build(entityPrivKey);
            PKCS10CertificationRequest csr = csrBuilder.build(csrSigner);


            EnrollmentResponse res = client.enrol(requesterCert, requesterPrivKey, csr);
//            client.enrol()

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
