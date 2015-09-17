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
package uk.ac.york.mondo.integration.hawk.servlet.artemis;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.hawk.core.VcsChangeType;
import org.hawk.core.VcsCommit;
import org.hawk.core.VcsCommitItem;
import org.hawk.core.VcsRepository;
import org.hawk.core.VcsRepositoryDelta;
import org.hawk.core.graph.IGraphChangeListener;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.model.IHawkClass;
import org.hawk.core.model.IHawkObject;
import org.hawk.core.model.IHawkPackage;
import org.hawk.core.runtime.CompositeGraphChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.york.mondo.integration.api.CommitItem;
import uk.ac.york.mondo.integration.api.CommitItemChangeType;
import uk.ac.york.mondo.integration.api.HawkAttributeRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkAttributeUpdateEvent;
import uk.ac.york.mondo.integration.api.HawkChangeEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceRemovalEvent;
import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.api.utils.ActiveMQBufferTransport;
import uk.ac.york.mondo.integration.hawk.servlet.utils.HawkModelElementEncoder;

/**
 * Hawk change listener that sends all changes to the specified address within
 * an Artemis in-VM server through its core protocol. Transaction management is
 * based on the {@link TransactionalSendTest} test suite in Artemis.
 *
 * This implementation redefines hashCode and equals based on the computed
 * address, so "duplicate" listeners that would result in duplicate events being
 * sent to the destination address are implicitly avoided by the
 * {@link CompositeGraphChangeListener} in most indexers.
 */
public class ArtemisProducerGraphChangeListener implements IGraphChangeListener {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArtemisProducerGraphChangeListener.class);

	private final ServerLocator locator;
	private final ClientSessionFactory sessionFactory;
	private final boolean messagesAreDurable;
	private final String queueAddress;
	private final TProtocolFactory protocolFactory;

	private ClientSession session;
	private ClientProducer producer;

	private final Pattern repositoryURIPattern, filePathPattern;

	public ArtemisProducerGraphChangeListener(String hawkInstance, String repositoryUri, List<String> filePaths, SubscriptionDurability durability, ThriftProtocol protocol) throws Exception {
		// Convert the repository URI and file paths into regexps, for faster matching
		this.repositoryURIPattern = Pattern.compile(repositoryUri.replace("*", ".*"));
		StringBuffer sbuf = new StringBuffer();
		boolean first = true;
		for (String filePath : filePaths) {
			if (first) {
				first = false;
			} else {
				sbuf.append("|");
			}
			sbuf.append(filePath.replace("*", ".*"));
		}
		this.filePathPattern = Pattern.compile(sbuf.toString());

		// Thrift protocol factory (for encoding the events)
		this.protocolFactory = protocol.getProtocolFactory();

		// Connect to Artemis
		this.locator = ActiveMQClient
				.createServerLocatorWithoutHA(new TransportConfiguration(
						InVMConnectorFactory.class.getName()));
		this.sessionFactory = locator.createSessionFactory();
		this.messagesAreDurable = durability == SubscriptionDurability.DURABLE;
		this.queueAddress = String.format("hawk.graphchanges.%s.%s.%s.%s.%s",
				hawkInstance, protocol.toString().toLowerCase(),
				repositoryUri.hashCode(), filePaths.hashCode(),
				durability.toString().toLowerCase());
	}

	public String getQueueAddress() {
		return queueAddress;
	}

	@Override
	public String getName() {
		return "Artemis graph change listener";
	}

	@Override
	public void synchroniseStart() {
		// nothing to do
	}

	@Override
	public void synchroniseEnd() {
		// nothing to do
	}

	@Override
	public void changeStart() {
		if (session == null) {
			try {
				this.session = sessionFactory
						.createSession(false, false, false);
				this.producer = session.createProducer(queueAddress);
			} catch (ActiveMQException e) {
				LOGGER.error("Could not start a new Artemis session", e);
			}
		} else {
			LOGGER.warn("session already open: last changeStart not closed");
		}
	}

	@Override
	public void changeSuccess() {
		try {
			session.commit();
			LOGGER.debug("Session committed");
		} catch (ActiveMQException e) {
			LOGGER.error("Could not commit the transaction", e);
			try {
				session.rollback();
			} catch (ActiveMQException e1) {
				LOGGER.error("Could not rollback the transaction", e1);
			}
		} finally {
			closeSession();
		}
	}

	@Override
	public void changeFailure() {
		try {
			session.rollback();
			LOGGER.debug("Session rolled back");
		} catch (ActiveMQException e) {
			LOGGER.error("Could not rollback the transaction", e);
		} finally {
			closeSession();
		}
	}

	@Override
	public void metamodelAddition(IHawkPackage pkg, IGraphNode pkgNode) {
		// nothing to do!
	}

	@Override
	public void classAddition(IHawkClass cls, IGraphNode clsNode) {
		// nothing to do!
	}

	@Override
	public void fileAddition(VcsCommitItem s, IGraphNode fileNode) {
		// nothing to do!
	}

	@Override
	public void fileRemoval(VcsCommitItem s, IGraphNode fileNode) {
		// nothing to do!
	}

	@Override
	public void modelElementAddition(VcsCommitItem s, IHawkObject element, IGraphNode elementNode, boolean isTransient) {
		if (isTransient || !isAcceptedByFilter(s)) return;

		try {
			final HawkModelElementAdditionEvent ev = new HawkModelElementAdditionEvent();
			ev.setVcsItem(mapToThrift(s));
			ev.setMetamodelURI(element.getType().getPackageNSURI());
			ev.setTypeName(element.getType().getName());
			ev.setId(elementNode.getId().toString());

			final HawkChangeEvent change = new HawkChangeEvent();
			change.setModelElementAddition(ev);
			sendEvent(change);
		} catch (Exception e) {
			LOGGER.error("Could not encode a model element", e);
		}
	}

	@Override
	public void modelElementRemoval(VcsCommitItem s, IGraphNode elementNode, boolean isTransient) {
		if (isTransient || !isAcceptedByFilter(s)) return;

		final HawkModelElementRemovalEvent ev = new HawkModelElementRemovalEvent();
		ev.setVcsItem(mapToThrift(s));
		ev.setId(elementNode.getId().toString());

		final HawkChangeEvent change = new HawkChangeEvent();
		change.setModelElementRemoval(ev);
		sendEvent(change);
	}

	@Override
	public void modelElementAttributeUpdate(VcsCommitItem s,
			IHawkObject eObject, String attrName, Object oldValue,
			Object newValue, IGraphNode elementNode, boolean isTransient) {
		if (isTransient || !isAcceptedByFilter(s)) return;

		final HawkAttributeUpdateEvent ev = new HawkAttributeUpdateEvent();
		ev.setAttribute(attrName);
		ev.setId(elementNode.getId().toString());
		ev.setValue(HawkModelElementEncoder.encodeAttributeSlot(attrName, newValue).value);
		ev.setVcsItem(mapToThrift(s));

		final HawkChangeEvent change = new HawkChangeEvent();
		change.setModelElementAttributeUpdate(ev);
		sendEvent(change);
	}

	@Override
	public void modelElementAttributeRemoval(VcsCommitItem s,
			IHawkObject eObject, String attrName, IGraphNode elementNode,
			boolean isTransient) {
		if (isTransient || !isAcceptedByFilter(s)) return;

		final HawkAttributeRemovalEvent ev = new HawkAttributeRemovalEvent();
		ev.setAttribute(attrName);
		ev.setId(elementNode.getId().toString());
		ev.setVcsItem(mapToThrift(s));

		final HawkChangeEvent change = new HawkChangeEvent();
		change.setModelElementAttributeRemoval(ev);
		sendEvent(change);
	}

	@Override
	public void referenceAddition(VcsCommitItem s, IGraphNode source,
			IGraphNode target, String refName, boolean isTransient) {
		if (isTransient || !isAcceptedByFilter(s)) return;

		final HawkReferenceAdditionEvent ev = new HawkReferenceAdditionEvent();
		ev.setSourceId(source.getId().toString());
		ev.setTargetId(target.getId().toString());
		ev.setVcsItem(mapToThrift(s));
		ev.setRefName(refName);

		final HawkChangeEvent change = new HawkChangeEvent();
		change.setReferenceAddition(ev);
		sendEvent(change);
	}

	@Override
	public void referenceRemoval(VcsCommitItem s, IGraphNode source,
			IGraphNode target, String refName, boolean isTransient) {
		if (isTransient || !isAcceptedByFilter(s)) return;

		final HawkReferenceRemovalEvent ev = new HawkReferenceRemovalEvent();
		ev.setSourceId(source.getId().toString());
		ev.setTargetId(target.getId().toString());
		ev.setVcsItem(mapToThrift(s));
		ev.setRefName(refName);

		final HawkChangeEvent change = new HawkChangeEvent();
		change.setReferenceRemoval(ev);
		sendEvent(change);
	}

	private boolean isAcceptedByFilter(VcsCommitItem s) {
		final VcsCommit commit = s.getCommit();
		final VcsRepositoryDelta delta = commit.getDelta();
		final VcsRepository repository = delta.getRepository();
		final String repositoryURL = repository.getUrl();
		return repositoryURIPattern.matcher(repositoryURL).matches()
				&& filePathPattern.matcher(s.getPath()).matches();
	}

	private CommitItem mapToThrift(VcsCommitItem s) {
		final VcsCommit commit = s.getCommit();
	
		final String repoURL = commit.getDelta().getRepository().getUrl();
		final String revision = commit.getRevision();
		final String path = s.getPath();
		final CommitItemChangeType changeType = mapToThrift(s.getChangeType());
	
		return new CommitItem(repoURL, revision, path, changeType);
	}

	private CommitItemChangeType mapToThrift(VcsChangeType changeType) {
		switch (changeType) {
		case ADDED: return CommitItemChangeType.ADDED;
		case DELETED: return CommitItemChangeType.DELETED;
		case REPLACED: return CommitItemChangeType.REPLACED;
		case UPDATED: return CommitItemChangeType.UPDATED;
		default: return CommitItemChangeType.UNKNOWN;
		}
	}

	private void sendEvent(HawkChangeEvent change) {
		try {
			final ClientMessage msg = session.createMessage(Message.BYTES_TYPE, messagesAreDurable);
			final TTransport trans = new ActiveMQBufferTransport(msg.getBodyBuffer());
			final TProtocol proto = protocolFactory.getProtocol(trans);
			change.write(proto);

			producer.send(msg);
		} catch (TException ex) {
			LOGGER.error("Serialization error", ex);
		} catch (ActiveMQException ex) {
			LOGGER.error("Error while sending event", ex);
		}
	}

	private void closeSession() {
		try {
			session.close();
		} catch (ActiveMQException e) {
			LOGGER.error("Could not close the session", e);
		} finally {
			session = null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((queueAddress == null) ? 0 : queueAddress.hashCode());
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
		ArtemisProducerGraphChangeListener other = (ArtemisProducerGraphChangeListener) obj;
		if (queueAddress == null) {
			if (other.queueAddress != null)
				return false;
		} else if (!queueAddress.equals(other.queueAddress))
			return false;
		return true;
	}
}
