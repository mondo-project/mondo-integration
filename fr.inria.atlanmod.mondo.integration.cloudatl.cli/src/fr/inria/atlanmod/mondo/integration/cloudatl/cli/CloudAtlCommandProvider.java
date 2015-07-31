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

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import uk.ac.york.mondo.integration.api.CloudATL;
import uk.ac.york.mondo.integration.api.ModelSpec;
import uk.ac.york.mondo.integration.api.TransformationStatus;
import uk.ac.york.mondo.integration.api.utils.APIUtils;

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
		client = APIUtils.connectTo(CloudATL.Client.class, url);
		return null;
	}

	public Object _cloudAtlDisconnect(CommandInterpreter intp) throws Exception {
		if (client != null) {
			client.getInputProtocol().getTransport().close();
			client = null;
			return String.format("Connection closed");
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
		return String.format("Launched Job with id '%s'", id);
	}

	public Object _cloudAtlList(CommandInterpreter intp) throws Exception {
		checkConnected();
		List<String> ids = client.getJobs();
		return String.format("Job ids: %s", ids.toString());

	}

	public Object _cloudAtlStatus(CommandInterpreter intp) throws Exception {
		checkConnected();
		String id = requiredArgument(intp, "id");
		TransformationStatus status = client.getStatus(id);
		return String.format("Job id: %s, %s, elapsed time: %d", id, status.isFinished() ? "FINISHED" : "RUNNING", status.elapsed);
	}
	
	public Object _cloudAtlKill(CommandInterpreter intp) throws Exception {
		checkConnected();
		String id = requiredArgument(intp, "id");
		client.kill(id);
		return String.format("Killed job with id '%s'", id);

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
				String.format("Required argument '%s' has not been provided", argumentName));
		}
		return value;
	}

	@Override
	public String getHelp() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("---CloudAtl (commands are case insensitive)---\n\t");
		sbuf.append("cloudAtlHelp - lists all the available commands for Hawk\n");
		sbuf.append("--Connections--\n\t");
		sbuf.append("cloudAtlConnect <url> - connects to a Thrift endpoint\n\t");
		sbuf.append("cloudAtlDisconnect - disconnects from the current Thrift endpoint\n");
		sbuf.append("--Commands--\n\t");
		sbuf.append("cloudAtlLaunch <tranformation> <source-mm> <target-mm> <input> <output> - launches an ATL transformation (all arguments are URIs to files)\n\t");
		sbuf.append("cloudAtlStatus <id> - shows the status of the specified transformation job\n\t");
		sbuf.append("cloudAtlList - lists all the transformation jobs tracked by this endpoint\n\t");
		sbuf.append("cloudAtlKill <id> - kills the transformation identified by <id>\n\t");
		return sbuf.toString();
	}

}
