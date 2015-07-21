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
 */
public class HawkModelDescriptor {

	public static final String DEFAULT_FILES = "*";
	public static final String DEFAULT_REPOSITORY = "*";

	private static final Logger LOGGER = LoggerFactory.getLogger(HawkModelDescriptor.class);
	private static final String FILE_PATTERN_SEP = ",";
	private static final String PROPERTY_HAWK_FILES = "hawk.files";
	private static final String PROPERTY_HAWK_REPOSITORY = "hawk.repository";
	private static final String PROPERTY_HAWK_INSTANCE = "hawk.instance";
	private static final String PROPERTY_HAWK_URL = "hawk.url";

	private String hawkURL;
	private String hawkInstance;
	private String hawkRepository;
	private String[] hawkFilePatterns;

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
		props.setProperty(PROPERTY_HAWK_REPOSITORY, hawkRepository);
		props.setProperty(PROPERTY_HAWK_FILES, concat(hawkFilePatterns, FILE_PATTERN_SEP));
		return props;
	}

	private void loadFromProperties(Properties props) throws IOException {
		this.hawkURL = requiredProperty(props, PROPERTY_HAWK_URL);
		this.hawkInstance = requiredProperty(props, PROPERTY_HAWK_INSTANCE);
		this.hawkRepository = optionalProperty(props, PROPERTY_HAWK_REPOSITORY, DEFAULT_REPOSITORY);
		this.hawkFilePatterns = optionalProperty(props, PROPERTY_HAWK_FILES, DEFAULT_FILES).split(FILE_PATTERN_SEP);
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
}
