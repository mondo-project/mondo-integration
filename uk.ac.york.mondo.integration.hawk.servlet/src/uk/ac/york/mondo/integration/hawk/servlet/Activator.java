/*******************************************************************************
 * Copyright (c) 2015 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Antonio Garcia-Dominguez - initial API and implementation
 ******************************************************************************/
package uk.ac.york.mondo.integration.hawk.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.ServletException;

import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.hawk.core.IModelIndexer.ShutdownRequestType;
import org.hawk.osgiserver.HManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import uk.ac.york.mondo.integration.artemis.server.Server;

/**
 * Activator for the Hawk servlet plugin. The plugin starts an embedded Apache
 * Artemis messaging server (for notifications). It listens on
 * {@link TransportConstants#DEFAULT_HOST} and port
 * {@link TransportConstants#DEFAULT_PORT} by default, but these can be changed
 * by setting the {@link #ARTEMIS_HOST_PROPERTY} and/or
 * {@link #ARTEMIS_PORT_PROPERTY} system properties.
 */
public class Activator implements BundleActivator {

	private static final String ARTEMIS_PORT_PROPERTY = "hawk.artemis.port";
	private static final String ARTEMIS_HOST_PROPERTY = "hawk.artemis.host";

	private static BundleContext context;
	private static Activator instance;

	public static Activator getInstance() {
		return instance;
	}

	public static String getPluginId() {
		return context.getBundle().getSymbolicName();
	}

	private Server artemis;

	public Activator() {
		Activator.instance = this;
	}

	public File getDataFile(String filename) {
		return context.getDataFile(filename);
	}

	public File writeToDataFile(String filename, ByteBuffer contents)
			throws FileNotFoundException, IOException {
		// Store in the plugin's persistent store
		final java.io.File destFile = getDataFile(filename);

		// The FOS is closed while closing the channel, so we can suppress this
		// warning
		try (@SuppressWarnings("resource")
		FileChannel fc = new FileOutputStream(destFile).getChannel()) {
			fc.write(contents);
		}

		return destFile;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		HManager.getInstance();

		String artemisHost = System.getProperty(ARTEMIS_HOST_PROPERTY);
		if (artemisHost == null) {
			artemisHost = TransportConstants.DEFAULT_HOST;
		}

		String sArtemisPort = System.getProperty(ARTEMIS_PORT_PROPERTY);
		int artemisPort;
		if (sArtemisPort == null) {
			artemisPort = TransportConstants.DEFAULT_PORT;
		} else {
			artemisPort = Integer.valueOf(sArtemisPort);
		}

		artemis = new Server(artemisHost, artemisPort);
		try {
			artemis.start();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		HManager.getInstance().stopAllRunningInstances(
				ShutdownRequestType.ONLY_LOCAL);
		artemis.stop();
		artemis = null;
	}

	public Server getArtemisServer() {
		return artemis;
	}
}
