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

import java.io.FileInputStream;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.THttpClient;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import uk.ac.york.mondo.integration.api.Credentials;
import uk.ac.york.mondo.integration.api.File;
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
		checkConnected();
		Collection<HawkInstance> instances = client.listInstances();
		if (instances.isEmpty()) {
			return "No instances exist";
		} else {
			StringBuffer sbuf = new StringBuffer();
			for (HawkInstance i : instances) {
				sbuf.append(String.format("%s (%s%s)\n", i.name,
					i.running ? "running" : "stopped",
					i.name.equals(currentInstance) ? ", selected": ""
				));
			}
			return sbuf.toString();
		}
	}

	public Object _hawkAddInstance(CommandInterpreter intp) throws Exception {
		checkConnected();
		final String name = requiredArgument(intp, "name");
		client.createInstance(name);
		return String.format("Created instance %s", name);
	}

	public Object _hawkRemoveInstance(CommandInterpreter intp) throws Exception {
		checkConnected();
		final String name = requiredArgument(intp, "name");
		client.removeInstance(name);
		return String.format("Removed instance %s", name);
	}

	public Object _hawkSelectInstance(CommandInterpreter intp) throws Exception {
		checkConnected();
		final String name = requiredArgument(intp, "name");
		findInstance(name);
		currentInstance = name;
		return String.format("Selected instance %s", name);
	}

	public Object _hawkStartInstance(CommandInterpreter intp) throws Exception {
		checkConnected();
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
		checkConnected();
		final String name = requiredArgument(intp, "name");
		final HawkInstance hi = findInstance(name);
		if (hi.running) {
			client.stopInstance(name);
			return String.format("Stopped instance %s", name);
		} else {
			return String.format("Instance %s was already stopped", name);
		}
	}

	/* METAMODEL MANAGEMENT */

	public Object _hawkRegisterMetamodel(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();

		List<File> mmFiles = new ArrayList<>();
		for (String path = intp.nextArgument(); path != null; path = intp.nextArgument()) {
			java.io.File rawFile = new java.io.File(path);
			try (FileInputStream fIS = new FileInputStream(rawFile)) {
				FileChannel chan = fIS.getChannel();

				/* Note: this cast limits us to 2GB files - this shouldn't
				 be a problem, but if it were we could use FileChannel#map
				 and call Hawk.Client#registerModels one file at a time. */ 
				ByteBuffer buf = ByteBuffer.allocate((int) chan.size());
				chan.read(buf);
				buf.flip();

				File mmFile = new File();
				mmFile.name = rawFile.getName();
				mmFile.contents = buf;
				mmFiles.add(mmFile);
			}
		}
		client.registerMetamodels(currentInstance, mmFiles);

		return String.format("Registered %d metamodel(s)", mmFiles.size());
	}

	public Object _hawkUnregisterMetamodel(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String mmURI = requiredArgument(intp, "uri");
		client.unregisterMetamodel(currentInstance, mmURI);
		return String.format("Unregistered metamodel %s", mmURI);
	}

	public Object _hawkListMetamodels(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		return StringUtils.join(client.listMetamodels(currentInstance), "\n");
	}

	/* REPOSITORY MANAGEMENT */

	public Object _hawkAddRepository(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String repoURL = requiredArgument(intp, "url");
		final String repoType = requiredArgument(intp, "type");

		final Credentials creds = new Credentials();
		creds.username = intp.nextArgument();
		creds.password = intp.nextArgument();

		// TODO tell Kostas that LocalFolder does not work if the path has a trailing separator
		client.addRepository(currentInstance, repoURL, repoType, creds);
		return String.format("Added repository of type '%s' at '%s'", repoType, repoURL);
	}

	public Object _hawkRemoveRepository(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String repoURL = requiredArgument(intp, "url");
		client.removeRepository(currentInstance, repoURL);
		return String.format("Removed repository '%s'", repoURL);
	}

	public Object _hawkListRepositories(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		return StringUtils.join(client.listRepositories(currentInstance), "\n");
	}

	public Object _hawkListRepositoryTypes(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		return StringUtils.join(client.listRepositoryTypes(), "\n");
	}

	/**
	 * Ensures that a connection has been established.
	 * @throws ConnectException No connection has been established yet.
	 * @see #_hawkConnect(CommandInterpreter)
	 * @see #_hawkDisconnect(CommandInterpreter)
	 */
	private void checkConnected() throws ConnectException {
		if (client == null) {
			throw new ConnectException("Please connect to a Thrift endpoint first!");
		}
	}

	private void checkInstanceSelected() throws ConnectException {
		checkConnected();
		if (currentInstance == null) {
			throw new IllegalArgumentException("No Hawk instance has been selected");
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
		sbuf.append("---HAWK (commands are case insensitive)---\n\t");
		sbuf.append("hawkHelp - lists all the available commands for Hawk\n");
		sbuf.append("--Connections--\n\t");
		sbuf.append("hawkConnect <url> - connects to a Thrift endpoint\n\t");
		sbuf.append("hawkDisconnect - disconnects from the current Thrift endpoint\n");
		sbuf.append("--Instances--\n\t");
		sbuf.append("hawkListInstances - lists the available Hawk instances\n\t");
		sbuf.append("hawkAddInstance <name> - adds an instance with the provided name\n\t");
		sbuf.append("hawkRemoveInstance <name> - removes an instance with the provided name, if it exists\n\t");
		sbuf.append("hawkSelectInstance <name> - selects the instance with the provided name\n\t");
		sbuf.append("hawkStartInstance <name> - starts the instance with the provided name\n\t");
		sbuf.append("hawkStopInstance <name> - stops the instance with the provided name\n");
		sbuf.append("--Metamodels--\n\t");
		sbuf.append("hawkRegisterMetamodel <files...> - registers one or more metamodels\n\t");
		sbuf.append("hawkUnregisterMetamodel <uri> - unregisters the metamodel with the specified URI\n\t");
		sbuf.append("hawkListMetamodels - lists all registered metamodels in this instance\n");
		sbuf.append("--Repositories--\n\t");
		sbuf.append("hawkAddRepository <url> <type> [user] [pwd] - adds a repository\n\t");
		sbuf.append("hawkRemoveRepository <url> - removes the repository with the specified URL\n\t");
		sbuf.append("hawkListRepositories - lists all registered metamodels in this instance\n\t");
		sbuf.append("hawkListRepositoryTypes - lists available repository types\n");
		return sbuf.toString();
	}

}
