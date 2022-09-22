package de.elbosso.tools.rfc3161timestampingserver;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class RequestReplyEntity
{
    java.util.List<X509Certificate> certs;
    PrivateKey privateKey;
    X509Certificate rsaSigningCert;
}
