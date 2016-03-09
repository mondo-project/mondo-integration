/*******************************************************************************
 * Copyright (c) 2016 University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antonio Garcia-Dominguez - initial API and implementation
 *******************************************************************************/
package uk.ac.york.mondo.integration.hawk.emf.emfsplitter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Plugin activator.
 */
public class HawkCrossReferencesPlugin extends Plugin {

	private static HawkCrossReferencesPlugin INSTANCE;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		INSTANCE = null;
	}

	public static HawkCrossReferencesPlugin getDefault() {
		return INSTANCE;
	}

	public void logError(Throwable t) {
		getLog().log(new Status(IStatus.ERROR, getBundle().getSymbolicName(), t.getMessage(), t));
	}
}
