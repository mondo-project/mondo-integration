/*******************************************************************************
 * Copyright (c) 2011-2015 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Antonio Garcia-Dominguez - initial API and implementation
 ******************************************************************************/
package uk.ac.york.mondo.integration.hawk.emf.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.MessageHandler;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.HawkAttributeRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkAttributeUpdateEvent;
import uk.ac.york.mondo.integration.api.HawkChangeEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkSynchronizationEndEvent;
import uk.ac.york.mondo.integration.api.HawkSynchronizationStartEvent;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.hawk.emf.IHawkChangeEventHandler;
import uk.ac.york.mondo.integration.api.utils.ActiveMQBufferTransport;

/**
 * Maps the Thrift messages sent through Artemis to a {@link HawkResourceImpl}, which simply keeps the contents of the resource up
 * to date.
 */
class CompositeHawkChangeEventHandler implements MessageHandler, IHawkChangeEventHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeHawkChangeEventHandler.class);

	private final Set<IHawkChangeEventHandler> handlers = new HashSet<>();
	private TProtocolFactory protocolFactory = ThriftProtocol.TUPLE.getProtocolFactory();

	public CompositeHawkChangeEventHandler(IHawkChangeEventHandler... initialHandlers) {
		handlers.addAll(Arrays.asList(initialHandlers));
	}

	public TProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public void setProtocolFactory(TProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	@Override
	public void onMessage(ClientMessage message) {
		try {
			final TProtocol proto = protocolFactory.getProtocol(new ActiveMQBufferTransport(message.getBodyBuffer()));
			final HawkChangeEvent change = new HawkChangeEvent();
			try {
				change.read(proto);

				// Artemis uses a pool of threads to receive messages: we need to serialize
				// the accesses to avoid race conditions between 'model element added' and
				// 'attribute changed', for instance.
				synchronized (this) {
					LOGGER.debug("Received message from Artemis at {}: {}", message.getAddress(), change);

					if (change.isSetModelElementAttributeUpdate()) {
						handle(change.getModelElementAttributeUpdate());
					}
					else if (change.isSetModelElementAttributeRemoval()) {
						handle(change.getModelElementAttributeRemoval());
					}
					else if (change.isSetModelElementAddition()) {
						handle(change.getModelElementAddition());
					}
					else if (change.isSetModelElementRemoval()) {
						handle(change.getModelElementRemoval());
					}
					else if (change.isSetReferenceAddition()) {
						handle(change.getReferenceAddition());
					}
					else if (change.isSetReferenceRemoval()) {
						handle(change.getReferenceRemoval());
					}
					else if (change.isSetSyncStart()) {
						handle(change.getSyncStart());
					}
					else if (change.isSetSyncEnd()) {
						handle(change.getSyncEnd());
					}
				}
			} catch (TException e) {
				LOGGER.error("Error while decoding incoming message", e);
			}

			message.acknowledge();
		} catch (ActiveMQException e) {
			LOGGER.error("Failed to ack message", e);
		}
	}

	@Override
	public void handle(HawkModelElementAdditionEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkModelElementRemovalEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkReferenceRemovalEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkReferenceAdditionEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkAttributeRemovalEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkAttributeUpdateEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkSynchronizationStartEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	@Override
	public void handle(HawkSynchronizationEndEvent ev) {
		for (IHawkChangeEventHandler h : handlers) {
			h.handle(ev);
		}
	}

	public boolean addHandler(IHawkChangeEventHandler handler) {
		return handlers.add(handler);
	}

	public boolean removeHandler(IHawkChangeEventHandler handler) {
		return handlers.remove(handler);
	}

}