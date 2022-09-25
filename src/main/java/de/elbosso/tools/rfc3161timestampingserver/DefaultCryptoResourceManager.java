package de.elbosso.tools.rfc3161timestampingserver;

import java.net.URL;

public class DefaultCryptoResourceManager extends java.lang.Object implements CryptoResourceManager
{
    public java.net.URL getChainPem()
    {
        java.net.URL url=de.netsysit.util.ResourceLoader.getDockerSecretResource("chain.pem");
        if(url==null)
            url=de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/chain.pem");
        return url;
    }
    public java.net.URL getTsaCert()
    {
        java.net.URL url=de.netsysit.util.ResourceLoader.getDockerSecretResource("tsa.crt");
        if(url==null)
            url=de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt");
        return url;
    }

    @Override
    public URL getTsaConf()
    {
        java.net.URL url=de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/etc/tsa.conf");
        return url;
    }

    @Override
    public URL getPrivateKey()
    {
        java.net.URL url=de.netsysit.util.ResourceLoader.getDockerSecretResource("tsa.key");
        if(url==null)
            url = de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key");
        return url;
    }
}
