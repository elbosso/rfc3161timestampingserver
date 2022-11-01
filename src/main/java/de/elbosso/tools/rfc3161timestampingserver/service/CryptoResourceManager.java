package de.elbosso.tools.rfc3161timestampingserver.service;

public interface CryptoResourceManager
{
    java.net.URL getChainPem();
    java.net.URL getTsaCert();
    java.net.URL getTsaConf();
    java.net.URL getPrivateKey();
}
