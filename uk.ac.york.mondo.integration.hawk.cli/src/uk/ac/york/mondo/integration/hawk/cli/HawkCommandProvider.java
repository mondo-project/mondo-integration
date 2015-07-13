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

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.THttpClient;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import uk.ac.york.mondo.integration.api.Hawk;

/**
 * Simple command-line based
 */
public class HawkCommandProvider implements CommandProvider {

	private Hawk.Client client;

	public Object _hawkConnect(CommandInterpreter intp) throws Exception {
		final String url = intp.nextArgument();

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
		}
		return null;
	}

	@Override
	public String getHelp() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("---Hawk commands---\n\t");
		sbuf.append("connect <url> - connects to a Thrift endpoint\n\t");
		sbuf.append("disconnect - disconnects from the current Thrift endpoint\n\t");
		return sbuf.toString();
	}

}
