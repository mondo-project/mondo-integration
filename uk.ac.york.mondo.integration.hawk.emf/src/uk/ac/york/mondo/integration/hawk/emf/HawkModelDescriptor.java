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
package uk.ac.york.mondo.integration.hawk.emf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;

/**
 * Abstraction over the <code>.hawkmodel</code> file format. The file format is a
 * simple Java properties file, with the following keys:
 * </p>
 * <ul>
 * <li>{@link #PROPERTY_HAWK_URL} (required) is the URL to the remote Hawk index
 * (e.g. <code>http://127.0.0.1:8080/thrift/hawk</code>.</li>
 * <li>{@link #PROPERTY_HAWK_INSTANCE} (required) is the name of the Hawk
 * instance within the server.
 * <li>{@link #PROPERTY_HAWK_REPOSITORY} (optional) is the URL of the VCS
 * repository within Hawk that contains the models of interest, or
 * <code>*</code> if all repositories should be considered (the default, as in
 * {@link #DEFAULT_REPOSITORY}).
 * <li>{@link #PROPERTY_HAWK_FILES} (optional) is a comma-separated list of file
 * patterns to filter (such as <code>*.xmi</code>), or <code>*</code> if all
 * files should be considered (the default, as in {@link #DEFAULT_REPOSITORY}).
 * <li>{@link #PROPERTY_HAWK_LOADING_MODE} (optional) is a string with one of the
 * values in {@link LoadingMode}, indicating how should the model be lodaded.
 * The default value is {@link #DEFAULT_LOADING_MODE}.</li>
 * <li>{@link #PROPERTY_HAWK_SUBSCRIBE} (optional), indicating if the client
 * should subscribe to changes in the Hawk index for the indicated files and
 * repository. The default value is {@link #DEFAULT_IS_SUBSCRIBED}.</li>
 * <li>{@link #PROPERTY_HAWK_TPROTOCOL} (optional) is one of the values of
 * {@link ThriftProtocol}. By default, it is {@link #DEFAULT_TPROTOCOL}.</li>
 * </ul>
 */
public class HawkModelDescriptor {

	/*
	 * Note: all values of this enum must have names in uppercase, so the
	 * loadingMode values of the <code>hawk+http(s)://</code> URLs will be case
	 * insensitive.
	 */
	public static enum LoadingMode {
		/**
		 * Request every model element initially, including all attributes and
		 * references.
		 */
		GREEDY(true, true, true),

		/** Request all references initially, and request attributes on demand. */
		LAZY_ATTRIBUTES(true, false, true),

		/**
		 * Request only the root nodes' attributes and references initially. If
		 * a reference is navigated, fetch all the nodes referenced by this node.
		 */
		LAZY_CHILDREN(false, true, true),

		/**
		 * Request only the root nodes' attributes and references initially. If
		 * a reference is navigated, fetch all the nodes in this reference.
		 */
		LAZY_REFERENCES(false, true, false),

		/**
		 * Request only the root nodes without attributes. Load attributes on
		 * demand, and fetch all references from a node when one is needed.
		 */
		LAZY_ATTRIBUTES_CHILDREN(false, false, true),

		/**
		 * Request only the root nodes without attributes. Load attributes and
		 * references on demand.
		 */
		LAZY_ATTRIBUTES_REFERENCES(false, false, false)
		;

		public static String[] strings() {
			return toStringArray(values());
		}

		private final boolean greedyElements, greedyAttributes, greedyReferences;

		private LoadingMode(boolean greedyElements, boolean greedyAttributes, boolean greedyChildren) {
			this.greedyElements = greedyElements;
			this.greedyAttributes = greedyAttributes;
			this.greedyReferences = greedyElements || greedyChildren;
		}

		/**
		 * Returns <code>true</code> if all model elements should be fetched at
		 * once. If <code>false</code>, only the root elements will be fetched.
		 */
		public boolean isGreedyElements() {
			return greedyElements;
		}

		/**
		 * Returns <code>true</code> if attributes should be fetched when first
		 * fetching a node. If <code>false</code>, the attributes will be
		 * fetched on the first isSet or get call on an EAttribute from EMF.
		 */
		public boolean isGreedyAttributes() {
			return greedyAttributes;
		}

		/**
		 * Returns <code>true</code> if all references in a node should be
		 * fetched when first resolving one of those references. If
		 * <code>false</code>, only the nodes in that reference will be
		 * resolved.
		 *
		 * Setting this to <code>false</code> only makes sense if
		 * {@link #isGreedyElements()} is set to <code>false</code>. If
		 * {@link #isGreedyElements()} is true, we will already have all nodes
		 * anyway: this setting can be safely ignored in that case.
		 */
		public boolean isGreedyReferences() {
			return greedyReferences;
		}
	}

	public static final String DEFAULT_FILES = "*";
	public static final String DEFAULT_REPOSITORY = "*";
	public static final String DEFAULT_URL = "http://127.0.0.1:8080/thrift/hawk/tuple";
	public static final String DEFAULT_INSTANCE = "myhawk";
	public static final LoadingMode DEFAULT_LOADING_MODE = LoadingMode.GREEDY;
	public static final boolean DEFAULT_IS_SUBSCRIBED = false;
	public static final ThriftProtocol DEFAULT_TPROTOCOL = ThriftProtocol.TUPLE;
	public static final String DEFAULT_CLIENTID = System.getProperty("user.name");
	public static final SubscriptionDurability DEFAULT_DURABILITY = SubscriptionDurability.DEFAULT;

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkModelDescriptor.class);
	private static final String FILE_PATTERN_SEP = ",";
	private static final String PROPERTY_HAWK_FILES = "hawk.files";
	private static final String PROPERTY_HAWK_REPOSITORY = "hawk.repository";
	private static final String PROPERTY_HAWK_INSTANCE = "hawk.instance";
	private static final String PROPERTY_HAWK_URL = "hawk.url";
	private static final String PROPERTY_HAWK_TPROTOCOL = "hawk.thrift.protocol";
	private static final String PROPERTY_HAWK_LOADING_MODE = "hawk.loadingMode";
	private static final String PROPERTY_HAWK_SUBSCRIBE = "hawk.subscribe";
	private static final String PROPERTY_HAWK_CLIENTID = "hawk.clientID";
	private static final String PROPERTY_HAWK_DURABILITY = "hawk.subscriptionDurability";

	private String hawkURL = DEFAULT_URL;
	private String hawkInstance = DEFAULT_INSTANCE;
	private String hawkRepository = DEFAULT_REPOSITORY;
	private String[] hawkFilePatterns = new String[] { DEFAULT_FILES };

	private LoadingMode loadingMode = DEFAULT_LOADING_MODE;
	private boolean isSubscribed = DEFAULT_IS_SUBSCRIBED;
	private ThriftProtocol thriftProtocol = DEFAULT_TPROTOCOL;
	private String subscriptionClientID = DEFAULT_CLIENTID;
	private SubscriptionDurability subscriptionDurability = DEFAULT_DURABILITY;

	public HawkModelDescriptor() {}

	public void load(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);
		loadFromProperties(props);
	}

	public void load(Reader r) throws IOException {
		Properties props = new Properties();
		props.load(r);
		loadFromProperties(props);
	}

	public String getHawkURL() {
		return hawkURL;
	}

	public void setHawkURL(String hawkURL) {
		this.hawkURL = hawkURL;
	}

	public String getHawkInstance() {
		return hawkInstance;
	}

	public void setHawkInstance(String hawkInstance) {
		this.hawkInstance = hawkInstance;
	}

	public String getHawkRepository() {
		return hawkRepository;
	}

	public void setHawkRepository(String hawkRepository) {
		this.hawkRepository = hawkRepository;
	}

	public String[] getHawkFilePatterns() {
		return hawkFilePatterns;
	}

	public void setHawkFilePatterns(String[] hawkFilePatterns) {
		this.hawkFilePatterns = hawkFilePatterns;
	}

	public LoadingMode getLoadingMode() {
		return loadingMode;
	}

	public void setLoadingMode(LoadingMode mode) {
		this.loadingMode = mode;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public ThriftProtocol getThriftProtocol() {
		return thriftProtocol;
	}

	public void setThriftProtocol(ThriftProtocol thriftProtocol) {
		this.thriftProtocol = thriftProtocol;
	}

	public String getSubscriptionClientID() {
		return subscriptionClientID;
	}

	public void setSubscriptionClientID(String clientID) {
		this.subscriptionClientID = clientID;
	}

	public SubscriptionDurability getSubscriptionDurability() {
		return subscriptionDurability;
	}

	public void setSubscriptionDurability(SubscriptionDurability subscriptionDurability) {
		this.subscriptionDurability = subscriptionDurability;
	}

	public void save(OutputStream os) throws IOException {
		createProperties().store(os, "");
	}

	public void save(Writer w) throws IOException {
		createProperties().store(w, "");
	}

	private Properties createProperties() {
		final Properties props = new Properties();
		props.setProperty(PROPERTY_HAWK_URL, hawkURL);
		props.setProperty(PROPERTY_HAWK_INSTANCE, hawkInstance);
		props.setProperty(PROPERTY_HAWK_TPROTOCOL, thriftProtocol.toString());

		props.setProperty(PROPERTY_HAWK_REPOSITORY, hawkRepository);
		props.setProperty(PROPERTY_HAWK_FILES, concat(hawkFilePatterns, FILE_PATTERN_SEP));
		props.setProperty(PROPERTY_HAWK_LOADING_MODE, loadingMode.toString());

		props.setProperty(PROPERTY_HAWK_SUBSCRIBE, Boolean.toString(isSubscribed));
		props.setProperty(PROPERTY_HAWK_CLIENTID, subscriptionClientID);
		props.setProperty(PROPERTY_HAWK_DURABILITY, subscriptionDurability.toString());
		return props;
	}

	private void loadFromProperties(Properties props) throws IOException {
		this.hawkURL = requiredProperty(props, PROPERTY_HAWK_URL);
		this.hawkInstance = requiredProperty(props, PROPERTY_HAWK_INSTANCE);
		this.thriftProtocol = ThriftProtocol.valueOf(optionalProperty(props, PROPERTY_HAWK_TPROTOCOL, DEFAULT_TPROTOCOL + ""));

		this.hawkRepository = optionalProperty(props, PROPERTY_HAWK_REPOSITORY, DEFAULT_REPOSITORY);
		this.hawkFilePatterns = optionalProperty(props, PROPERTY_HAWK_FILES, DEFAULT_FILES).split(FILE_PATTERN_SEP);
		this.loadingMode = LoadingMode.valueOf(optionalProperty(props, PROPERTY_HAWK_LOADING_MODE, DEFAULT_LOADING_MODE + ""));

		this.isSubscribed = Boolean.valueOf(optionalProperty(props, PROPERTY_HAWK_SUBSCRIBE, Boolean.toString(DEFAULT_IS_SUBSCRIBED)));
		this.subscriptionClientID = optionalProperty(props, PROPERTY_HAWK_CLIENTID, DEFAULT_CLIENTID);
		this.subscriptionDurability = SubscriptionDurability.valueOf(optionalProperty(props, PROPERTY_HAWK_DURABILITY, DEFAULT_DURABILITY + ""));
	}

	private static String requiredProperty(Properties props, String name) throws IOException {
		final String value = (String) props.get(name);
		if (value == null) {
			throw new IOException(name + " has not been set");
		}
		return value;
	}

	private static String optionalProperty(Properties props, String name, String defaultValue) throws IOException {
		final String value = (String) props.get(name);
		if (value == null) {
			LOGGER.info("{} has not been set, using {} as default", name, defaultValue);
			return defaultValue;
		}
		return value;
	}

	private static String concat(final String[] elems, final String separator) {
		final StringBuffer sbuf = new StringBuffer();
		boolean bFirst = true;
		for (String filePattern : elems) {
			if (bFirst) {
				bFirst = false;
			} else {
				sbuf.append(separator);
			}
			sbuf.append(filePattern);
		}
		return sbuf.toString();
	}

	private static <T> String[] toStringArray(Object[] c) {
		final String[] strings = new String[c.length];
		int i = 0;
		for (Object o : c) {
			strings[i++] = o + "";
		}
		return strings;
	}
}
