/*******************************************************************************
 * Copyright (c) 2015 University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antonio Garcia-Dominguez - initial API and implementation
 *******************************************************************************/
package uk.ac.york.mondo.integration.hawk.remote.thrift;

import java.io.File;
import java.util.List;

import org.hawk.core.IAbstractConsole;
import org.hawk.core.IHawk;
import org.hawk.core.IHawkFactory;

import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.utils.APIUtils;

public class ThriftRemoteHawkFactory implements IHawkFactory {

	@Override
	public IHawk create(String name, File parentFolder, String location, IAbstractConsole console) throws Exception {
		return new ThriftRemoteHawk(name, location, parentFolder, console);
	}

	@Override
	public boolean instancesAreExtensible() {
		return false;
	}

	@Override
	public boolean instancesCreateGraph() {
		return false;
	}

	@Override
	public boolean instancesUseLocation() {
		return true;
	}

	@Override
	public InstanceInfo[] listInstances(String location) throws Exception {
		Hawk.Client client = APIUtils.connectToHawk(location);

		final List<HawkInstance> instances = client.listInstances();
		final InstanceInfo[] infos = new InstanceInfo[instances.size()];
		for (int iInfo = 0; iInfo < instances.size(); ++iInfo) {
			HawkInstance instance = instances.get(iInfo);
			infos[iInfo] = new InstanceInfo(instance.name, null, instance.running);
		}

		return infos;
	}
}
