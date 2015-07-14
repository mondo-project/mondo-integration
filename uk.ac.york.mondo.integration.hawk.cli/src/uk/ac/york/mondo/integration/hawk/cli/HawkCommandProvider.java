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
package uk.ac.york.mondo.integration.hawk.cli;

import java.net.ConnectException;
import java.util.Collection;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.THttpClient;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkInstance;

/**
 * Simple command-line based client for a remote Hawk instance.
 */
public class HawkCommandProvider implements CommandProvider {

	private Hawk.Client client;
	private String currentInstance;

	/* CONNECTION HANDLING */

	public Object _hawkConnect(CommandInterpreter intp) throws Exception {
		final String url = requiredArgument(intp, "url");

		final THttpClient transport = new THttpClient(url);
		client = new Hawk.Client(new TCompactProtocol(transport));
		transport.open();
		if (transport.isOpen()) {
			System.out.println(String.format("Connected to URL %s", url));
		}

		return null;
	}

	public Object _hawkDisconnect(CommandInterpreter intp) throws Exception {
		if (client != null) {
			client.getInputProtocol().getTransport().close();
			client = null;
			currentInstance = null;
		}
		return null;
	}

	/* INSTANCE HANDLING */

	public Object _hawkListInstances(CommandInterpreter intp) throws Exception {
		checkConnection();
		Collection<HawkInstance> instances = client.listInstances();
		if (instances.isEmpty()) {
			System.out.println("No instances exist");
		} else {
			for (HawkInstance i : instances) {
				System.out.println(String.format("%s (%s)", i.name, i.running ? "running" : "stopped"));
			}
		}
		return null;
	}

	public Object _hawkAddInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		client.createInstance(name);
		return null;
	}

	public Object _hawkRemoveInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		client.removeInstance(name);
		return null;
	}

	public Object _hawkSelectInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		for (HawkInstance i : client.listInstances()) {
			if (i.name.equals(name)) {
				currentInstance = name;
				return null;
			}
		}
		throw new IllegalArgumentException(String.format("No instance exists with the name '%s'", name));
	}

	/**
	 * Ensures that a connection has been established.
	 * @throws ConnectException No connection has been established yet.
	 * @see #_hawkConnect(CommandInterpreter)
	 * @see #_hawkDisconnect(CommandInterpreter)
	 */
	private void checkConnection() throws ConnectException {
		if (client == null) {
			throw new ConnectException("Please connect to a Thrift endpoint first!");
		}
	}

	/**
	 * Reads an expected argument from the interpreter.
	 * @throws IllegalArgumentException The argument has not been provided.
	 */
	private String requiredArgument(CommandInterpreter intp, String argumentName) {
		String value = intp.nextArgument();
		if (value == null) {
			throw new IllegalArgumentException(
				String.format("Required argument '%s' has not been provided", argumentName));
		}
		return value;
	}

	@Override
	public String getHelp() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("---HAWK---\n\n");
		sbuf.append("--Connections--\n\t");
		sbuf.append("hawkConnect <url> - connects to a Thrift endpoint\n\t");
		sbuf.append("hawkDisconnect - disconnects from the current Thrift endpoint\n");
		sbuf.append("--Instances--\n\t");
		sbuf.append("hawkListInstances - lists the available Hawk instances\n\t");
		sbuf.append("hawkAddInstance <name> - adds an instance with the provided name\n\t");
		sbuf.append("hawkRemoveInstance <name> - removes an instance with the provided name, if it exists\n\t");
		sbuf.append("hawkSelectInstance <name> - selects the instance with the provided name\n\t");
		return sbuf.toString();
	}

}
