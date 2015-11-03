package uk.ac.york.mondo.integration.server.users.servlet;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import uk.ac.york.mondo.integration.server.users.servlet.db.UserStorage;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Activator instance;

	static BundleContext getContext() {
		return context;
	}

	private UserStorage storage;

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.instance = this;
		File dataFile = FrameworkUtil.getBundle(Activator.class).getDataFile("users.db");
		storage = new UserStorage(dataFile);

	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		Activator.instance = null;
		storage.close();
	}

	public static Activator getInstance() {
		return instance;
	}

	public UserStorage getStorage() {
		return storage;
	}
}
