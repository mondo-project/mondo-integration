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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.MessageHandler;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.AttributeSlot;
import uk.ac.york.mondo.integration.api.ContainerSlot;
import uk.ac.york.mondo.integration.api.Credentials;
import uk.ac.york.mondo.integration.api.DerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.File;
import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkChangeEvent;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.IndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.ReferenceSlot;
import uk.ac.york.mondo.integration.api.Repository;
import uk.ac.york.mondo.integration.api.Subscription;
import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.utils.APIUtils;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.api.utils.ActiveMQBufferTransport;
import uk.ac.york.mondo.integration.artemis.consumer.Consumer;

/**
 * Simple command-line based client for a remote Hawk instance, using the Thrift API.
 */
public class HawkCommandProvider implements CommandProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkCommandProvider.class);

	private Hawk.Client client;
	private ThriftProtocol clientProtocol;
	private String currentInstance;
	private Consumer consumer;

	public Object _hawkHelp(CommandInterpreter intp) {
		return getHelp();
	}

	/* CONNECTION HANDLING */

	public Object _hawkConnect(CommandInterpreter intp) throws Exception {
		final String url = requiredArgument(intp, "url");

		clientProtocol = ThriftProtocol.guessFromURL(url);
		if (client != null) {
			final TTransport transport = client.getInputProtocol().getTransport();
			Activator.getInstance().removeCloseable(transport);
			transport.close();
		}

		client = APIUtils.connectToHawk(url, clientProtocol);
		Activator.getInstance().addCloseable(client.getInputProtocol().getTransport());
		currentInstance = null;

		return "Connected to " + url;
	}

	public Object _hawkDisconnect(CommandInterpreter intp) throws Exception {
		if (client != null) {
			final TTransport transport = client.getInputProtocol().getTransport();
			Activator.getInstance().removeCloseable(transport);
			transport.close();

			client = null;
			currentInstance = null;
			return "Connection closed";
		}
		else {
			return "Connection not open";
		}
	}

	/* INSTANCE HANDLING */

	public Object _hawkListInstances(CommandInterpreter intp) throws Exception {
		checkConnected();
		List<HawkInstance> instances = client.listInstances();
		Collections.sort(instances, new Comparator<HawkInstance>() {
			@Override
			public int compare(HawkInstance o1, HawkInstance o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

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
		// TODO add extra parameter to pick the backend + listBackends operation
		checkConnected();
		final String name = requiredArgument(intp, "name");
		final String adminPassword = requiredArgument(intp, "adminPassword");
		client.createInstance(name, adminPassword);
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
		final String adminPassword = requiredArgument(intp, "adminPassword");
		final HawkInstance hi = findInstance(name);
		if (!hi.running) {
			client.startInstance(name, adminPassword);
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

	public Object _hawkSyncInstance(CommandInterpreter intp) throws Exception {
		checkConnected();
		final String name = requiredArgument(intp, "name");
		final HawkInstance hi = findInstance(name);
		if (hi.running) {
			client.syncInstance(name);
			return String.format("Requested immediate sync on instance %s", currentInstance);
		} else {
			return String.format("Instance %s is not running", name);
		}
	}

	/* METAMODEL MANAGEMENT */

	public Object _hawkRegisterMetamodel(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();

		List<File> mmFiles = new ArrayList<>();
		for (String path = intp.nextArgument(); path != null; path = intp.nextArgument()) {
			java.io.File rawFile = new java.io.File(path);
			mmFiles.add(APIUtils.convertJavaFileToThriftFile(rawFile));
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
		return formatList(client.listMetamodels(currentInstance));
	}

	/* REPOSITORY MANAGEMENT */

	public Object _hawkAddRepository(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String repoURL = requiredArgument(intp, "url");
		final String repoType = requiredArgument(intp, "type");

		final Credentials creds = new Credentials();
		creds.username = intp.nextArgument();
		creds.password = intp.nextArgument();
		if (creds.username == null) { creds.username = "anonymous"; }
		if (creds.password == null) { creds.password = "anonymous"; }

		// TODO tell Kostas that LocalFolder does not work if the path has a trailing separator
		client.addRepository(currentInstance, new Repository(repoURL, repoType), creds);
		return String.format("Added repository of type '%s' at '%s'", repoType, repoURL);
	}

	public Object _hawkRemoveRepository(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String repoURL = requiredArgument(intp, "url");
		client.removeRepository(currentInstance, repoURL);
		return String.format("Removed repository '%s'", repoURL);
	}

	public Object _hawkUpdateRepositoryCredentials(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String repoURL = requiredArgument(intp, "url");
		final String user = requiredArgument(intp, "user");
		final String pass = requiredArgument(intp, "pass");
		client.updateRepositoryCredentials(currentInstance, repoURL, new Credentials(user, pass));
		return String.format("Credentials changed for '%s'", repoURL);
	}

	public Object _hawkListRepositories(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		return formatList(client.listRepositories(currentInstance));
	}

	public Object _hawkListRepositoryTypes(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		return formatList(client.listRepositoryTypes());
	}

	public Object _hawkListFiles(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		// TODO allow for multiple repositories
		final String repo = requiredArgument(intp, "url");
		final List<String> filePatterns = readRemainingArguments(intp);
		if (filePatterns.isEmpty()) {
			filePatterns.add("*");
		}
		return formatList(client.listFiles(currentInstance, Arrays.asList(repo), filePatterns));
	}

	/* QUERIES */

	public Object _hawkListQueryLanguages(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		return formatList(client.listQueryLanguages(currentInstance));
	}

	public Object _hawkQuery(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String query = requiredArgument(intp, "query");
		final String language = requiredArgument(intp, "language");
		String repo = intp.nextArgument();
		if (repo == null) {
			repo = "*";
		}

		List<String> filePatterns = readRemainingArguments(intp);
		if (filePatterns.isEmpty()) {
			filePatterns.add("*");
		}

		// TODO extend hawkQuery command to provide flags
		Object ret = client.query(currentInstance, query, language, repo, filePatterns, true, true, true, false);
		// TODO do something better than toString here
		return "Result: " + ret;
	}

	public Object _hawkGetModel(CommandInterpreter intp) throws Exception {
		return listModelElements(intp, true);
	}

	public Object _hawkGetRoots(CommandInterpreter intp) throws Exception {
		return listModelElements(intp, false);
	}

	public Object _hawkResolveProxies(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();

		final List<String> ids = readRemainingArguments(intp);
		final List<ModelElement> elems = client.resolveProxies(currentInstance, ids, true, true);
		return formatModelElements(elems, "");
	}

	/* INDEXED ATTRIBUTES */

	public Object _hawkListIndexedAttributes(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		List<String> lines = new ArrayList<>();
		for (IndexedAttributeSpec spec : client.listIndexedAttributes(currentInstance)) {
			lines.add(String.format("metamodel '%s', type '%s', indexed attribute '%s'",
					spec.metamodelUri, spec.typeName, spec.attributeName));
		}
		return formatList(lines);
	}

	public Object _hawkAddIndexedAttribute(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String mmURI = requiredArgument(intp, "mmURI");
		final String typeName = requiredArgument(intp, "typeName");
		final String attributeName = requiredArgument(intp, "attributeName");
		client.addIndexedAttribute(currentInstance, new IndexedAttributeSpec(mmURI, typeName, attributeName));
		return String.format("Added indexed attribute '%s' to '%s' in '%s'", attributeName, typeName, mmURI);
	}

	public Object _hawkRemoveIndexedAttribute(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String mmURI = requiredArgument(intp, "mmURI");
		final String typeName = requiredArgument(intp, "typeName");
		final String attributeName = requiredArgument(intp, "attributeName");
		client.removeIndexedAttribute(currentInstance, new IndexedAttributeSpec(mmURI, typeName, attributeName));
		return String.format("Removed indexed attribute '%s' from '%s' in '%s'", attributeName, typeName, mmURI);
	}

	/* DERIVED ATTRIBUTES */

	public Object _hawkListDerivedAttributes(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		List<String> lines = new ArrayList<>();
		for (DerivedAttributeSpec spec : client.listDerivedAttributes(currentInstance)) {
			lines.add(String.format("metamodel '%s', type '%s', derived attribute '%s'",
					spec.metamodelUri, spec.typeName, spec.attributeName));
		}
		return formatList(lines);
	}

	public Object _hawkAddDerivedAttribute(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();

		DerivedAttributeSpec spec = new DerivedAttributeSpec();
		spec.metamodelUri = requiredArgument(intp, "mmURI");
		spec.typeName = requiredArgument(intp, "typeName");
		spec.attributeName = requiredArgument(intp, "attributeName");
		spec.attributeType = requiredArgument(intp, "attributeType");
		spec.derivationLanguage = requiredArgument(intp, "lang");
		spec.derivationLogic = requiredArgument(intp, "expr");

		String nextArg;
		while ((nextArg = intp.nextArgument()) != null) {
			switch (nextArg.toLowerCase()) {
			case "many": spec.isMany = true; break;
			case "ordered": spec.isOrdered = true; break;
			case "unique": spec.isUnique = true; break;
			}
		}

		client.addDerivedAttribute(currentInstance, spec);
		return String.format("Added derived attribute '%s' to '%s' in '%s'",
			spec.attributeName, spec.typeName, spec.metamodelUri);
	}

	public Object _hawkRemoveDerivedAttribute(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		final String mmURI = requiredArgument(intp, "mmURI");
		final String typeName = requiredArgument(intp, "typeName");
		final String attributeName = requiredArgument(intp, "attributeName");

		final DerivedAttributeSpec spec = new DerivedAttributeSpec();
		spec.metamodelUri = mmURI;
		spec.typeName = typeName;
		spec.attributeName = attributeName;
		client.removeDerivedAttribute(currentInstance, spec);
		return String.format("Removed derived attribute '%s' from '%s' in '%s'", attributeName, typeName, mmURI);
	}

	/* NOTIFICATIONS */

	public Object _hawkWatchModelChanges(CommandInterpreter intp) throws Exception {
		checkInstanceSelected();
		if (consumer != null) {
			consumer.closeSession();
			Activator.getInstance().removeCloseable(consumer);
		}

		SubscriptionDurability durability = SubscriptionDurability.DEFAULT;
		String sDurability = intp.nextArgument();
		if (sDurability != null) {
			durability = SubscriptionDurability.valueOf(sDurability.toUpperCase());
		}
		String clientId = intp.nextArgument();
		if (clientId == null) {
			clientId = System.getenv("user.name");
		}
		String repository = intp.nextArgument();
		if (repository == null) {
			repository = "*";
		}
		List<String> files = readRemainingArguments(intp);
		if (files.isEmpty()) {
			files.add("*");
		}

		Subscription subscription = client.watchModelChanges(currentInstance, repository, files, clientId, durability);
		consumer = APIUtils.connectToArtemis(subscription, durability);
		consumer.openSession();
		Activator.getInstance().addCloseable(consumer);
		final MessageHandler handler = new MessageHandler() {
			@Override
			public void onMessage(ClientMessage message) {
				try {
					final TProtocol proto = clientProtocol.getProtocolFactory().getProtocol(new ActiveMQBufferTransport(message.getBodyBuffer()));
					final HawkChangeEvent change = new HawkChangeEvent();
					try {
						change.read(proto);
						System.out.println("Received message from Artemis: " + change);
					} catch (TException e) {
						// TODO Auto-generated catch block
						System.err.println("Error while decoding incoming message");
						e.printStackTrace();
					}
					message.acknowledge();

					// Normally, Artemis waits until a minimum number of bytes is reached (even on auto-commit mode).
					// Clients should specify some additional strategy for committing acknowledgements.
					consumer.commitSession();
				} catch (ActiveMQException e) {
					LOGGER.error("Failed to ack message", e);
				}
			}
		};
		consumer.processChangesAsync(handler);
		return String.format(
				"Watching changes on queue '%s' at address '%s' of '%s:%s'",
				subscription.queueName, subscription.queueAddress,
				subscription.host, subscription.port);
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

	private Object formatList(final List<?> elements) {
		if (elements.isEmpty()) {
			return "(no results)";
		} else {
			StringBuffer sbuf = new StringBuffer();
			boolean bFirst = true;
			for (Object element : elements) {
				if (bFirst) {
					bFirst = false;
				} else {
					sbuf.append("\n");
				}
				sbuf.append("\t- ");
				sbuf.append(element);
			}
			return sbuf.toString();
		}
	}

	private Object formatModelElements(final List<ModelElement> elems, String indent) {
		final StringBuffer sbuf = new StringBuffer();
		boolean isFirst = true;
		for (ModelElement me : elems) {
			if (isFirst) {
				isFirst = false;
			} else {
				sbuf.append("\n");
			}
			sbuf.append(String.format("%sElement %s:\n\t", indent, me.id));
			sbuf.append(String.format("%sMetamodel: %s\n\t", indent, me.metamodelUri));
			sbuf.append(String.format("%sType: %s\n\t", indent, me.typeName));
			if (me.isSetAttributes()) {
				sbuf.append(indent + "Attributes:");
				for (AttributeSlot s : me.attributes) {
					sbuf.append(String
							.format("\n\t\t%s%s = %s", indent, s.name, s.value));
				}
			}
			if (me.isSetReferences()) {
				sbuf.append("\n\t" + indent + "References:");
				for (ReferenceSlot s : me.references) {
					sbuf.append(String.format("\n\t\t%s%s =", indent, s.name));
					if (s.isSetId()) { sbuf.append(String.format(" id(%s)", s.id)); }
					if (s.isSetIds()) { sbuf.append(String.format(" ids(%s)", s.ids)); }
					if (s.isSetPosition()) { sbuf.append(String.format(" position(%s)", s.position)); }
					if (s.isSetPositions()) { sbuf.append(String.format(" positions(%s)", s.positions)); }
				}
			}
			if (me.isSetContainers()) {
				sbuf.append("\n\t" + indent + "Contained elements:");
				for (ContainerSlot s : me.containers) {
					sbuf.append(String.format("\n\t\t%s%s = %s", indent, s.name, formatModelElements(s.elements, indent + "\t\t")));
				}
			}
		}
		return sbuf.toString();
	}

	private Object listModelElements(CommandInterpreter intp,
			final boolean entireModel) throws Exception {
		checkInstanceSelected();

		// TODO accept multiple repositories
		final String repo = requiredArgument(intp, "repo");
		final List<String> patterns = readRemainingArguments(intp);
		if (patterns.isEmpty()) {
			patterns.add("*");
		}

		List<ModelElement> elems;
		if (entireModel) {
			elems = client.getModel(currentInstance, Arrays.asList(repo), patterns, true, true, false);
		} else {
			elems = client.getRootElements(currentInstance, Arrays.asList(repo), patterns, true, true);
		}
		return formatModelElements(elems, "");
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

	private List<String> readRemainingArguments(CommandInterpreter intp) {
		final List<String> patterns = new ArrayList<>();
		for (String pattern = intp.nextArgument(); pattern != null; pattern = intp.nextArgument()) {
			patterns.add(pattern);
		}
		return patterns;
	}

	@Override
	public String getHelp() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("---HAWK (commands are case insensitive, <> means required, [] means optional)---\n\t");
		sbuf.append("hawkHelp - lists all the available commands for Hawk\n");
		sbuf.append("--Connections--\n\t");
		sbuf.append("hawkConnect <url> - connects to a Thrift endpoint (guesses the protocol from the URL)\n\t");
		sbuf.append("hawkDisconnect - disconnects from the current Thrift endpoint\n");
		sbuf.append("--Instances--\n\t");
		sbuf.append("hawkAddInstance <name> <adminPassword> - adds an instance with the provided name\n\t");
		sbuf.append("hawkListInstances - lists the available Hawk instances\n\t");
		sbuf.append("hawkRemoveInstance <name> - removes an instance with the provided name, if it exists\n\t");
		sbuf.append("hawkSelectInstance <name> - selects the instance with the provided name\n\t");
		sbuf.append("hawkStartInstance <name> <adminPassword> - starts the instance with the provided name\n\t");
		sbuf.append("hawkStopInstance <name> - stops the instance with the provided name\n\t");
		sbuf.append("hawkSyncInstance <name> - forces an immediate sync on the instance with the provided name\n");
		sbuf.append("--Metamodels--\n\t");
		sbuf.append("hawkListMetamodels - lists all registered metamodels in this instance\n\t");
		sbuf.append("hawkRegisterMetamodel <files...> - registers one or more metamodels\n\t");
		sbuf.append("hawkUnregisterMetamodel <uri> - unregisters the metamodel with the specified URI\n");
		sbuf.append("--Repositories--\n\t");
		sbuf.append("hawkAddRepository <url> <type> [user] [pwd] - adds a repository\n\t");
		sbuf.append("hawkListFiles <url> [filepatterns...] - lists files within a repository\n\t");
		sbuf.append("hawkListRepositories - lists all registered metamodels in this instance\n\t");
		sbuf.append("hawkListRepositoryTypes - lists available repository types\n\t");
		sbuf.append("hawkRemoveRepository <url> - removes the repository with the specified URL\n\t");
		sbuf.append("hawkUpdateRepositoryCredentials <url> <user> <pwd> - changes the user/password used to monitor a repository\n");
		sbuf.append("--Queries--\n\t");
		sbuf.append("hawkGetModel <repo> [filepatterns...] - returns all the model elements of the specified files within the repo\n\t");
		sbuf.append("hawkGetRoots <repo> [filepatterns...] - returns only the root model elements of the specified files within the repo\n\t");
		sbuf.append("hawkListQueryLanguages - lists all available query languages\n\t");
		sbuf.append("hawkQuery <query> <language> [repo] [files] - queries the index\n\t");
		sbuf.append("hawkResolveProxies <ids...> - retrieves model elements by ID\n");
		sbuf.append("--Derived attributes--\n\t");
		sbuf.append("hawkAddDerivedAttribute <mmURI> <mmType> <name> <type> <lang> <expr> [many|ordered|unique]* - adds a derived attribute\n\t");
		sbuf.append("hawkListDerivedAttributes - lists all available derived attributes\n\t");
		sbuf.append("hawkRemoveDerivedAttribute <mmURI> <mmType> <name> - removes a derived attribute, if it exists\n");
		sbuf.append("--Indexed attributes--\n\t");
		sbuf.append("hawkAddIndexedAttribute <mmURI> <mmType> <name> - adds an indexed attribute\n\t");
		sbuf.append("hawkListIndexedAttributes - lists all available indexed attributes\n\t");
		sbuf.append("hawkRemoveIndexedAttribute <mmURI> <mmType> <name> - removes an indexed attribute, if it exists\n");
		sbuf.append("--Notifications--\n\t");
		sbuf.append("hawkWatchModelChanges [default|temporary|durable] [client ID] [repo] [files...] - watches an Artemis message queue with detected model changes\n");
		return sbuf.toString();
	}

}
