package de.elbosso.tools.rfc3161timestampingserver.util;

import jdk.jshell.execution.Util;

public class DockerSecrets
{
	private static final org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(DockerSecrets.class);
	private static final org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");

    public static java.lang.String readPassword(java.lang.String dockerSecretsFile,java.lang.String envPropertyName, java.lang.String fallback)
    {
        java.lang.String pw=null;
        java.net.URL url=de.netsysit.util.ResourceLoader.getDockerSecretResource(dockerSecretsFile);
        try
        {
            if (url != null)
            {
                java.io.InputStream is = url.openStream();
                pw = de.elbosso.util.io.Utilities.readIntoString(is).trim();
                is.close();
            }
        }
        catch(java.io.IOException exp)
        {
            CLASS_LOGGER.error(exp.getMessage(),exp);
        }
        if(pw==null)
            pw= Utilities.getEnvSysPropertyFallback(envPropertyName,fallback);
        return pw;
    }
}
