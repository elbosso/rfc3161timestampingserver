package de.elbosso.tools.rfc3161timestampingserver;
import ch.qos.logback.classic.Level;
import de.elbosso.tools.rfc3161timestampingserver.dao.DaoFactory;
import de.elbosso.tools.rfc3161timestampingserver.impl.DefaultCryptoResourceManager;
import de.elbosso.tools.rfc3161timestampingserver.util.PersistenceManager;
import io.javalin.Javalin;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.influx.InfluxConfig;
import io.micrometer.influx.InfluxMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
		Javalin app = Javalin.create().start(port);
		CLASS_LOGGER.debug("started app - listening on port 7000");
		app.config.addStaticFiles("/site");
		CLASS_LOGGER.debug("added path for static contents: /site (allowed methods: GET)");
		Handlers handlers=new Handlers(df,new DefaultCryptoResourceManager());
		app.get("/chain.pem", handlers::handleGetChain);
		CLASS_LOGGER.debug("added path for cert chain: /chain.pem (allowed methods: GET)");
		app.get("/tsa.crt", handlers::handleGetSignerCert);
		CLASS_LOGGER.debug("added path for cert: /tsa.cert (allowed methods: GET)");
		app.get("/tsa.conf", handlers::handleGetTsaConf);
		CLASS_LOGGER.debug("added path for tsa configuration: /tsa.conf (allowed methods: GET)");
		app.post("/query", handlers::handlePostQuery);
		app.post("/", handlers::handlePost);
		CLASS_LOGGER.debug("added path for requesting timestamps: / (allowed methods: POST)");
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
}
