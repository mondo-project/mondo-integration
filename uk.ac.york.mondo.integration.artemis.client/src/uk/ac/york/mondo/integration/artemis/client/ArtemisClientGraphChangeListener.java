package uk.ac.york.mondo.integration.artemis.client;

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
import org.hawk.core.VcsCommitItem;
import org.hawk.core.graph.IGraphChangeListener;
import org.hawk.core.graph.IGraphNode;
import org.hawk.core.model.IHawkClass;
import org.hawk.core.model.IHawkObject;
import org.hawk.core.model.IHawkPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hawk change listener that posts all changes to an Artemis in-VM server
 * through its core protocol. Transaction management is based on the
 * {@link TransactionalSendTest} test suite in Artemis.
 */
public class ArtemisClientGraphChangeListener implements IGraphChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArtemisClientGraphChangeListener.class);

	private final ServerLocator locator;
	private final ClientSessionFactory sessionFactory;
	private final String queueName;
	private final boolean durable;

	private ClientSession  session;
	private ClientProducer producer;

	public ArtemisClientGraphChangeListener(String queueName, boolean durable) throws Exception {
		this.locator = ActiveMQClient.createServerLocatorWithoutHA(
			new TransportConfiguration(InVMConnectorFactory.class.getName()));
		this.sessionFactory = locator.createSessionFactory();
		this.queueName = queueName;
		this.durable = durable;

		// Connect to Artemis, create the queue and disconnect
		ClientSession queueSession = sessionFactory.createSession(false, false, false);
		queueSession.createQueue(queueName, queueName, durable);
		queueSession.close();
	}

	@Override
	public String getName() {
		return "Artemis graph change listener";
	}

	@Override
	public void changeStart() {
		if (session == null) {
			try {
				this.session = sessionFactory.createSession(false, false, false);
				this.producer = session.createProducer(queueName);
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
		sendTextMessage("added class " + cls.getName());
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
	public void modelElementAddition(VcsCommitItem s, IHawkObject element,
			IGraphNode elementNode, boolean isTransient) {
		sendTextMessage("added model element " + element + " as node " + elementNode.getId());
	}

	@Override
	public void modelElementRemoval(VcsCommitItem s, IGraphNode elementNode, boolean isTransient) {
		sendTextMessage("removed model element node " + elementNode.getId());
	}

	@Override
	public void modelElementAttributeUpdate(VcsCommitItem s,
			IHawkObject eObject, String attrName, Object oldValue,
			Object newValue, IGraphNode elementNode, boolean isTransient) {
		sendTextMessage("updated field " + attrName + " in node " + elementNode.getId());
	}

	@Override
	public void modelElementAttributeRemoval(VcsCommitItem s,
			IHawkObject eObject, String attrName, IGraphNode elementNode,
			boolean isTransient) {
		sendTextMessage("removed field " + attrName + " in node " + elementNode.getId());
	}

	@Override
	public void referenceAddition(VcsCommitItem s, IGraphNode source,
			IGraphNode destination, String edgelabel, boolean isTransient) {
		sendTextMessage("added reference " + edgelabel + " in node " + source.getId());
	}

	@Override
	public void referenceRemoval(VcsCommitItem s, IGraphNode source,
			IGraphNode destination, String edgelabel, boolean isTransient) {
		sendTextMessage("removed reference " + edgelabel + " in node " + source.getId());
	}

	private void sendTextMessage(final String text) {
		try {
			final ClientMessage msg = session.createMessage(Message.TEXT_TYPE, durable);
			msg.writeBodyBufferString(text);
			producer.send(msg);
		} catch (ActiveMQException e) {
			LOGGER.error(String.format("Could not send message '%s'", text), e);
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

}
