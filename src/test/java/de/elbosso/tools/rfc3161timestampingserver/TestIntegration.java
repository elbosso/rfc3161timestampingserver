package de.elbosso.tools.rfc3161timestampingserver;

import de.elbosso.util.Utilities;
import io.javalin.Javalin;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.*;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

//see: https://www.baeldung.com/integration-testing-a-rest-api

public class TestIntegration
{
    private final static org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(TestIntegration.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final static int TEST_PORT=13456;
    private static final BouncyCastleProvider BC = new BouncyCastleProvider();

    static Javalin javalin;
    @BeforeAll
    static void setup()
    {
        Security.addProvider(BC);
        javalin=App.init(TEST_PORT);
    }
    @AfterAll
    static void tearDown()
    {
        javalin.stop();
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = "PERSISTENCE_UNIT_NAME_FOR_TESTS")
    public void test_Success() throws Exception
    {
        HttpUriRequest request=new HttpGet( "http://localhost:"+TEST_PORT+"/hguhu");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

        // Then
        Assertions.assertEquals(HttpStatus.SC_NOT_FOUND,httpResponse.getCode());
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPart() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        entity = ((CloseableHttpResponse) httpResponse).getEntity();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        java.io.InputStream responseStream = entity.getContent();
        TimeStampResponse response = new TimeStampResponse(responseStream);
        responseStream.close();
        validate(request,response);
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessBody() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:" + TEST_PORT + "/");
        java.net.URL url = TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        post.setHeader("Content-type", "application/timestamp-query");

        post.setEntity(EntityBuilder.create().setBinary(baos.toByteArray()).build());


        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(post);
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED, httpResponse.getCode());

        HttpEntity entity = ((CloseableHttpResponse) httpResponse).getEntity();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        java.io.InputStream responseStream = entity.getContent();
        TimeStampResponse response = new TimeStampResponse(responseStream);
        responseStream.close();
        validate(request,response);
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPartNoCertificate() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example_nocert.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        entity = ((CloseableHttpResponse) httpResponse).getEntity();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        java.io.InputStream responseStream = entity.getContent();
        TimeStampResponse response = new TimeStampResponse(responseStream);
        responseStream.close();
        validate(request,response);
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessBodyNoCertificate() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:" + TEST_PORT + "/");
        java.net.URL url = TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        post.setHeader("Content-type", "application/timestamp-query");

        post.setEntity(EntityBuilder.create().setBinary(baos.toByteArray()).build());


        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(post);
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED, httpResponse.getCode());

        HttpEntity entity = ((CloseableHttpResponse) httpResponse).getEntity();
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        java.io.InputStream responseStream = entity.getContent();
        TimeStampResponse response = new TimeStampResponse(responseStream);
        responseStream.close();
        validate(request,response);
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPartQuery_msgDigestBase64() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        post = new HttpPost("http://localhost:"+TEST_PORT+"/query");
        builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addTextBody("msgDigestBase64", new String(Base64.getEncoder().encode(request.getMessageImprintDigest())));

        entity = builder.build();
        post.setEntity(entity);

        httpResponse = HttpClientBuilder.create().build().execute( post );

        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        entity = ((CloseableHttpResponse) httpResponse).getEntity();

        java.io.InputStream responseStream = entity.getContent();
        TimeStampResponse response = new TimeStampResponse(responseStream);
        responseStream.close();
        validate(request,response);
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPartQuery_msgDigestBase64_NotFound() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        post = new HttpPost("http://localhost:"+TEST_PORT+"/query");
        builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addTextBody("msgDigestBase64", "some_unknown_digest_value");

        entity = builder.build();
        post.setEntity(entity);

        httpResponse = HttpClientBuilder.create().build().execute( post );

        // Then
        Assertions.assertEquals(HttpStatus.SC_NOT_FOUND,httpResponse.getCode());

    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPartQuery_msgDigestHex() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        post = new HttpPost("http://localhost:"+TEST_PORT+"/query");
        builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addTextBody("msgDigestHex", de.elbosso.util.Utilities.formatHexDump(request.getMessageImprintDigest(),false));

        entity = builder.build();
        post.setEntity(entity);

        httpResponse = HttpClientBuilder.create().build().execute( post );

        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        entity = ((CloseableHttpResponse) httpResponse).getEntity();

        java.io.InputStream responseStream = entity.getContent();
        TimeStampResponse response = new TimeStampResponse(responseStream);
        responseStream.close();
        validate(request,response);
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPartQuery_msgDigestHex_NotFound() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(), baos, true);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        TimeStampRequest request = new TimeStampRequest(bais);
        bais.close();

        post = new HttpPost("http://localhost:"+TEST_PORT+"/query");
        builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addTextBody("msgDigestHex", de.elbosso.util.Utilities.formatHexDump("some_unknown_digest_value".getBytes(),false));

        entity = builder.build();
        post.setEntity(entity);

        httpResponse = HttpClientBuilder.create().build().execute( post );

        // Then
        Assertions.assertEquals(HttpStatus.SC_NOT_FOUND,httpResponse.getCode());

    }

    private void validate(TimeStampRequest request, TimeStampResponse response) throws TSPException, CertificateException, OperatorCreationException, IOException, CRLException
    {
        response.validate(request);

        TimeStampToken timeStampToken = response.getTimeStampToken();

        // Get the tsa-certificate from the response
        SignerId signerID = timeStampToken.getSID();
        Store allCertificates = timeStampToken.getCertificates();

        Collection signerCertificates = allCertificates.getMatches(signerID);

        if(request.getCertReq()==true)
            Assertions.assertFalse(signerCertificates.isEmpty());
        if(request.getCertReq()==false)
            Assertions.assertTrue(signerCertificates.isEmpty());

        X509Certificate tsaCert=null;

        if(request.getCertReq()==true)
        {
            X509CertificateHolder certHolder = null;
            for (Object match : signerCertificates)
            {
                certHolder = (X509CertificateHolder) match;
                break;
            }
            Assertions.assertNotNull(certHolder);
            tsaCert = new JcaX509CertificateConverter()
                    .setProvider(BC).getCertificate(certHolder);
            SignerInformationVerifier siv = new JcaSimpleSignerInfoVerifierBuilder()
                    .setProvider(BC).build(tsaCert);

            timeStampToken.validate(siv);

        }


        Assertions.assertTrue(System.currentTimeMillis()-10000l<timeStampToken.getTimeStampInfo().getGenTime().getTime());

        InputStream chainStream = TestIntegration.class.getClassLoader().getResource("rfc3161timestampingserver/priv/chain.pem").openStream();
        InputStream rootStream = TestIntegration.class.getClassLoader().getResource("crypto/root.pem").openStream();


        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Collection<X509Certificate> chainCerts
                = (Collection<X509Certificate>) cf.generateCertificates(
                chainStream);
        List<String> crlUrls=new java.util.LinkedList();
        for(X509Certificate cert:chainCerts)
        {
            crlUrls.addAll(de.elbosso.util.security.Utilities.getCrlDistributionPoints(cert));
        }
        if(request.getCertReq()==true)
        {
            crlUrls.addAll(de.elbosso.util.security.Utilities.getCrlDistributionPoints(tsaCert));
        }
        Collection<X509CRL> crls=new java.util.LinkedList();
        for (String surl : crlUrls)
        {
            Utilities.sopln(surl);
            java.net.URL url=new java.net.URL(surl);
            try
            {
                java.io.InputStream is = url.openStream();
                crls.add((X509CRL) cf.generateCRL(is));
                is.close();
            }
            catch(java.io.FileNotFoundException exp)
            {
                Utilities.sopln(exp.getMessage());
            }
        }
        Collection<X509Certificate> trustedRoots
                = (Collection<X509Certificate>) cf.generateCertificates(
                rootStream);
        //Collection<X509CRL> crls
        //        = java.util.Collections.emptyList();//(Collection<X509CRL>) cf.generateCRLs(crlStream);

        for (X509CRL crl : crls) {
            CLASS_LOGGER.debug("CRL from issuer " + crl.getIssuerDN()
                    + " thisUpdate: " + crl.getThisUpdate());
        }
        if(request.getCertReq()==true)
        {
            CLASS_LOGGER.info(java.util.Objects.toString(tsaCert));
            CLASS_LOGGER.info(java.util.Objects.toString(trustedRoots));
            CLASS_LOGGER.info(java.util.Objects.toString(chainCerts));
            try
            {
                PKIXCertPathBuilderResult result
                        = buildPath(tsaCert, trustedRoots, chainCerts, crls);

                String trustAnchor = result.getTrustAnchor().getTrustedCert()
                        .getSubjectDN().getName();
            } catch (java.lang.Throwable t)
            {
                CLASS_LOGGER.warn(t.getMessage(), t);
            }
        }
    }
    /**
     * Build a validation path for a given certificate.
     *
     * @param cert the certificate
     * @param rootCerts trusted root certificates
     * @param intermediateCerts intermediate certificates to build the path from
     * @param crls all crls needed for revocation checking
     * @return successful result containing the path
     * @throws GeneralSecurityException
     */
    private static PKIXCertPathBuilderResult buildPath(X509Certificate cert,
                                                       Collection<X509Certificate> rootCerts,
                                                       Collection<X509Certificate> intermediateCerts,
                                                       Collection<X509CRL> crls)
            throws GeneralSecurityException {

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);

        HashSet<TrustAnchor> rootSet = new HashSet();
        if(rootCerts!=null)
        {
            for (X509Certificate rootCert : rootCerts)
            {
                rootSet.add(new TrustAnchor(rootCert, null));
            }
        }
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "SUN");
        for(TrustAnchor trustAnchor:rootSet)
        {
            Utilities.sopln("trust anchor: "+trustAnchor.getCA()+" "+trustAnchor.getTrustedCert().getSubjectDN());
        }
        PKIXBuilderParameters buildParams;
        buildParams = new PKIXBuilderParameters(rootSet, selector);

        CertStore intermediateStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCerts));
        for(X509Certificate x509:intermediateCerts)
        {
            Utilities.sopln("intermediate: "+x509.getSubjectDN());
        }
        CertStore crlStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(crls));

        buildParams.addCertStore(intermediateStore);
        buildParams.addCertStore(crlStore);
        buildParams.setRevocationEnabled(false);

        return (PKIXCertPathBuilderResult) builder.build(buildParams);
    }
}
