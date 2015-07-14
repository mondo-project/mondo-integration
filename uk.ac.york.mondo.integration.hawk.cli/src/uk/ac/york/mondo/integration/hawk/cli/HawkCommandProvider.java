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
import java.util.NoSuchElementException;

import org.apache.thrift.TException;
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

	public Object _hawkHelp(CommandInterpreter intp) {
		return getHelp();
	}

	/* CONNECTION HANDLING */

	public Object _hawkConnect(CommandInterpreter intp) throws Exception {
		final String url = requiredArgument(intp, "url");

		final THttpClient transport = new THttpClient(url);
		client = new Hawk.Client(new TCompactProtocol(transport));
		currentInstance = null;

		transport.open();
		if (transport.isOpen()) {
			return String.format("Connected to URL %s", url);
		}
		return null;
	}

	public Object _hawkDisconnect(CommandInterpreter intp) throws Exception {
		if (client != null) {
			client.getInputProtocol().getTransport().close();
			client = null;
			currentInstance = null;
			return String.format("Connection closed");
		}
		else {
			return "Connection already closed";
		}
	}

	/* INSTANCE HANDLING */

	public Object _hawkListInstances(CommandInterpreter intp) throws Exception {
		checkConnection();
		Collection<HawkInstance> instances = client.listInstances();
		if (instances.isEmpty()) {
			return "No instances exist";
		} else {
			StringBuffer sbuf = new StringBuffer();
			for (HawkInstance i : instances) {
				sbuf.append(String.format("%s (%s)\n", i.name, i.running ? "running" : "stopped"));
			}
			return sbuf.toString();
		}
	}

	public Object _hawkAddInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		client.createInstance(name);
		return String.format("Created instance %s", name);
	}

	public Object _hawkRemoveInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		client.removeInstance(name);
		return String.format("Removed instance %s", name);
	}

	public Object _hawkSelectInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		findInstance(name);
		currentInstance = name;
		return String.format("Selected instance %s", name);
	}

	public Object _hawkStartInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		final HawkInstance hi = findInstance(name);
		if (!hi.running) {
			client.startInstance(name);
			return String.format("Started instance %s", name);
		} else {
			return String.format("Instance %s was already running", name);
		}
	}

	public Object _hawkStopInstance(CommandInterpreter intp) throws Exception {
		checkConnection();
		final String name = requiredArgument(intp, "name");
		final HawkInstance hi = findInstance(name);
		if (hi.running) {
			client.stopInstance(name);
			return String.format("Stopped instance %s", name);
		} else {
			return String.format("Instance %s was already stopped", name);
		}
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
	 * Queries the Thrift endpoint about the specified instance.
	 *
	 * @throws NoSuchElementException
	 *             No instance exists with that name.
	 * @throws TException
	 *             Server or communication error with the Thrift endpoint.
	 */
	private HawkInstance findInstance(final String name) throws TException {
		for (HawkInstance i : client.listInstances()) {
			if (i.name.equals(name)) {
				return i;
			}
		}
		throw new NoSuchElementException(String.format("No instance exists with the name '%s'", name));
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
