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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import org.hawk.core.IAbstractConsole;
import org.hawk.core.IMetaModelResourceFactory;
import org.hawk.core.IMetaModelUpdater;
import org.hawk.core.IModelIndexer;
import org.hawk.core.IModelResourceFactory;
import org.hawk.core.IModelUpdater;
import org.hawk.core.IVcsManager;
import org.hawk.core.VcsCommitItem;
import org.hawk.core.VcsRepository;
import org.hawk.core.VcsRepositoryDelta;
import org.hawk.core.graph.IGraphChangeListener;
import org.hawk.core.graph.IGraphDatabase;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.query.IAccessListener;
import org.hawk.core.query.IQueryEngine;
import org.hawk.core.query.InvalidQueryException;
import org.hawk.core.query.QueryExecutionException;
import org.hawk.core.runtime.util.SecurityManager;
import org.hawk.core.util.HawkProperties;

import uk.ac.york.mondo.integration.api.Credentials;
import uk.ac.york.mondo.integration.api.DerivedAttributeSpec;
import uk.ac.york.mondo.integration.api.FailedQuery;
import uk.ac.york.mondo.integration.api.Hawk.Client;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.IndexedAttributeSpec;
import uk.ac.york.mondo.integration.api.InvalidQuery;
import uk.ac.york.mondo.integration.api.Repository;
import uk.ac.york.mondo.integration.api.UnknownQueryLanguage;
import uk.ac.york.mondo.integration.api.utils.APIUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ThriftRemoteModelIndexer implements IModelIndexer {

	private final class RemoteQueryEngine implements IQueryEngine {
		private final String language;

		private RemoteQueryEngine(String language) {
			this.language = language;
		}

		@Override
		public List<String> validate(String derivationlogic) {
			// TODO should we add something for this in the API?
			return Arrays.asList(derivationlogic);
		}

		@Override
		public String getType() {
			return language;
		}

		@Override
		public Object contextlessQuery(IGraphDatabase g, File query)
				throws InvalidQueryException, QueryExecutionException {
			try {
				return contextlessQuery(g, fileToString(query));
			} catch (IOException e) {
				throw new InvalidQueryException(e);
			}
		}

		@Override
		public Object contextlessQuery(IGraphDatabase g, String query)
				throws InvalidQueryException, QueryExecutionException {
			try {
				final boolean includeAttributes = true;
				final boolean includeReferences = true;
				final boolean includeNodeIDs = true;
				final boolean includeContained = false;
				return client.query(name, query, language, "*", Arrays.asList("*"),
						includeAttributes, includeReferences, includeNodeIDs, includeContained);
			} catch (UnknownQueryLanguage|InvalidQuery ex) {
				throw new InvalidQueryException(ex);
			} catch (FailedQuery ex) {
				throw new QueryExecutionException(ex);
			} catch (TException e) {
				console.printerrln("Could not run contextless query");
				console.printerrln(e);
				return null;
			}
		}

		@Override
		public Object contextfullQuery(IGraphDatabase g, File query,
				Map<String, String> context) throws InvalidQueryException,
				QueryExecutionException {
			try {
				return contextfullQuery(g, fileToString(query), context);
			} catch (IOException e) {
				throw new InvalidQueryException(e);
			}
		}

		@Override
		public Object contextfullQuery(IGraphDatabase g, String query,
				Map<String, String> context) throws InvalidQueryException,
				QueryExecutionException {
			String sRepoScope = context.get(PROPERTY_REPOSITORYCONTEXT);
			if (sRepoScope == null) {
				sRepoScope = "*";
			}

			final String sFileScope = context.get(PROPERTY_FILECONTEXT);
			final List<String> filePatterns = new ArrayList<>();
			if (sFileScope == null) {
				filePatterns.add("*");
			} else {
				final String[] sFilePatterns = sFileScope.split(",");
				for (String sFilePattern : sFilePatterns) {
					filePatterns.add(sFilePattern);
				}
			}

			try {
				final boolean includeAttributes = true;
				final boolean includeReferences = true;
				final boolean includeNodeIDs = true;
				final boolean includeContained = false;
				return client.query(name, query, language, sRepoScope,
						filePatterns, includeAttributes, includeReferences,
						includeNodeIDs, includeContained);
			} catch (UnknownQueryLanguage|InvalidQuery ex) {
				throw new InvalidQueryException(ex);
			} catch (FailedQuery ex) {
				throw new QueryExecutionException(ex);
			} catch (TException e) {
				console.printerrln("Could not run contextful query");
				console.printerrln(e);
				return e;
			}
		}

		@Override
		public IAccessListener calculateDerivedAttributes(IGraphDatabase g,
				Iterable<IGraphNode> nodes) throws InvalidQueryException,
				QueryExecutionException {
			// this dummy query engine does *not* update derived attributes
			// -- we're just a client.
			return null;
		}
	}

	/**
	 * Dummy implementation of {@link IVcsManager} that only provides the
	 * location and type and sends credential changes to the remote instance.
	 * Only useful for the GUI when querying a remote Hawk instance using the
	 * Thrift API.
	 */
	private final class DummyVcsManager implements IVcsManager {
		private final String location, type;

		private DummyVcsManager(String location, String type) {
			this.location = location;
			this.type = type;
		}

		@Override
		public String getCurrentRevision(VcsRepository repository)
				throws Exception {
			return null;
		}

		@Override
		public String getFirstRevision(VcsRepository repository)
				throws Exception {
			return null;
		}

		@Override
		public VcsRepositoryDelta getDelta(VcsRepository repository,
				String startRevision) throws Exception {
			return null;
		}

		@Override
		public VcsRepositoryDelta getDelta(VcsRepository repository,
				String startRevision, String endRevision)
				throws Exception {
			return null;
		}

		@Override
		public void importFiles(String path, File temp) {
			// nothing to do
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public void run(String vcsloc, String un, String pw,
				IAbstractConsole c) throws Exception {
			// nothing to do
		}

		@Override
		public void shutdown() {
			// nothing to do
		}

		@Override
		public String getLocation() {
			return location;
		}

		@Override
		public String getUsername() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public String getHumanReadableName() {
			return location;
		}

		@Override
		public String getCurrentRevision() throws Exception {
			return null;
		}

		@Override
		public List<VcsCommitItem> getDelta(String string)
				throws Exception {
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((location == null) ? 0 : location.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DummyVcsManager other = (DummyVcsManager) obj;
			if (location == null) {
				if (other.location != null)
					return false;
			} else if (!location.equals(other.location))
				return false;
			return true;
		}

		@Override
		public boolean isAuthSupported() {
			return false;
		}

		@Override
		public boolean isPathLocationAccepted() {
			return true;
		}

		@Override
		public boolean isURLLocationAccepted() {
			return true;
		}

		@Override
		public void setCredentials(String username, String password) {
			try {
				client.updateRepositoryCredentials(
					name, location, new Credentials(username, password));
			} catch (TException e) {
				console.printerrln(e);
			}
		}
	}

	private final String name;
	private final Client client;
	private final IAbstractConsole console;
	private char[] adminPw;

	/** Folder containing the Hawk properties.xml file. */
	private final File parentFolder;

	public ThriftRemoteModelIndexer(String name, File parentFolder, Client client, IAbstractConsole console) throws IOException {
		this.name = name;
		this.client = client;
		this.console = console;
		this.parentFolder = parentFolder;

		createDummyProperties(parentFolder);
	}

	private void createDummyProperties(File parentFolder) throws IOException {
		if (parentFolder.exists()) {
			return;
		}

		parentFolder.mkdirs();
		HawkProperties props = new HawkProperties();
		props.setDbType("dummy");
		props.setMonitoredVCS(new ArrayList<String[]>());

		XStream stream = new XStream(new DomDriver());
		stream.processAnnotations(HawkProperties.class);
		String out = stream.toXML(props);
		try (BufferedWriter b = new BufferedWriter(new FileWriter(
				getParentFolder() + File.separator + "properties.xml"))) {
			b.write(out);
			b.flush();
		}
	}

	@Override
	public boolean synchronise() throws Exception {
		// the server takes care of the synchronisation on its own
		return true;
	}

	@Override
	public void shutdown(ShutdownRequestType type) throws Exception {
		if (type == ShutdownRequestType.ALWAYS) {
			// for remote instances, we only honour explicit requests by users.
			client.stopInstance(name);
		}
	}

	@Override
	public void delete() throws Exception {
		client.removeInstance(name);
	}

	@Override
	public IGraphDatabase getGraph() {
		console.printerrln("Graph is not accessible for " + ThriftRemoteHawk.class.getName());
		return null;
	}

	@Override
	public Set<IVcsManager> getRunningVCSManagers() {
		try {
			List<Repository> repositories = client.listRepositories(name);
			Set<IVcsManager> dummies = new HashSet<>();
			for (final Repository repo : repositories) {
				dummies.add(new DummyVcsManager(repo.uri, repo.type));
			}
			return dummies;
		} catch (TException e) {
			console.printerrln(e);
			return Collections.emptySet();
		}
	}

	@Override
	public Set<String> getKnownMMUris() {
		try {
			return new HashSet<>(client.listMetamodels(name));
		} catch (TException e) {
			console.printerrln(e);
			return Collections.emptySet();
		}
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public void registerMetamodel(File[] files) throws Exception {
		List<uk.ac.york.mondo.integration.api.File> thriftFiles = new ArrayList<>();
		for (File f : files) {
			thriftFiles.add(APIUtils.convertJavaFileToThriftFile(f));
		}
		client.registerMetamodels(name, thriftFiles);
	}

	@Override
	public void registerMetamodel(File f) throws Exception {
		registerMetamodel(new File[]{ f });
	}

	@Override
	public void removeMetamodel(File[] metamodel) throws Exception {
		/*
		 * TODO ask Kostas - why does this take a File and not a metamodel URI?
		 * Hawk copies metamodels to its own storage, so this doesn't make
		 * sense.
		 */
		console.printerrln("WARN: removing metamodels is not supported yet in " + this.getClass().getName());
	}

	@Override
	public void removeMetamodel(File metamodel) throws Exception {
		removeMetamodel(new File[] { metamodel });
	}

	@Override
	public IAbstractConsole getConsole() {
		return console;
	}

	@Override
	public void addVCSManager(IVcsManager vcs, boolean persist) {
		Credentials credentials = null;
		if (vcs.getUsername() != null || vcs.getPassword() != null) {
			credentials = new Credentials();
			credentials.setUsername(vcs.getUsername());
			credentials.setPassword(vcs.getPassword());
		}
		try {
			client.addRepository(name, new Repository(vcs.getLocation(), vcs.getType()), credentials);
		} catch (TException e) {
			console.printerrln("Could not add the specified repository");
			console.printerrln(e);
		}
	}

	@Override
	public void addModelUpdater(IModelUpdater updater) {
		console.printerrln("Cannot add model updaters to " + this.getClass().getName());
	}

	@Override
	public void addMetaModelResourceFactory(IMetaModelResourceFactory metaModelParser) {
		console.printerrln("Cannot add metamodel resource factories to " + this.getClass().getName());
	}

	@Override
	public void addModelResourceFactory(IModelResourceFactory modelParser) {
		console.printerrln("Cannot add model resource factories to " + this.getClass().getName());
	}

	@Override
	public void setDB(IGraphDatabase db, boolean persist) {
		console.printerrln("Cannot change the DB in " + this.getClass().getName());
	}

	@Override
	public void addQueryEngine(IQueryEngine q) {
		console.printerrln("Cannot add query engines to " + this.getClass().getName());
	}

	@Override
	public void init() throws Exception {
		try {
			client.startInstance(name, new String(adminPw));
		} catch (HawkInstanceNotFound ex) {
			client.createInstance(name, new String(adminPw));
		}
	}

	@Override
	public IModelResourceFactory getModelParser(String type) {
		console.printerrln("Cannot access model parsers in " + this.getClass().getName());
		return null;
	}

	@Override
	public IMetaModelResourceFactory getMetaModelParser(String metaModelType) {
		console.printerrln("Cannot access metamodel parsers in " + this.getClass().getName());
		return null;
	}

	@Override
	public Map<String, IQueryEngine> getKnownQueryLanguages() {
		try {
			final Map<String, IQueryEngine> dummyMap = new HashMap<>();
			for (final String language : client.listQueryLanguages(name)) {
				dummyMap.put(language, new RemoteQueryEngine(language));
			}
			return dummyMap;
		} catch (TException e) {
			console.printerrln("Could not retrieve the known query languages");
			console.printerrln(e);
			return Collections.emptyMap();
		}
	}

	@Override
	public File getParentFolder() {
		return parentFolder;
	}

	@Override
	public void logFullStore() throws Exception {
		console.printerrln("Cannot log content of graph in " + this.getClass().getName());
	}

	@Override
	public void resetScheduler() {
		console.printerrln("Cannot reset the scheduler for a " + this.getClass().getName());
	}

	@Override
	public void setMetaModelUpdater(IMetaModelUpdater metaModelUpdater) {
		console.printerrln("Cannot change the metamodel updater in " + this.getClass().getName());
	}

	@Override
	public void addDerivedAttribute(String metamodeluri, String typename,
			String attributename, String attributetype, boolean isMany,
			boolean isOrdered, boolean isUnique, String derivationlanguage,
			String derivationlogic) {
		DerivedAttributeSpec spec = new DerivedAttributeSpec();
		spec.setMetamodelUri(metamodeluri);
		spec.setTypeName(typename);
		spec.setAttributeName(attributename);
		spec.setAttributeType(attributetype);
		spec.setIsMany(isMany);
		spec.setIsOrdered(isOrdered);
		spec.setIsUnique(isUnique);
		spec.setDerivationLanguage(derivationlanguage);
		spec.setDerivationLogic(derivationlogic);

		try {
			client.addDerivedAttribute(name, spec);
		} catch (TException e) {
			console.printerrln("Could not add derived attribute");
			console.printerrln(e);
		}
	}

	@Override
	public void addIndexedAttribute(String metamodeluri, String typename, String attributename) {
		IndexedAttributeSpec spec = new IndexedAttributeSpec();
		spec.setMetamodelUri(metamodeluri);
		spec.setTypeName(typename);
		spec.setAttributeName(attributename);

		try {
			client.addIndexedAttribute(name, spec);
		} catch (TException e) {
			console.printerrln("Could not add indexed attribute");
			console.printerrln(e);
		}
	}

	private static String fileToString(File queryFile) throws IOException, FileNotFoundException {
		final StringBuffer sbuf = new StringBuffer();
		try (BufferedReader reader = new BufferedReader(new FileReader(queryFile))) {
			sbuf.append(reader.readLine());
		}
		final String query = sbuf.toString();
		return query;
	}

	@Override
	public Collection<String> getDerivedAttributes() {
		final List<String> attrs = new ArrayList<>();
		try {
			for (DerivedAttributeSpec spec : client.listDerivedAttributes(name)) {
				attrs.add(String.format("%s##%s##%s", spec.metamodelUri, spec.typeName, spec.attributeName));
			}
		} catch (TException e) {
			console.printerrln("Could not list the derived attributes");
			console.printerrln(e);
		}
		return attrs;
	}

	@Override
	public Collection<String> getIndexedAttributes() {
		final List<String> attrs = new ArrayList<>();
		try {
			for (IndexedAttributeSpec spec : client.listIndexedAttributes(name)) {
				attrs.add(String.format("%s##%s##%s", spec.metamodelUri, spec.typeName, spec.attributeName));
			}
		} catch (TException e) {
			console.printerrln("Could not list the indexed attributes");
			console.printerrln(e);
		}
		return attrs;
	}

	@Override
	public Collection<String> getIndexes() {
		return Collections.emptyList();
	}

	@Override
	public List<String> validateExpression(String derivationlanguage, String derivationlogic) {
		return getKnownQueryLanguages().get(derivationlanguage).validate(derivationlogic);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setAdminPassword(char[] pw) {
		this.adminPw = pw;
	}

	@Override
	public String decrypt(String pw) throws GeneralSecurityException, IOException {
		return SecurityManager.decrypt(pw, adminPw);
	}

	@Override
	public boolean isRunning() {
		try {
			for (HawkInstance instance : client.listInstances()) {
				if (instance.name.equals(name)) {
					return instance.running;
				}
			}
		} catch (TException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean addGraphChangeListener(IGraphChangeListener changeListener) {
		// TODO Integrate remote notifications here!
		return false;
	}

	@Override
	public boolean removeGraphChangeListener(IGraphChangeListener changeListener) {
		// TODO Integrate remote notifications here!
		return false;
	}

	@Override
	public IGraphChangeListener getCompositeGraphChangeListener() {
		// TODO Integrate remote notifications here!
		return null;
	}

}
