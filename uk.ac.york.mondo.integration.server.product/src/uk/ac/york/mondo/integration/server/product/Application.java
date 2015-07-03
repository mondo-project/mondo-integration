package uk.ac.york.mondo.integration.server.product;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Application implements IApplication {

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
