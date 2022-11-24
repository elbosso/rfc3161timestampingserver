package de.elbosso.tools.rfc3161timestampingserver;
import ch.qos.logback.classic.Level;
import de.elbosso.tools.rfc3161timestampingserver.dao.DaoFactory;
import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.domain.TotalNumber;
import de.elbosso.tools.rfc3161timestampingserver.impl.DefaultCryptoResourceManager;
import de.elbosso.tools.rfc3161timestampingserver.util.PersistenceManager;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.annotations.*;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.security.Security;
import java.time.Duration;

public class App {
	private final static org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(App.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
	private static final int DEFAULT_PORT = 7000;

	public static void main(String[] args)
	{
		de.elbosso.util.Utilities.configureBasicStdoutLogging(Level.ALL);
		InfluxConfig config = new InfluxConfig() {
			java.util.Properties props;

			@Override
			public Duration step() {
				return Duration.ofSeconds(10);
			}

			@Override
			public String db() {
				return "monitoring";
			}

			@Override
			public String get(String k) {
				if(props==null)
				{
					props = new java.util.Properties();
					java.net.URL url=de.netsysit.util.ResourceLoader.getResource("influxdb_micrometer.properties");
					if(url==null)
						CLASS_LOGGER.error("could not load default influxdb monitoring properties!");
					else
					{
						try
						{
							java.io.InputStream is = url.openStream();
							props.load(is);
							is.close();
						}
						catch(java.io.IOException exp)
						{
							CLASS_LOGGER.error(exp.getMessage(),exp);
						}
					}
				}
				String rv=System.getenv(k)!=null?System.getenv(k):props.getProperty(k);
				CLASS_LOGGER.debug("getting value of "+k+": "+rv);
				return rv;
			}
		};
		MeterRegistry registry = new InfluxMeterRegistry(config, Clock.SYSTEM);
		Metrics.globalRegistry.add(registry);
		App.init(DEFAULT_PORT);
	}
	static final Javalin init(int port)
	{
		DaoFactory df=new DaoFactory();
		CLASS_LOGGER.debug("adding BouncyCastle crypto provider");
		Security.addProvider(new BouncyCastleProvider());
		Javalin app = Javalin.create(config ->
						config
//						.registerPlugin(new RouteOverviewPlugin("/"))
								.registerPlugin(new OpenApiPlugin(getOpenApiOptions()))
								.enableWebjars()
								.addStaticFiles("/site")
		).
				start(port);
		CLASS_LOGGER.debug("started app - listening on port 7000");
		CLASS_LOGGER.debug("added path for static contents: /site (allowed methods: GET)");
		Handlers handlers=new Handlers(df,new DefaultCryptoResourceManager());
		app.get("/chain.pem", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Get Chain",
					method = HttpMethod.GET,
					deprecated = false,
					//tags = {"user"},
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = java.lang.String.class, type="application/pkcs7-mime")),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				handlers.handleGetChain(context);
			}
		});
		CLASS_LOGGER.debug("added path for cert chain: /chain.pem (allowed methods: GET)");
		app.get("/tsa.crt", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Get Signer Certificate",
					operationId = "getAllUsers",
					method = HttpMethod.GET,
					deprecated = false,
					//tags = {"user"},
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = java.lang.String.class,type = "application/pkix-cert")),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				handlers.handleGetSignerCert(context);
			}
		});
		CLASS_LOGGER.debug("added path for cert: /tsa.cert (allowed methods: GET)");
		app.get("/tsa.conf", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Get TSA Configuration",
					method = HttpMethod.GET,
					deprecated = false,
					//tags = {"user"},
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = java.lang.String.class)),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				handlers.handleGetTsaConf(context);
			}
		});
		CLASS_LOGGER.debug("added path for tsa configuration: /tsa.conf (allowed methods: GET)");
		app.post("/query", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Query Timestamps",
					deprecated = false,
					formParams = {
							@OpenApiFormParam(name = "algoid", type = String.class,required = false),
							@OpenApiFormParam(name = "msgDigestBase64", type = String.class,required = false),
							@OpenApiFormParam(name = "msgDigestHex", type = String.class,required = false),
					},
					//tags = {"user"},
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = byte[].class, type="application/timestamp-reply")),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				handlers.handlePostQuery(context);
			}
		});
		app.post("/", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Create Timestamps",
					deprecated = false,
					fileUploads = {
						@OpenApiFileUpload(name = "tsq",required = false)
					},
//					requestBody = @OpenApiRequestBody(required = false,content =  @OpenApiContent(from = byte[].class)),
					//tags = {"user"},
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = byte[].class, type="application/timestamp-reply")),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				handlers.handlePost(context);
			}
		});
		CLASS_LOGGER.debug("added path for requesting timestamps: / (allowed methods: POST)");
/*		AdminHandlers adminHandlers=new AdminHandlers(df,new DefaultCryptoResourceManager());
		app.post("/admin/totalNumber", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Assess Total Number of Timestamps in Database",
					deprecated = false,
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = TotalNumber.class)),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				adminHandlers.handlePostTotalNumber(context);
			}
		});
		app.post("/admin/youngest", new Handler()
		{
			@Override
			@OpenApi(
					summary = "Assess Total Number of Timestamps in Database",
					deprecated = false,
					responses = {
							@OpenApiResponse(status = "200", content = @OpenApiContent(from = Rfc3161timestamp.class)),
							@OpenApiResponse(status = "204") // No content
					}
			)
			public void handle(@NotNull Context context) throws Exception
			{
				adminHandlers.handlePostYoungest(context);
			}
		});
*/
		app.before(ctx -> {
			CLASS_LOGGER.debug(ctx.req.getMethod()+" "+ctx.contentType());
		});
		CLASS_LOGGER.debug("added before interceptor");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			app.stop();
		}));
		CLASS_LOGGER.debug("added callback for stopping the application");
		app.events(event -> {
			event.serverStopping(() -> { /* Your code here */ });
			event.serverStopped(() -> {
				CLASS_LOGGER.debug("Server stopped");
				PersistenceManager.getSharedInstance().close();
				CLASS_LOGGER.debug("Persistence manager closed");
			});
			CLASS_LOGGER.debug("added listener for server stopped event");
		});
		return app;
	}
	private static OpenApiOptions getOpenApiOptions()
	{
		Info applicationInfo = new Info()
			.version("1.6.0-SNAPSHOT")
			.description("de.elbosso.tools.rfc3161timestampingserver");
		return new OpenApiOptions(applicationInfo)
				.path("/open-api-spec")
				.swagger(new SwaggerOptions("/try-it").title("de.elbosso.tools.rfc3161timestampingserver - try it!"))
//				.reDoc(new ReDocOptions("/redoc").title("My ReDoc Documentation"))
		;
	}
}
