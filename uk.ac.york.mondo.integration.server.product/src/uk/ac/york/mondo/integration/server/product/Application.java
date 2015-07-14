package uk.ac.york.mondo.integration.server.product;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application implements IApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	@Override
	public Object start(IApplicationContext context) throws Exception {
		// We don't really do anything at the moment for the application:
		// we just want a working Equinox instance for now		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do!
	}

}
