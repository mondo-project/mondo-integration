/*******************************************************************************
 * Copyright (c) 2015 Atlanmod.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abel Gómez - initial API and implementation
 *******************************************************************************/
package fr.inria.atlanmod.mondo.integration.cloudatl.cli;

import java.io.Console;
import java.net.ConnectException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import uk.ac.york.mondo.integration.api.CloudATL;
import uk.ac.york.mondo.integration.api.ModelSpec;
import uk.ac.york.mondo.integration.api.TransformationStatus;
import uk.ac.york.mondo.integration.api.utils.APIUtils;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;

/**
 * Simple command-line based client for a remote CloudAtl instance, using the Thrift API.
 */
public class CloudAtlCommandProvider implements CommandProvider {

	private CloudATL.Client client;

	public Object _cloudAtlHelp(CommandInterpreter intp) {
		return getHelp();
	}

	public Object _cloudAtlConnect(CommandInterpreter intp) throws Exception {
		final String url = requiredArgument(intp, "url");

		final String username = intp.nextArgument();
		String password = intp.nextArgument();
		if (username != null && password == null) {
			Console console = System.console();
			if (console == null) {
				throw new Exception("No console: cannot read password safely");
			}

			console.writer().print("Password: ");
			password = String.valueOf(console.readPassword());
		}

		client = APIUtils.connectTo(CloudATL.Client.class, url, ThriftProtocol.JSON, username, password);
		return null;
	}

	public Object _cloudAtlDisconnect(CommandInterpreter intp) throws Exception {
		if (client != null) {
			client.getInputProtocol().getTransport().close();
			client = null;
			return "Connection closed";
		}
		else {
			return "Connection already closed";
		}
	}
	
	public Object _cloudAtlLaunch(CommandInterpreter intp) throws Exception {
		checkConnected();
		String transformation = requiredArgument(intp, "transformation-location");
		String sourcemm = requiredArgument(intp, "source-mm-location");
		String targetmm = requiredArgument(intp, "target-mm-location");
		String input = requiredArgument(intp, "input-location");
		String output = requiredArgument(intp, "output-location");
		
		ModelSpec source = new ModelSpec(input, Arrays.asList(new String[] { sourcemm }));
		ModelSpec target = new ModelSpec(output, Arrays.asList(new String[] { targetmm }));
		
		String id = client.launch(transformation, source, target);
		return MessageFormat.format("Launched Job with id ''{0}''", id);
	}

	public Object _cloudAtlList(CommandInterpreter intp) throws Exception {
		checkConnected();
		List<String> ids = client.getJobs();
		return MessageFormat.format("Job ids: {0}", ids.toString());

	}

	public Object _cloudAtlStatus(CommandInterpreter intp) throws Exception {
		checkConnected();
		String id = requiredArgument(intp, "id");
		TransformationStatus status = client.getStatus(id);
		return MessageFormat.format("Job id: {0}, {1}, elapsed time: {2} ms{3}", id,
				status.getState().toString(), status.elapsed,
				status.error != null && status.error.length() > 0 ? ", error: " + status.getError() : "");
	}
	
	public Object _cloudAtlKill(CommandInterpreter intp) throws Exception {
		checkConnected();
		String id = requiredArgument(intp, "id");
		client.kill(id);
		return MessageFormat.format("Killed job with id ''{0}''", id);

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

	/**
	 * Reads an expected argument from the interpreter.
	 * @throws IllegalArgumentException The argument has not been provided.
	 */
	private static String requiredArgument(CommandInterpreter intp, String argumentName) {
		String value = intp.nextArgument();
		if (value == null) {
			throw new IllegalArgumentException(
				MessageFormat.format("Required argument ''{0}'' has not been provided", argumentName));
		}
		return value;
	}

	@Override
	public String getHelp() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("---CloudAtl (commands are case insensitive)---\n\t");
		sbuf.append("cloudAtlHelp - lists all the available commands for CloudATL\n");
		sbuf.append("--Connections--\n\t");
		sbuf.append("cloudAtlConnect <url> [username] [password] - connects to a Thrift endpoint\n\t");
		sbuf.append("cloudAtlDisconnect - disconnects from the current Thrift endpoint\n");
		sbuf.append("--Commands--\n\t");
		sbuf.append("cloudAtlLaunch <transformation> <source-mm> <target-mm> <input> <output> - launches an ATL transformation (all arguments are hdfs:// or hawk+http:// URLs)\n\t");
		sbuf.append("cloudAtlStatus <id> - shows the status of the specified transformation job\n\t");
		sbuf.append("cloudAtlList - lists all the transformation jobs tracked by this endpoint\n\t");
		sbuf.append("cloudAtlKill <id> - kills the transformation identified by <id>\n\t");
		return sbuf.toString();
	}

}
