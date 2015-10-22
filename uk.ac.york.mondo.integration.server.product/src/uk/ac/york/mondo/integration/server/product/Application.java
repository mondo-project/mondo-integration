package uk.ac.york.mondo.integration.server.product;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		// We need to test the secure store, so the user will get a warning
		// if they haven't set up a proper password.
		ISecurePreferences factory = SecurePreferencesFactory.getDefault();
		factory.node("mondo.test").put("testvalue", "1", true);
		factory.flush();

		// We don't really do anything at the moment for the application:
		// we just want a working Equinox instance for now		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do!
	}

}
