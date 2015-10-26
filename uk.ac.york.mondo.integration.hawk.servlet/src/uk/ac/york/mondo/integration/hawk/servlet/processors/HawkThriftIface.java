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
package uk.ac.york.mondo.integration.hawk.servlet.processors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSession.QueueQuery;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.thrift.TException;
import org.hawk.core.IModelIndexer.ShutdownRequestType;
import org.hawk.core.IVcsManager;
import org.hawk.core.graph.IGraphDatabase;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.graph.IGraphNodeReference;
import org.hawk.core.graph.IGraphTransaction;
import org.hawk.core.graph.IGraphTypeNodeReference;
import org.hawk.core.query.IQueryEngine;
import org.hawk.core.query.InvalidQueryException;
import org.hawk.core.query.QueryExecutionException;
import org.hawk.core.runtime.LocalHawkFactory;
import org.hawk.graph.FileNode;
import org.hawk.graph.GraphWrapper;
import org.hawk.graph.ModelElementNode;
import org.hawk.neo4j_v2.Neo4JDatabase;
import org.hawk.osgiserver.HManager;
import org.hawk.osgiserver.HModel;
import org.hawk.osgiserver.SecurePreferencesCredentialsStore;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.Credentials;
import uk.ac.york.mondo.integration.api.DerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.FailedQuery;
import uk.ac.york.mondo.integration.api.File;
import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.HawkInstanceNotRunning;
import uk.ac.york.mondo.integration.api.IndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidDerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidIndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidMetamodel;
import uk.ac.york.mondo.integration.api.InvalidPollingConfiguration;
import uk.ac.york.mondo.integration.api.InvalidQuery;
import uk.ac.york.mondo.integration.api.ModelElement;
import uk.ac.york.mondo.integration.api.QueryResult;
import uk.ac.york.mondo.integration.api.QueryResult._Fields;
import uk.ac.york.mondo.integration.api.Repository;
import uk.ac.york.mondo.integration.api.Subscription;
import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.UnknownQueryLanguage;
import uk.ac.york.mondo.integration.api.UnknownRepositoryType;
import uk.ac.york.mondo.integration.api.VCSAuthenticationFailed;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.artemis.server.Server;
import uk.ac.york.mondo.integration.hawk.servlet.Activator;
import uk.ac.york.mondo.integration.hawk.servlet.artemis.ArtemisProducerGraphChangeListener;
import uk.ac.york.mondo.integration.hawk.servlet.servlets.HawkThriftTupleServlet;
import uk.ac.york.mondo.integration.hawk.servlet.utils.HawkModelElementEncoder;
import uk.ac.york.mondo.integration.hawk.servlet.utils.HawkModelElementTypeEncoder;

/**
 * Entry point to the Hawk model indexers, implementing a Thrift-based API.
 */
final class HawkThriftIface implements Hawk.Iface {

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkThriftIface.class); 

	private final ThriftProtocol thriftProtocol;
	private final Server artemisServer;

	// TODO: create Equinox declarative service for using this information for ACL
	@SuppressWarnings("unused")
	private final HttpServletRequest request;
	
	private static enum CollectElements { ALL, ONLY_ROOTS; }

	/**
	 * Only to be used from {@link HawkThriftProcessorFactory} to retrieve the
	 * original process map.
	 */
	HawkThriftIface() {
		this(null, null, null);
	}

	public HawkThriftIface(ThriftProtocol eventProtocol, HttpServletRequest request, Server artemisServer) {
		this.thriftProtocol = eventProtocol;
		this.request = request;
		this.artemisServer = artemisServer;
	}

	public ThriftProtocol getThriftProtocol() {
		return thriftProtocol;
	}

	private HModel getRunningHawkByName(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		HModel model = getHawkByName(name);
		if (model.isRunning()) {
			return model;
		} else {
			throw new HawkInstanceNotRunning();
		}
	}

	private HModel getHawkByName(String name) throws HawkInstanceNotFound {
		final HModel model = HManager.getInstance().getHawkByName(name);
		if (model == null) {
			throw new HawkInstanceNotFound();
		}
		return model;
	}

	@Override
	public void registerMetamodels(String name, List<File> metamodels) throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidMetamodel, TException {
		final HModel model = getRunningHawkByName(name);

		for (File f : metamodels) {
			try {
				// Remove path separators for now (UNIX-style / and Windows-style \)
				final String safeName = f.name.replaceAll("/", "_").replaceAll("\\\\", "_");
				final java.io.File dataFile = Activator.getInstance().writeToDataFile(safeName, f.contents);
				// TODO No way to report a bad file?
				model.registerMeta(dataFile);
			} catch (FileNotFoundException ex) {
				throw new TException(ex);
			} catch (IOException ex) {
				throw new TException(ex);
			}
		}
	}

	@Override
	public void unregisterMetamodels(String name, List<String> metamodels) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);
		model.removeMetamodels(metamodels.toArray(new String[metamodels.size()]));
	}

	@Override
	public List<String> listMetamodels(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);
		return model.getRegisteredMetamodels();
	}

	@Override
	public List<String> listQueryLanguages(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);
		return new ArrayList<String>(model.getKnownQueryLanguages());
	}

	@Override
	public List<QueryResult> query(String name, String query, String language,
			String repo, List<String> filePatterns, boolean includeAttributes,
			boolean includeReferences, boolean includeNodeIDs, boolean includeContained)
			throws HawkInstanceNotFound, UnknownQueryLanguage, InvalidQuery,
			FailedQuery, TException {
		final HModel model = getRunningHawkByName(name);
		Map<String, String> context = new HashMap<>();
		context.put(IQueryEngine.PROPERTY_REPOSITORYCONTEXT, repo);
		context.put(IQueryEngine.PROPERTY_FILECONTEXT, join(filePatterns, ","));
		try {
			Object ret;
			if ("*".equals(repo) && Arrays.asList("*").equals(filePatterns)) {
				ret = model.query(query, language);
			} else {
				ret = model.contextFullQuery(query, language, context);
			}

			final GraphWrapper gw = new GraphWrapper(model.getGraph());
			final HawkModelElementEncoder enc = new HawkModelElementEncoder(gw);
			enc.setUseContainment(includeContained);
			enc.setIncludeNodeIDs(includeNodeIDs);
			enc.setIncludeAttributes(includeAttributes);
			enc.setIncludeReferences(includeReferences);

			final HawkModelElementTypeEncoder typeEnc = new HawkModelElementTypeEncoder(gw);

			try (final IGraphTransaction t = model.getGraph().beginTransaction()) {
				final List<QueryResult> l = new ArrayList<>();
				addEncodedValue(model, ret, l, enc, typeEnc);
				return l;
			}
		} catch (NoSuchElementException ex) {
			ex.printStackTrace();
			throw new UnknownQueryLanguage();
		} catch (InvalidQueryException ex) {
			throw new InvalidQuery(ex.getMessage());
		} catch (QueryExecutionException ex) {
			throw new FailedQuery(ex.getMessage());
		} catch (Exception ex) {
			throw new TException(ex);
		}
	}

	private String join(List<String> strings, String separator) {
		final StringBuffer sbuf = new StringBuffer();
		boolean first = true;
		for (String s : strings) {
			if (first) {
				first = false;
			} else {
				sbuf.append(separator);
			}
			sbuf.append(s);
		}
		return sbuf.toString();
	}

	private void addEncodedValue(final HModel model, Object ret, final List<QueryResult> l,
			HawkModelElementEncoder enc,
			HawkModelElementTypeEncoder typeEnc) throws Exception {
		if (ret instanceof Boolean) {
			l.add(new QueryResult(_Fields.V_BOOLEAN, (Boolean)ret));
		} else if (ret instanceof Byte) {
			l.add(new QueryResult(_Fields.V_BYTE, (Byte)ret));
		} else if (ret instanceof Double || ret instanceof Float) {
			l.add(new QueryResult(_Fields.V_DOUBLE, (Double)ret));
		} else if (ret instanceof Integer) {
			l.add(new QueryResult(_Fields.V_INTEGER, (Integer)ret));
		} else if (ret instanceof Long) {
			l.add(new QueryResult(_Fields.V_LONG, (Long)ret));
		} else if (ret instanceof Short) {
			l.add(new QueryResult(_Fields.V_SHORT, (Short)ret));
		} else if (ret instanceof String) {
			l.add(new QueryResult(_Fields.V_STRING, (String)ret));
		} else if (ret instanceof IGraphTypeNodeReference) {
			final String id = ((IGraphTypeNodeReference)ret).getId();
			l.add(new QueryResult(_Fields.V_MODEL_ELEMENT_TYPE, typeEnc.encode(id)));
		} else if (ret instanceof IGraphNodeReference) {
			final String id = ((IGraphNodeReference)ret).getId();
			if (!enc.isEncoded(id)) {
				l.add(new QueryResult(_Fields.V_MODEL_ELEMENT, enc.encode(id)));
			}
		} else if (ret instanceof IGraphNode) {
			final ModelElementNode meNode = new ModelElementNode((IGraphNode)ret);
			if (!enc.isEncoded(meNode)) {
				l.add(new QueryResult(_Fields.V_MODEL_ELEMENT, enc.encode(meNode)));
			}
		} else if (ret instanceof Iterable) {
			final Iterable<?> c = (Iterable<?>) ret;
			for (Object o : c) {
				addEncodedValue(model, o, l, enc, typeEnc);
			}
		} else {
			l.add(new QueryResult(_Fields.V_STRING, ret + ""));
		}
	}

	@Override
	public List<ModelElement> resolveProxies(String name, List<String> ids, boolean includeAttributes, boolean includeReferences) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);

		final IGraphDatabase graph = model.getGraph();
		try (IGraphTransaction tx = graph.beginTransaction()) {
			final HawkModelElementEncoder encoder = new HawkModelElementEncoder(new GraphWrapper(graph));
			encoder.setIncludeNodeIDs(true);
			encoder.setUseContainment(false);
			encoder.setIncludeAttributes(includeAttributes);
			encoder.setIncludeReferences(includeReferences);
			for (String id : ids) {
				try {
					encoder.encode(id);
				} catch (Exception ex) {
					LOGGER.error(ex.getMessage(), ex);
				}
			}
			final List<ModelElement> results = new ArrayList<ModelElement>(encoder.getElements());
			return results;
		} catch (Exception ex) {
			throw new TException(ex);
		}
	}

	@Override
	public void addRepository(String name, Repository repo, Credentials credentials) throws HawkInstanceNotFound, HawkInstanceNotRunning, UnknownRepositoryType, VCSAuthenticationFailed {

		// TODO Integrate with centralized repositories API
		final HModel model = getRunningHawkByName(name);
		try {
			final String username = credentials != null ? credentials.username : null;
			final String password = credentials != null ? credentials.password : null;
			model.addVCS(repo.uri, repo.type, username, password);
		} catch (NoSuchElementException ex) {
			throw new UnknownRepositoryType();
		}
	}

	@Override
	public void removeRepository(String name, String uri) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);
		try {
			model.removeRepository(uri);
		} catch (Exception ex) {
			throw new TException(ex);
		}
	}

	@Override
	public void updateRepositoryCredentials(String name, String uri, Credentials cred) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);
		for (IVcsManager mgr : model.getRunningVCSManagers()) {
			if (mgr.getLocation().equals(uri)) {
				mgr.setCredentials(cred.username, cred.password, model.getManager().getCredentialsStore());
				return;
			}
		}
	}

	@Override
	public List<Repository> listRepositories(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);
		final List<Repository> repos = new ArrayList<Repository>();
		for (IVcsManager mgr : model.getRunningVCSManagers()) {
			repos.add(new Repository(mgr.getLocation(), mgr.getType()));
		}
		return repos;
	}

	@Override
	public List<String> listRepositoryTypes() {
		return new ArrayList<String>(HManager.getInstance().getVCSTypes());
	}

	@Override
	public List<String> listFiles(String name, List<String> repository, List<String> filePatterns) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);

		final IGraphDatabase graph = model.getGraph();
		try (IGraphTransaction t = graph.beginTransaction()) {
			final GraphWrapper gw = new GraphWrapper(graph);

			final Set<FileNode> fileNodes = gw.getFileNodes(repository, filePatterns);
			final List<String> files = new ArrayList<>(fileNodes.size());
			for (FileNode node : fileNodes) {
				files.add(node.getFilePath());
			}

			return files;
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new TException(ex);
		}
	}

	@Override
	public void configurePolling(String name, int base, int max) throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidPollingConfiguration {
		final HModel model = getRunningHawkByName(name);
		model.configurePolling(base, max);
	}

	@Override
	public void addDerivedAttribute(String name, DerivedAttributeSpec spec)
			throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidDerivedAttributeSpec,
			TException {
		final HModel model = getRunningHawkByName(name);

		try {
			model.addDerivedAttribute(
				spec.metamodelUri, spec.typeName, spec.attributeName, spec.attributeType,
				spec.isMany, spec.isOrdered, spec.isUnique,
				spec.derivationLanguage, spec.derivationLogic);
		} catch (Exception ex) {
			throw new TException(ex);
		}
	}

	@Override
	public void removeDerivedAttribute(String name, DerivedAttributeSpec spec) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);
		model.removeDerivedAttribute(
			spec.metamodelUri, spec.typeName, spec.attributeName);
	}

	@Override
	public List<DerivedAttributeSpec> listDerivedAttributes(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);

		final List<DerivedAttributeSpec> specs = new ArrayList<>();
		for (String sIndexedAttr : model.getDerivedAttributes()) {
			String[] parts = sIndexedAttr.split("##", 3);
			if (parts.length != 3) {
				LOGGER.warn("Expected {} to have 3 parts, but had {} instead: skipping", sIndexedAttr, parts.length);
				continue;
			}

			final DerivedAttributeSpec spec = new DerivedAttributeSpec();
			spec.metamodelUri = parts[0];
			spec.typeName = parts[1];
			spec.attributeName = parts[2];
			specs.add(spec);
		}
		return specs;
	}

	@Override
	public void addIndexedAttribute(String name, IndexedAttributeSpec spec)
			throws HawkInstanceNotFound, HawkInstanceNotRunning, InvalidIndexedAttributeSpec, TException {
		final HModel model = getRunningHawkByName(name);
		try {
			model.addIndexedAttribute(spec.metamodelUri, spec.typeName, spec.attributeName);
		} catch (Exception e) {
			throw new TException(e);
		}
	}

	@Override
	public void removeIndexedAttribute(String name, IndexedAttributeSpec spec) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);
		model.removeIndexedAttribute(spec.metamodelUri, spec.typeName, spec.attributeName);
	}

	@Override
	public List<IndexedAttributeSpec> listIndexedAttributes(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning {
		final HModel model = getRunningHawkByName(name);

		final List<IndexedAttributeSpec> specs = new ArrayList<>();
		for (String sIndexedAttr : model.getIndexedAttributes()) {
			String[] parts = sIndexedAttr.split("##", 3);
			if (parts.length != 3) {
				LOGGER.warn("Expected {} to have 3 parts, but had {} instead: skipping", sIndexedAttr, parts.length);
				continue;
			}

			final IndexedAttributeSpec spec = new IndexedAttributeSpec(parts[0], parts[1], parts[2]);
			specs.add(spec);
		}
		return specs;
	}

	@Override
	public List<ModelElement> getModel(String name, List<String> repositories, List<String> filePath, boolean includeAttributes, boolean includeReferences, boolean includeNodeIDs) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		return collectElements(name, repositories, filePath, CollectElements.ALL, includeAttributes, includeReferences, includeNodeIDs);
	}

	@Override
	public List<ModelElement> getRootElements(String name, List<String> repositories, List<String> filePath, boolean includeAttributes, boolean includeReferences) throws TException {
		return collectElements(name, repositories, filePath, CollectElements.ONLY_ROOTS, includeAttributes, includeReferences, true);
	}

	private List<ModelElement> collectElements(String name,
			List<String> repositories, List<String> filePath, final CollectElements collectType,
			boolean includeAttributes, boolean includeReferences, boolean includeNodeIDs)
			throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);
		final GraphWrapper gw = new GraphWrapper(model.getGraph());

		// TODO filtering by repository
		try (IGraphTransaction tx = model.getGraph().beginTransaction()) {
			final HawkModelElementEncoder encoder = new HawkModelElementEncoder(new GraphWrapper(model.getGraph()));
			encoder.setIncludeAttributes(includeAttributes);
			encoder.setIncludeReferences(includeReferences);
			encoder.setIncludeNodeIDs(includeNodeIDs);
			for (FileNode fileNode : gw.getFileNodes(repositories, filePath)) {
				LOGGER.info("Retrieving elements from {}", filePath);

				if (collectType == CollectElements.ALL) {
					for (ModelElementNode meNode : fileNode.getModelElements()) {
						encoder.encode(meNode);
					}
				} else {
					encoder.setUseContainment(false);
					for (ModelElementNode meNode : fileNode.getRootModelElements()) {
						encoder.encode(meNode);
					}
				}
			}

			return new ArrayList<>(encoder.getElements());
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new TException(ex);
		}
	}

	@Override
	public void createInstance(String name, int minDelay, int maxDelay) throws TException {
		try {
			final HManager manager = HManager.getInstance();
			if (manager.getHawkByName(name) == null) {
				HModel.create(new LocalHawkFactory(), name, storageFolder(name),
						null, Neo4JDatabase.class.getName(), null,
						manager, new SecurePreferencesCredentialsStore(), minDelay, maxDelay);
			}
		} catch (Exception ex) {
			throw new TException(ex);
		}
	}

	@Override
	public List<HawkInstance> listInstances() throws TException {
		final List<HawkInstance> instances = new ArrayList<>();
		for (HModel m : HManager.getInstance().getHawks()) {
			final HawkInstance instance = new HawkInstance();
			instance.name = m.getName();
			instance.running = m.isRunning();
			instances.add(instance);
		}
		return instances;
	}

	@Override
	public void removeInstance(String name) throws HawkInstanceNotFound, TException {
		final HModel model = getHawkByName(name);
		try {
			HManager.getInstance().delete(model, true);
		} catch (BackingStoreException e) {
			throw new TException(e.getMessage(), e);
		}
	}

	@Override
	public void startInstance(String name) throws HawkInstanceNotFound, TException {
		final HModel model = getHawkByName(name);
		if (!model.isRunning()) {
			model.start(HManager.getInstance());
		}
	}

	@Override
	public void stopInstance(String name) throws HawkInstanceNotFound, TException {
		final HModel model = getHawkByName(name);
		if (model.isRunning()) {
			model.stop(ShutdownRequestType.ALWAYS);
		}
	}

	@Override
	public Subscription watchModelChanges(String name,
			String repositoryUri, List<String> filePaths,
			String uniqueClientID, SubscriptionDurability durability)
			throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getHawkByName(name);

		// TODO keep track of durable subscriptions and save/restore/list/delete them?
		try {
			final ArtemisProducerGraphChangeListener listener = new ArtemisProducerGraphChangeListener(
					model.getName(), repositoryUri, filePaths, durability, thriftProtocol);

			// TODO sanitize unique client ID?
			final String queueAddress = listener.getQueueAddress();
			final String queueName = queueAddress + "." + uniqueClientID;
			createQueue(queueAddress, queueName, durability);

			model.addGraphChangeListener(listener);
			return new Subscription(artemisServer.getHost(), artemisServer.getPort(), queueAddress, queueName);
		} catch (Exception e) {
			LOGGER.error("Could not register the new listener", e);
			throw new TException(e);
		}
	}

	private void createQueue(final String queueAddress, final String queueName, SubscriptionDurability durability) throws ActiveMQException, Exception {
		final TransportConfiguration inVMTransportConfig = new TransportConfiguration(InVMConnectorFactory.class.getName());
		try (ServerLocator loc = ActiveMQClient.createServerLocatorWithoutHA(inVMTransportConfig)) {
			try (ClientSessionFactory sf = loc.createSessionFactory()) {
				try (ClientSession session = sf.createSession()) {
					final QueueQuery queryResults = session.queueQuery(new SimpleString(queueName));
					if (!queryResults.isExists()) {
						switch (durability) {
						case TEMPORARY:
							// If we created a temporary queue here, it'd be removed right after closing the ClientSession:
							// there's no point. Only clients may create temporary queues (e.g. through their Consumer).
							LOGGER.warn("Only a client may create a temporary queue: ignoring request");
							break;
						case DURABLE:
						case DEFAULT:
							session.createQueue(queueAddress, queueName, durability == SubscriptionDurability.DURABLE);
							break;
						default:
							throw new IllegalArgumentException("Unknown subscription durability " + durability);
						}
						session.commit();
					}
				}
			}
		}
	}

	private java.io.File storageFolder(String instanceName) throws IOException {
		java.io.File dataFile = FrameworkUtil.getBundle(HawkThriftTupleServlet.class).getDataFile("hawk-" + instanceName);
		if (!dataFile.exists()) {
			dataFile.mkdir();
			LOGGER.info("Created storage directory for instance '{}' in '{}'", instanceName, dataFile.getPath());
		} else {
			LOGGER.info("Reused storage directory for instance '{}' in '{}'", instanceName, dataFile.getPath());
		}
		return dataFile;
	}

	@Override
	public void syncInstance(String name) throws HawkInstanceNotFound, HawkInstanceNotRunning, TException {
		final HModel model = getRunningHawkByName(name);
		try {
			model.sync();
		} catch (Exception e) {
			throw new TException("Could not force an immediate synchronisation", e);
		}
	}
}