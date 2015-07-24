package uk.ac.york.mondo.integration.server.gzip;

import java.util.Dictionary;

import org.eclipse.equinox.http.jetty.JettyCustomizer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.GzipHandler;

public class Customizer extends JettyCustomizer {

	@Override
	public Object customizeContext(Object context,
			Dictionary<String, ?> settings) {
		if (context instanceof ContextHandler) {
			final GzipHandler gzipHandler = new GzipHandler();
			final ContextHandler contextHandler = (ContextHandler)context;
			contextHandler.setHandler(gzipHandler);
		}
		return super.customizeContext(context, settings);
	}

	
}
