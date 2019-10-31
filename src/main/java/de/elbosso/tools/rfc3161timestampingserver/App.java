package de.elbosso.tools.rfc3161timestampingserver;
import io.javalin.Javalin;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

public class App {
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		Javalin app = Javalin.create().start(7000);
		app.get("/", ctx -> {
			System.out.println("get");
			ctx.result("Hello World");
		});
		app.post("/", ctx -> {
			byte[] tsq=null;
			if(ctx.contentType().equals("application/timestamp-query"))
			{
				//curl -H "Content-Type: application/timestamp-query" --data-binary '@../../work/expect-dialog-ca.git/_priv/create_ca.sh.tsq' http://localhost:7000/
				System.out.println("body " + ctx.bodyAsBytes().length);
				tsq=ctx.bodyAsBytes();
			}
			else if(ctx.contentType().startsWith("multipart/form-data"))
			{
				//curl -F "tsq=@../../work/expect-dialog-ca.git/_priv/create_ca.sh.tsq" http://localhost:7000/
				if(ctx.uploadedFile("tsq")!=null)
				{
					System.out.println("tsq "+ctx.uploadedFile("tsq").getContentLength());
					java.io.InputStream is=ctx.uploadedFile("tsq").getContent();
					java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
					de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
					tsq=baos.toByteArray();
				}
				//curl -F "file=@../../work/expect-dialog-ca.git/_priv/create_ca.sh.tsq" http://localhost:7000/
				else
				{
				}
/*				ctx.uploadedFiles("files").forEach(file -> {
					System.out.println(file.getContentLength());
				});
*/			}
			if(tsq!=null)
			{
				CertificateFactory cf = CertificateFactory.getInstance("X.509");

				java.net.URL url=de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt");
				java.io.InputStream is=url.openStream();
				X509Certificate rsaSigningCert=(X509Certificate)cf.generateCertificate(is);
				is.close();

				url=de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key");
				is=url.openStream();
				java.lang.String privateKeyPEM=de.elbosso.util.Utilities.readIntoString(is,StandardCharsets.UTF_8);
				is.close();

				// strip of header, footer, newlines, whitespaces
				privateKeyPEM = privateKeyPEM
						.replace("-----BEGIN PRIVATE KEY-----", "")
						.replace("-----END PRIVATE KEY-----", "")
						.replaceAll("\\s", "");

				// decode to get the binary DER representation
				byte[] privateKeyDER = Base64.getDecoder().decode(privateKeyPEM);

				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyDER));

				AlgorithmIdentifier digestAlgorithm = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384);
				DigestCalculatorProvider digProvider = new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
				TimeStampTokenGenerator tsTokenGen = new TimeStampTokenGenerator(
						new JcaSimpleSignerInfoGeneratorBuilder().build("SHA384withRSA", privateKey, rsaSigningCert),
						digProvider.get(digestAlgorithm),
						new ASN1ObjectIdentifier("1.2"));

				tsTokenGen.addCertificates(new JcaCertStore(Collections.singleton(rsaSigningCert)));

				TimeStampResponseGenerator tsRespGen = new TimeStampResponseGenerator(tsTokenGen, TSPAlgorithms.ALLOWED);

				byte[] tsr=tsRespGen.generate(new TimeStampRequest(tsq), new BigInteger("23"), new Date()).getEncoded();

				System.out.println("tsr length "+tsr.length);

				ctx.status(201);
				ctx.contentType("application/timestamp-reply");
				ctx.result(new java.io.ByteArrayInputStream(tsr));
			}
			else
			{
				ctx.status(500);
			}
		});
		app.before(ctx -> {
			System.out.println("before");
			System.out.println(ctx.contentType());
			System.out.println(ctx.req.getMethod());
		});
	}
	byte[] createTspResponse(PrivateKey tspSigningKey, X509Certificate tspSigningCert, byte[] encRequest)
			throws TSPException, OperatorCreationException, GeneralSecurityException, IOException
	{
		AlgorithmIdentifier digestAlgorithm = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384);
		DigestCalculatorProvider digProvider = new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
		TimeStampTokenGenerator tsTokenGen = new TimeStampTokenGenerator(
				new JcaSimpleSignerInfoGeneratorBuilder().build("SHA384withRSA", tspSigningKey, tspSigningCert),
				digProvider.get(digestAlgorithm),
				new ASN1ObjectIdentifier("1.2"));

		tsTokenGen.addCertificates(new JcaCertStore(Collections.singleton(tspSigningCert)));

		TimeStampResponseGenerator tsRespGen = new TimeStampResponseGenerator(tsTokenGen, TSPAlgorithms.ALLOWED);

		return tsRespGen.generate(new TimeStampRequest(encRequest), new BigInteger("23"), new Date()).getEncoded();
	}

}
