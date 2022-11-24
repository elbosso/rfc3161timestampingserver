package de.elbosso.tools.rfc3161timestampingserver;

import de.elbosso.tools.rfc3161timestampingserver.dao.DaoFactory;
import de.elbosso.tools.rfc3161timestampingserver.dao.Rfc3161timestampDao;
import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.domain.TotalNumber;
import de.elbosso.tools.rfc3161timestampingserver.service.CryptoResourceManager;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.micrometer.core.instrument.Metrics;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.jcajce.JcaCRLStore;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Optional;

public class AdminHandlers extends Object implements Constants
{
    private final static org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(AdminHandlers.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final CryptoResourceManager cryptoResourceManager;
    private final DaoFactory df;

    AdminHandlers(DaoFactory df, CryptoResourceManager cryptoResourceManager)
    {
        super();
        this.df=df;
        this.cryptoResourceManager=cryptoResourceManager;
    }

    public void handlePostTotalNumber(Context ctx) throws Exception
    {
        CLASS_LOGGER.debug("Post for total number of timestamps in database");
        Rfc3161timestampDao timestampDao=df.createRfc3161timestampDao();
        Optional<Long> totalNumber=timestampDao.findTotalNumber();
        if(totalNumber.isPresent())
        {
            CLASS_LOGGER.info("Found some entries in database");
            Long bi = totalNumber.get();
            ctx.status(200);
            ctx.json(new TotalNumber(bi.longValue()));
            Metrics.counter("rfc3161timestampingserver.postTotalNumber", "resourcename","admin/totalNumber","httpstatus", Integer.toString(ctx.status()),"success","true","remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
        }
        else
        {
            CLASS_LOGGER.info("No entry found in database");
            ctx.status(404);
            Metrics.counter("rfc3161timestampingserver.postTotalNumber", "resourcename","admin/totalNumber","httpstatus", Integer.toString(ctx.status()),"success","false","remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
        }
    }
    public void handlePostYoungest(Context ctx) throws Exception
    {
        CLASS_LOGGER.debug("Post for youngest timestamp in database");
        Rfc3161timestampDao timestampDao=df.createRfc3161timestampDao();
        Optional<Rfc3161timestamp> youngest=timestampDao.findYoungest();
        if(youngest.isPresent())
        {
            CLASS_LOGGER.info("Found some entries in database");
            Rfc3161timestamp timestamp = youngest.get();
            ctx.status(200);
            ctx.json(timestamp);
            Metrics.counter("rfc3161timestampingserver.postYoungest", "resourcename","admin/youngest","httpstatus", Integer.toString(ctx.status()),"success","true","remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
        }
        else
        {
            CLASS_LOGGER.info("No entry found in database");
            ctx.status(404);
            Metrics.counter("rfc3161timestampingserver.postYoungest", "resourcename","admin/youngest","httpstatus", Integer.toString(ctx.status()),"success","false","remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
        }
    }
 }
