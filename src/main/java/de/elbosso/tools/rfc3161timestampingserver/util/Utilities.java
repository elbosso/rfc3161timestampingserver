package de.elbosso.tools.rfc3161timestampingserver.util;

public class Utilities extends java.lang.Object
{
    private Utilities()
    {
        super();
    }

    static String getEnvSysPropertyFallback(String name, String fallback)
    {
        String rv=fallback;
        if(System.getenv(name)!=null)
            rv=System.getenv(name);
        else if(System.getProperty(name)!=null)
            rv=System.getProperty(name);
        return rv;
    }
}
