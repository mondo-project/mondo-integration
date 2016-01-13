package uk.ac.york.mondo.integration.hawk.remote.thrift.ui;

import java.security.Principal;

import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.hawk.core.ICredentialsStore;
import org.hawk.osgiserver.HManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the Hawk credentials store to provide HTTP auth credentials. If needed, it
 * will show a username and password dialog for entering the username/password.
 */
public class LazyCredentials implements Credentials {

	private final class CredentialsPrompter implements Runnable {
		private final Display display;
		private org.hawk.core.ICredentialsStore.Credentials creds;

		private CredentialsPrompter(Display display) {
			this.display = display;
		}

		@Override
		public void run() {
			UsernamePasswordDialog dlg = new UsernamePasswordDialog(display.getActiveShell(), storeKey);
			if (dlg.open() == Dialog.OK) {
				creds = new org.hawk.core.ICredentialsStore.Credentials(dlg.getUsername(), dlg.getPassword());
				try {
					credStore.put(storeKey, creds);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		public org.hawk.core.ICredentialsStore.Credentials getCredentials() {
			return creds;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(LazyCredentials.class);
	private final String storeKey;
	private final ICredentialsStore credStore;
	private Principal principal;
	private String password;

	public LazyCredentials(String storeKey) {
		this(storeKey, HManager.getInstance().getCredentialsStore());
	}

	public LazyCredentials(String storeKey, ICredentialsStore credStore) {
		this.storeKey = storeKey;
		this.credStore = credStore;
	}

	@Override
	public Principal getUserPrincipal() {
		if (principal == null) {
			getCredentials();
		}
		return principal;
	}

	@Override
	public String getPassword() {
		if (password == null) {
			getCredentials();
		}
		return password;
	}

	protected void getCredentials() {
		try {
			org.hawk.core.ICredentialsStore.Credentials creds = credStore.get(storeKey);
			if (creds == null && PlatformUI.isWorkbenchRunning()) {
				final Display display = PlatformUI.getWorkbench().getDisplay();
				final CredentialsPrompter prompter = new CredentialsPrompter(display);
				display.syncExec(prompter);
				creds = prompter.getCredentials();
			}
			if (creds != null) {
				principal = new BasicUserPrincipal(creds.getUsername());
				password = creds.getPassword();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

}
