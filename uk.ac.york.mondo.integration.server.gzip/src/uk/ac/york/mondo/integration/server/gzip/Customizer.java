package uk.ac.york.mondo.integration.server.gzip;

import java.util.Dictionary;

import org.eclipse.equinox.http.jetty.JettyCustomizer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.GzipHandler;

public class Customizer extends JettyCustomizer {

	@Override
	public Object customizeContext(Object context,
			Dictionary<String, ?> settings) {
		// TODO Auto-generated method stub
		GzipHandler handler = new GzipHandler();
		if (context instanceof ContextHandler) {
			final ContextHandler contextHandler = (ContextHandler)context;
			contextHandler.setHandler(handler);
		}
		return super.customizeContext(context, settings);
	}

	
}
