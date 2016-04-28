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
package uk.ac.york.mondo.integration.artemis.server;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper over an embedded Apache Artemis server. The server listens on port
 * {@link TransportConstants#DEFAULT_LOCAL_PORT} by default and also supports
 * in-VM connections. SSL can be turned on optionally.
 */
public class Server {

	private static final String SECURITY_ENABLED_PROPERTY = "artemis.security.enabled";

	private class FluidMap<K, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 1L;

		public FluidMap<K, V> with(K key, V value) {
			put(key, value);
			return this;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

	private final String host;
	private final int port;

	private boolean listenToAllInterfaces = false;

	/**
	 * <p>
	 * Whether SSL should be enabled or not. Note that if SSL is enabled, the
	 * user will be expected to set up the proper system properties for the key
	 * store and trust store. According to the <a href=
	 * "https://activemq.apache.org/artemis/docs/1.0.0/configuring-transports.html">
	 * official docs</a>, these are:
	 * </p>
	 * <ul>
	 * <li>javax.net.ssl.keyStore / org.apache.activemq.ssl.keyStore</li>
	 * <li>javax.net.ssl.keyStorePassword / org.apache.activemq.ssl.keyStorePassword</li>
	 * <li>javax.net.ssl.trustStore / org.apache.activemq.ssl.trustStore</li>
	 * <li>javax.net.ssl.trustStorePassword / org.apache.activemq.ssl.trustStorePassword</li>
	 * </ul>
	 */
	private boolean sslEnabled = false;

	private EmbeddedActiveMQ server;

	public Server(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public boolean isListenOnAllInterfaces() {
		return listenToAllInterfaces;
	}

	public void setListenOnAllInterfaces(boolean listenOnAllInterfaces) {
		this.listenToAllInterfaces = listenOnAllInterfaces;
	}

	public boolean isSSLEnabled() {
		return sslEnabled;
	}

	public void setSSLEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public void start() throws Exception {
		if (server != null) return;

		LOGGER.info("Artemis server starting...");

		Configuration config = new ConfigurationImpl();

		// Enable/disable Artemis security through property
		final String sSecurityEnabled = System.getProperty(SECURITY_ENABLED_PROPERTY);
		boolean securityEnabled = false;
		if (sSecurityEnabled != null) {
			securityEnabled = Boolean.valueOf(sSecurityEnabled);
		}
		config.setSecurityEnabled(securityEnabled);

		// Enable in-VM, regular HTTP and Stomp over Web Sockets
		Set<TransportConfiguration> transports = new HashSet<>();
		transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

		final FluidMap<String, Object> nettyOptions = new FluidMap<String, Object>()
			.with(TransportConstants.HOST_PROP_NAME, listenToAllInterfaces ? "0.0.0.0" : host)
			.with(TransportConstants.PORT_PROP_NAME, port + "")
			.with(TransportConstants.PROTOCOLS_PROP_NAME, "CORE,STOMP")
			.with(TransportConstants.SSL_ENABLED_PROP_NAME, sslEnabled + "");
		transports.add(new TransportConfiguration(NettyAcceptorFactory.class.getName(), nettyOptions));
		config.setAcceptorConfigurations(transports);

		// Set up a paging directory (for when an address has too many messages to fit on memory)
		config.setPagingDirectory(createArtemisFolder("paging").getAbsolutePath());
		config.setBindingsDirectory(createArtemisFolder("bindings").getAbsolutePath());
		config.setJournalDirectory(createArtemisFolder("journal").getAbsolutePath());
		config.setLargeMessagesDirectory(createArtemisFolder("largemsg").getAbsolutePath());

		// Set up the default address settings:
		//   - activate paging when we hit 100MB for a single address
		//   - redeliver after 2 seconds with 1.5x multiplier (up to 30s)
		final AddressSettings defaultAddressSettings = new AddressSettings();
		defaultAddressSettings.setMaxSizeBytes(100_000_000);
		defaultAddressSettings.setRedeliveryDelay(2_000);
		defaultAddressSettings.setRedeliveryMultiplier(1.5);
		defaultAddressSettings.setMaxRedeliveryDelay(30_000);
		config.addAddressesSetting("#", defaultAddressSettings);

		server = new EmbeddedActiveMQ();
		server.setConfiguration(config);
		server.setSecurityManager(new ShiroRealmSecurityManager());
		server.start();

		LOGGER.info("Artemis server started");
	}

	protected File createArtemisFolder(final String folderSuffix) {
		File dataFile = FrameworkUtil.getBundle(Server.class).getDataFile("artemis-" + folderSuffix);
		if (!dataFile.exists()) {
			dataFile.mkdir();
			LOGGER.info("Created Artemis paging directory in '{}'", dataFile.getPath());
		} else {
			LOGGER.info("Reused Artemis paging directory in '{}'", dataFile.getPath());
		}
		return dataFile;
	}

	public void stop() throws Exception {
		if (server == null) return;

		LOGGER.info("Artemis server stopping...");
		server.stop();
		server = null;
		LOGGER.info("Artemis server stopped");
	}

	public boolean isRunning() {
		return server != null;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}
