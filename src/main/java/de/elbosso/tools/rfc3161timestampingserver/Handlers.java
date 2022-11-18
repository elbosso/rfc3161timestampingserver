package de.elbosso.tools.rfc3161timestampingserver;

import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161Timestamp;
import de.elbosso.tools.rfc3161timestampingserver.service.CryptoResourceManager;
import de.elbosso.tools.rfc3161timestampingserver.util.PersistenceManager;
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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.Date;

public class Handlers extends java.lang.Object implements Constants
{
    private final static org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(Handlers.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private EntityManager em;
    private final CryptoResourceManager cryptoResourceManager;

    Handlers(CryptoResourceManager cryptoResourceManager)
    {
        this(null,cryptoResourceManager);
    }
    Handlers(EntityManager em,CryptoResourceManager cryptoResourceManager)
    {
        super();
        this.em=em;
        this.cryptoResourceManager=cryptoResourceManager;
    }

    public void handleGetChain(io.javalin.http.Context ctx) throws java.lang.Exception
    {
        CLASS_LOGGER.debug("GET for chain.pem");
        java.net.URL url=cryptoResourceManager.getChainPem();
        java.io.InputStream is=url.openStream();
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
        byte[] content=baos.toByteArray();
        ctx.status(201);
        ctx.contentType("application/pkcs7-mime");
        ctx.result(new java.io.ByteArrayInputStream(content));
        Metrics.counter("rfc3161timestampingserver.get", "resourcename","chain.pem", "remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
    }
    public void handleGetSignerCert(io.javalin.http.Context ctx) throws java.lang.Exception
    {
        CLASS_LOGGER.debug("GET for tsa.crt");
        java.net.URL url=cryptoResourceManager.getTsaCert();
        java.io.InputStream is=url.openStream();
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
        byte[] content=baos.toByteArray();
        ctx.status(201);
        ctx.contentType("application/pkix-cert");
        ctx.result(new java.io.ByteArrayInputStream(content));
        Metrics.counter("rfc3161timestampingserver.get", "resourcename","tsa.crt","remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
    }
    public void handleGetTsaConf(io.javalin.http.Context ctx) throws java.lang.Exception
    {
        CLASS_LOGGER.debug("GET for tsa.conf");
        java.net.URL url=cryptoResourceManager.getTsaConf();
        java.io.InputStream is=url.openStream();
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
        byte[] content=baos.toByteArray();
        ctx.status(201);
        ctx.contentType("text/plain");
        ctx.result(new java.io.ByteArrayInputStream(content));
        Metrics.counter("rfc3161timestampingserver.get", "resourcename","tsa.conf","remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
    }
    public void handlePostQuery(io.javalin.http.Context ctx) throws java.lang.Exception
    {
        java.lang.String contentType=ctx.contentType();
        if((contentType.startsWith("multipart/form-data"))||(contentType.startsWith("application/x-www-form-urlencoded")))
        {
            CLASS_LOGGER.debug("request is multipart/form-data or application/x-www-form-urlencoded - searching for parameters algoid and msgDigest");
            String algoid=ctx.formParam("algoid");
            if(algoid!=null)
            {
                 CLASS_LOGGER.debug("found algoid to be " + algoid);
            }
            else
            {
                 CLASS_LOGGER.warn("did not find algoid");
            }
            String msgDigestBase64 = ctx.formParam("msgDigestBase64");
            if(msgDigestBase64!=null)
            {
                 CLASS_LOGGER.debug("found msgDigestBase64 to be " + msgDigestBase64);
            }
            else
            {
                 CLASS_LOGGER.warn("did not find msgDigestBase64");
            }
            String msgDigestHex = ctx.formParam("msgDigestHex");
            if(msgDigestHex!=null)
            {
                 CLASS_LOGGER.debug("found msgDigestHex to be " + msgDigestHex);
            }
            else
            {
                 CLASS_LOGGER.warn("did not find msgDigestHex");
            }
            if(em==null)
                em= PersistenceManager.getSharedInstance().getEntityManager();
            if((algoid!=null)&&(msgDigestBase64!=null))
            {
                 CLASS_LOGGER.debug("searching using message digest algorithm and message digest (Base64) imprint as parameters");
                Query namedQuery = em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgDigestAndImprintBase64");
                namedQuery.setParameter("Alg", algoid);
                namedQuery.setParameter("Imprint", msgDigestBase64);
                namedQuery.setMaxResults(1);
                java.util.List resultList= namedQuery.getResultList();
                if(resultList.isEmpty()==false)
                {
                    CLASS_LOGGER.info("Entry found in database");
                    Rfc3161Timestamp rfc3161Timestamp = (Rfc3161Timestamp) resultList.get(0);
                    ctx.status(201);
                    ctx.contentType("application/timestamp-reply");
                    ctx.header("Content-Disposition","filename=\"queried.tsr\"");
                    setCachingPolixy(ctx);
                    ctx.result(new java.io.ByteArrayInputStream(rfc3161Timestamp.getTsrData()));
                    Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"params","alg+base64","success","true","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                }
                else
                {
                    CLASS_LOGGER.info("No entry found in database");
                    ctx.status(404);
                    Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"params","alg+base64","success","false","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                }
            }
            else if(msgDigestBase64!=null)
            {
                 CLASS_LOGGER.debug("searching using only message digest (Base64) imprint as parameter");
                Query namedQuery = em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgImprintBase64");
                namedQuery.setParameter("Imprint", msgDigestBase64);
                namedQuery.setMaxResults(1);
                java.util.List resultList= namedQuery.getResultList();
                if(resultList.isEmpty()==false)
                {
                    CLASS_LOGGER.info("Entry found in database");
                    Rfc3161Timestamp rfc3161Timestamp = (Rfc3161Timestamp) resultList.get(0);
                    ctx.status(201);
                    ctx.contentType("application/timestamp-reply");
                    ctx.header("Content-Disposition","filename=\"queried.tsr\"");
                    setCachingPolixy(ctx);
                    ctx.result(new java.io.ByteArrayInputStream(rfc3161Timestamp.getTsrData()));
                    Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"params","base64","success","true","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                }
                else
                {
                    CLASS_LOGGER.info("No entry found in database");
                    ctx.status(404);
                    Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"params","base64","success","false","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                }
            }
            else if(msgDigestHex!=null)
            {
                 CLASS_LOGGER.debug("searching using only message digest (hex) imprint as parameter");
                Query namedQuery = em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgImprintHex");
                namedQuery.setParameter("Imprint", msgDigestHex.toUpperCase());
                namedQuery.setMaxResults(1);
                java.util.List resultList= namedQuery.getResultList();
                if(resultList.isEmpty()==false)
                {
                    CLASS_LOGGER.info("Entry found in database");
                    Rfc3161Timestamp rfc3161Timestamp = (Rfc3161Timestamp) resultList.get(0);
                    ctx.status(201);
                    ctx.contentType("application/timestamp-reply");
                    ctx.header("Content-Disposition","filename=\"queried.tsr\"");
                    setCachingPolixy(ctx);
                    ctx.result(new java.io.ByteArrayInputStream(rfc3161Timestamp.getTsrData()));
                    Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"params","hex","success","true","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                }
                else
                {
                    CLASS_LOGGER.info("No entry found in database");
                    ctx.status(404);
                    Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"params","hex","success","false","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                }
            }
            else
            {
                CLASS_LOGGER.error("Not all needed information present");
                ctx.status(500);
                Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"error","params","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
            }
        }
        else
        {
             CLASS_LOGGER.error("request is not multipart/form-data or application/x-www-form-urlencoded");
            ctx.status(500);
            Metrics.counter("rfc3161timestampingserver.post", "resourcename","query","httpstatus",java.lang.Integer.toString(ctx.status()),"error","encoding","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
        }
    }

    private void setCachingPolixy(Context ctx)
    {
        ctx.header("Pragma", "no-cache");
        ctx.header("Cache-Control", "no-cache");
        ctx.header("Cache-Control", "no-store");
        ctx.header("Cache-Control", "max-age=0");
        ctx.header("Cache-Control", "must-revalidate");
    }

    public void handlePost(io.javalin.http.Context ctx) throws java.lang.Exception
    {
        CLASS_LOGGER.debug("received timestamp request");
        byte[] tsq=null;
        java.lang.String contentType=ctx.contentType();
        if(ctx.contentType().equals("application/timestamp-query"))
        {
            CLASS_LOGGER.debug("timestamp query data in body of request");
            //curl -H "Content-Type: application/timestamp-query" --data-binary '@../../work/expect-dialog-ca.git/_priv/create_ca.sh.tsq' http://localhost:7000/
            CLASS_LOGGER.debug("length of timestamp query in body " + ctx.bodyAsBytes().length);
            tsq=ctx.bodyAsBytes();
        }
        else if(ctx.contentType().startsWith("multipart/form-data"))
        {
            CLASS_LOGGER.debug("request is multipart/form-data - searching for timestamp query under key\"tsq\"");
            //curl -F "tsq=@../../work/expect-dialog-ca.git/_priv/create_ca.sh.tsq" http://localhost:7000/
            UploadedFile uploadedFile=ctx.uploadedFile("tsq");
            if(uploadedFile!=null)
            {
                CLASS_LOGGER.debug("found it - timestamp query length is "+uploadedFile.getContentLength());
                java.io.InputStream is=ctx.uploadedFile("tsq").getContent();
                java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
                de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
                tsq=baos.toByteArray();
            }
            //curl -F "file=@../../work/expect-dialog-ca.git/_priv/create_ca.sh.tsq" http://localhost:7000/
            else
            {
                CLASS_LOGGER.error("no field named \"tsq\" found in form data . corrupted request?");
                Metrics.counter("rfc3161timestampingserver.post", "resourcename","/","httpstatus","500","error","tsq not found","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
            }
        }
        else
        {
             CLASS_LOGGER.error("request is not multipart/form-data or application/x-www-form-urlencoded");
            Metrics.counter("rfc3161timestampingserver.post", "resourcename","/","httpstatus","500","error","encoding","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
        }
        if(tsq!=null)
        {
            if(em==null)
                em=PersistenceManager.getSharedInstance().getEntityManager();
            em.getTransaction().begin();
            try
            {
                RequestReplyEntity rre=new RequestReplyEntity();

                TimeStampRequest timeStampRequest=createTimestampRequest(tsq,rre);

                Rfc3161Timestamp rfc3161Timestamp=new Rfc3161Timestamp();
                rfc3161Timestamp.setCreationDate(Date.from(Clock.systemUTC().instant()));
                rfc3161Timestamp.setMessageImprintAlgOID(timeStampRequest.getMessageImprintAlgOID().getId());
                rfc3161Timestamp.setMessageImprintDigestBase64(de.elbosso.util.Utilities.base64Encode(timeStampRequest.getMessageImprintDigest()));
                rfc3161Timestamp.setMessageImprintDigestHex(de.elbosso.util.Utilities.formatHexDump(timeStampRequest.getMessageImprintDigest(),false).toUpperCase());
                em.persist(rfc3161Timestamp);

                byte[] tsr=executeTsaProtocol(timeStampRequest,rre,rfc3161Timestamp);

                rfc3161Timestamp.setTsrData(tsr);
                em.persist(rfc3161Timestamp);

                
                    CLASS_LOGGER.debug("Timestamp Response created - length: " + tsr.length);

                
                    CLASS_LOGGER.debug("Response (Base64): " + de.elbosso.util.Utilities.base64Encode(tsr));

                ctx.status(201);
                ctx.contentType("application/timestamp-reply");
                ctx.header("Content-Disposition","filename=\"reply.tsr\"");
                setCachingPolixy(ctx);
                ctx.result(new java.io.ByteArrayInputStream(tsr));
                em.getTransaction().commit();
                Metrics.counter("rfc3161timestampingserver.post", "resourcename","/","httpstatus",java.lang.Integer.toString(ctx.status()),"success","true","contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
            }
            catch(java.lang.Throwable t)
            {
                CLASS_LOGGER.error(t.getMessage(),t);
                ctx.status(500);
                Metrics.counter("rfc3161timestampingserver.post", "resourcename","/","httpstatus",java.lang.Integer.toString(ctx.status()),"error",(t.getMessage()!=null?t.getMessage():"NPE"),"contentType",contentType,"remoteAddr",ctx.ip(),"remoteHost",ctx.ip(), "localAddr",ctx.host(), "localName",ctx.host()).increment();
                em.getTransaction().rollback();
            }
            finally
            {
            }
        }
        else
        {
            ctx.status(500);
        }
    }
    private byte[] executeTsaProtocol(TimeStampRequest timeStampRequest, RequestReplyEntity rre,Rfc3161Timestamp rfc3161Timestamp) throws OperatorCreationException, CertificateException, CRLException, IOException, TSPException
    {
        AlgorithmIdentifier digestAlgorithm = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384);
        DigestCalculatorProvider digProvider = new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build();

        TimeStampTokenGenerator tsTokenGen = new TimeStampTokenGenerator(
                new JcaSimpleSignerInfoGeneratorBuilder().build("SHA384withRSA", rre.privateKey, rre.rsaSigningCert),
                digProvider.get(digestAlgorithm),
                timeStampRequest.getReqPolicy()!=null?timeStampRequest.getReqPolicy():new ASN1ObjectIdentifier("0.4.0.2023.1.1"));
        
            CLASS_LOGGER.debug("TimeStampTokenGenerator successfully instantiated");

        boolean includeFullChain=false;
        if(System.getenv(INCLUDE_FULL_CHAIN)!=null)
            includeFullChain=java.lang.Boolean.valueOf(System.getenv(INCLUDE_FULL_CHAIN));

        if(includeFullChain==false)
        {
            rre.certs.clear();
            rre.certs.add(rre.rsaSigningCert);
        }
        if(timeStampRequest.getCertReq())
        {
            tsTokenGen.addCertificates(new JcaCertStore(rre.certs));
             CLASS_LOGGER.debug("added certificates");
            boolean includeCRLs=false;
            if(System.getenv(INCLUDE_CRLS)!=null)
                includeCRLs=java.lang.Boolean.valueOf(System.getenv(INCLUDE_CRLS));

            if(includeCRLs)
            {
                java.util.List<X509CRL> crls = new java.util.LinkedList();
                for (X509Certificate cert : rre.certs)
                {
                    crls.addAll(de.elbosso.util.security.Utilities.getCRLs(cert));
                }
                JcaCRLStore store = new JcaCRLStore(crls);
                tsTokenGen.addCRLs(store);
                 CLASS_LOGGER.debug("added CRLs");
            }
            else
            {
                 CLASS_LOGGER.debug("CRLs not included (as requested)");
            }
        }
        else
        {
             CLASS_LOGGER.debug("No certificates and no CRLs added (as requested)");
        }
        GeneralName gn= new GeneralName(JcaX500NameUtil.getSubject(rre.rsaSigningCert));
        tsTokenGen.setTSA(gn);

        TimeStampResponseGenerator tsRespGen = new TimeStampResponseGenerator(tsTokenGen, TSPAlgorithms.ALLOWED);
        
            CLASS_LOGGER.debug("TimeStampResponseGenerator successfully instantiated");

        
            CLASS_LOGGER.debug("Message imprint: " + timeStampRequest.getMessageImprintAlgOID().getId() + " " + de.elbosso.util.Utilities.formatHexDump(timeStampRequest.getMessageImprintDigest(), true));
        
            CLASS_LOGGER.debug("Message imprint (Base64): " + timeStampRequest.getMessageImprintAlgOID().getId() + " " + de.elbosso.util.Utilities.base64Encode(timeStampRequest.getMessageImprintDigest()));

        byte[] tsr = tsRespGen.generateGrantedResponse(timeStampRequest, rfc3161Timestamp.getId(), rfc3161Timestamp.getCreationDate()).getEncoded();
        return tsr;
    }
    private TimeStampRequest createTimestampRequest(byte[] tsq,RequestReplyEntity rre) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException
    {
         CLASS_LOGGER.debug("timestamp query found");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        java.net.URL url = cryptoResourceManager.getTsaCert();
         CLASS_LOGGER.debug("Loading TSA cert from " + url);

        java.io.InputStream is = url.openStream();
        rre.rsaSigningCert = (X509Certificate) cf.generateCertificate(is);
        is.close();

        url = cryptoResourceManager.getChainPem();
         CLASS_LOGGER.debug("Loading chain from " + url);

        is = url.openStream();
        rre.certs=new java.util.LinkedList((java.util.Collection<X509Certificate>) cf.generateCertificates(is));
        rre.certs.add(rre.rsaSigningCert);
        is.close();

        url=cryptoResourceManager.getPrivateKey();
         CLASS_LOGGER.debug("Loading TSA private key from " + url);
        is = url.openStream();
        java.lang.String privateKeyPEM = de.elbosso.util.Utilities.readIntoString(is, StandardCharsets.UTF_8);
        is.close();

        // strip of header, footer, newlines, whitespaces
        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // decode to get the binary DER representation
        byte[] privateKeyDER = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        rre.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyDER));
         CLASS_LOGGER.debug("Decoding private key successfully finished");

        TimeStampRequest timeStampRequest = new TimeStampRequest(tsq);
        return timeStampRequest;
    }
}
