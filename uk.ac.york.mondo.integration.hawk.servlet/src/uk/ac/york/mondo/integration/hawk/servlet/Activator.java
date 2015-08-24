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

import org.hawk.core.IModelIndexer.ShutdownRequestType;
import org.hawk.osgiserver.HManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Activator instance;

	public static Activator getInstance() {
		return instance;
	}

	public static String getPluginId() {
		return context.getBundle().getSymbolicName();
	}

	public Activator() {
		Activator.instance = this;
	}

	public File getDataFile(String filename) {
		return context.getDataFile(filename);
	}

	public File writeToDataFile(String filename, ByteBuffer contents) throws FileNotFoundException, IOException {
		// Store in the plugin's persistent store
		final java.io.File destFile = getDataFile(filename);

		// The FOS is closed while closing the channel, so we can suppress this warning
		try (@SuppressWarnings("resource") FileChannel fc = new FileOutputStream(destFile).getChannel()) {
			fc.write(contents);
		}

		return destFile;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		HManager.getInstance();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		HManager.getInstance().stopAllRunningInstances(ShutdownRequestType.ONLY_LOCAL);
	}
}
