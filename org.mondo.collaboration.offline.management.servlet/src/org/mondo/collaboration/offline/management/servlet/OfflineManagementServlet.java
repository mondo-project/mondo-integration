package org.mondo.collaboration.offline.management.servlet;

import org.apache.thrift.server.TServlet;

import uk.ac.york.mondo.integration.api.OfflineCollaboration;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;

/*******************************************************************************
 * Copyright (c) 2016 Budapest University of Technology and Economics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/
public class OfflineManagementServlet extends TServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6509336680278155129L;

	public OfflineManagementServlet() {
		super(
				new OfflineCollaboration.Processor<OfflineCollaboration.Iface>(new OfflineCollaborationHandler()), 
				ThriftProtocol.JSON.getProtocolFactory()
		);
	}
	
}
