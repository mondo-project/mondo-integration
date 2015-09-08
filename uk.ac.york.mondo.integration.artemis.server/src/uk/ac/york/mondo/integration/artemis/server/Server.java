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

import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper over an embedded Apache Artemis server. The server listens on port
 * {@link TransportConstants#DEFAULT_LOCAL_PORT} by default and also supports
 * in-VM connections.
 */
public class Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	private EmbeddedActiveMQ server;

	public void start() throws Exception {
		if (server != null) return;

		LOGGER.info("Artemis server starting...");

		Configuration config = new ConfigurationImpl();

		// TODO: integrate with auth after it's done
		config.setSecurityEnabled(false);

		Set<TransportConfiguration> transports = new HashSet<>();
		transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
		transports.add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
		config.setAcceptorConfigurations(transports);

		server = new EmbeddedActiveMQ();
		server.setConfiguration(config);
		server.start();

		LOGGER.info("Artemis server started");
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
	
}
