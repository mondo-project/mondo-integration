package uk.ac.york.mondo.integration.server.users.servlet;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import uk.ac.york.mondo.integration.server.users.servlet.db.UserStorage;

public class UsersPlugin implements BundleActivator {

	private static BundleContext context;
	private static UsersPlugin instance;

	static BundleContext getContext() {
		return context;
	}

	private UserStorage storage;

	public void start(BundleContext bundleContext) throws Exception {
		UsersPlugin.context = bundleContext;
		UsersPlugin.instance = this;
		File dataFile = FrameworkUtil.getBundle(UsersPlugin.class).getDataFile("users.db");
		storage = new UserStorage(dataFile);

	}

	public void stop(BundleContext bundleContext) throws Exception {
		UsersPlugin.context = null;
		UsersPlugin.instance = null;
		storage.close();
	}

	public static UsersPlugin getInstance() {
		return instance;
	}

	public UserStorage getStorage() {
		return storage;
	}
}
