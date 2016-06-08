package org.mondo.collaboration.offline.management.client.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ViewPart;

import uk.ac.york.mondo.integration.api.GoldRepoNotFound;
import uk.ac.york.mondo.integration.api.OfflineCollaboration;
import uk.ac.york.mondo.integration.api.OfflineCollaboration.Client;
import uk.ac.york.mondo.integration.api.OfflineCollaborationInternalError;
import uk.ac.york.mondo.integration.api.UnauthorizedRepositoryOperation;
import uk.ac.york.mondo.integration.api.dt.prefs.CredentialsStore;
import uk.ac.york.mondo.integration.api.dt.prefs.CredentialsStore.Credentials;
import uk.ac.york.mondo.integration.api.dt.prefs.Server;
import uk.ac.york.mondo.integration.api.dt.prefs.ServerStore;
import uk.ac.york.mondo.integration.api.utils.APIUtils;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;

@SuppressWarnings("deprecation")
public class MONDOServerView extends ViewPart {

	private static final String CUSTOM_URL_TEXT = "Custom URL...";
	public static final String ID = "org.mondo.collaboration.offline.management.client.ui.MONDOView"; //$NON-NLS-1$
	private Text frontRepoURL;
	private Text userName;
	private Text customURL;

	private static Logger logger = Logger.getLogger(MONDOServerView.class);
	private Credentials credentials;
	private Text goldRepositoryText;

	public MONDOServerView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		Composite userDataComposite = new Composite(parent, SWT.NONE);
		userDataComposite.setLayout(new GridLayout(1, false));
		
		Label lblGoldRepositoryName = new Label(userDataComposite, SWT.NONE);
		lblGoldRepositoryName.setText("Enter gold repository URL:");
		
		goldRepositoryText = new Text(userDataComposite, SWT.BORDER);
		goldRepositoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label_1 = new Label(userDataComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		Label lblUserSpecificDetails = new Label(userDataComposite, SWT.NONE);
		lblUserSpecificDetails.setText("User specific details:");

		Button btnGetMyFront = new Button(userDataComposite, SWT.NONE);
		btnGetMyFront.setSize(111, 31);

		btnGetMyFront.setText("Get username and front URL");

		Label lblUser = new Label(userDataComposite, SWT.NONE);
		lblUser.setSize(166, 17);
		lblUser.setText("Username set in preferences:");

		userName = new Text(userDataComposite, SWT.BORDER);
		userName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		userName.setSize(54, 27);
		userName.setEditable(false);

		Label lblFrontRepoUrl = new Label(userDataComposite, SWT.NONE);
		lblFrontRepoUrl.setSize(86, 17);
		lblFrontRepoUrl.setText("Front repo URL:");

		frontRepoURL = new Text(userDataComposite, SWT.BORDER);
		frontRepoURL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		frontRepoURL.setSize(54, 27);
		frontRepoURL.setEditable(false);

		Composite serversComposite = new Composite(parent, SWT.NONE);
		serversComposite.setLayout(new GridLayout(1, false));

		Label lblListOfMondo = new Label(serversComposite, SWT.NONE);
		lblListOfMondo.setSize(128, 17);
		lblListOfMondo.setText("List of MONDO servers");

		final List list = new List(serversComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Button btnReloadListOf = new Button(serversComposite, SWT.NONE);
		btnReloadListOf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadListOfServers(list);
			}
		});
		btnReloadListOf.setText("Reload list of servers");

		Label lblYouAnAdd = new Label(serversComposite, SWT.WRAP | SWT.LEFT);
		lblYouAnAdd.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1));
		lblYouAnAdd.setText(
				"You can add further servers under the Mondo servers preference page, or connect to a given URL here:");

		customURL = new Text(serversComposite, SWT.BORDER);
		customURL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		loadListOfServers(list);

		userName.setEnabled(false);

		Label label = new Label(userDataComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Label lblGlobalActions = new Label(userDataComposite, SWT.NONE);
		lblGlobalActions.setText("Other actions:");

		Button btnOpenOnlineCollaboration = new Button(userDataComposite, SWT.NONE);
		btnOpenOnlineCollaboration.setText("Open online collaboration login screen in browser");
		btnOpenOnlineCollaboration.addSelectionListener(createOnlineCollaborationListener(list));

		Button btnResetFromGold = new Button(userDataComposite, SWT.NONE);
		btnResetFromGold.setSize(129, 31);

		btnResetFromGold.setText("Reset all repositories based on gold");

		btnResetFromGold.addSelectionListener(createResetListener(list));
		list.addSelectionListener(createListSelectionListener(list));

		btnGetMyFront.addSelectionListener(createGetFrontListener(list));

		createActions();
		initializeToolBar();
		initializeMenu();
	}
	
	private OfflineCollaboration.Client createClient(List list) {
		String[] selection = list.getSelection();
		Credentials credentials;
		String managementURL = getManagementURL(selection);

		URI url;
		try {
			url = new URI(managementURL);
			String scheme = url.getScheme();
			if(scheme == null ){
				MessageBox uriErrorBox = new MessageBox(getSite().getShell());
				uriErrorBox.setText("Server URI malformed");
				uriErrorBox.setMessage("Missing URI scheme (e.g. http://, https://)");			
				logger.log(Priority.ERROR,"Missing URI scheme (e.g. http://, https://)");				
				return null;
			}
		} catch (URISyntaxException e1) {
			MessageBox uriErrorBox = new MessageBox(getSite().getShell());
			uriErrorBox.setText("Server URI malformed");
			uriErrorBox.setMessage(e1.getMessage());			
			logger.log(Priority.ERROR, e1.getMessage());
			return null;
		}
		credentials = loadCredentials(selection);
		
		Client client = null;
		try {
			client = APIUtils.connectTo(OfflineCollaboration.Client.class, managementURL, ThriftProtocol.JSON, credentials.getUsername(), credentials.getPassword());
		} catch (TTransportException e) {
			MessageBox transportErrorBox = new MessageBox(getSite().getShell());
			transportErrorBox.setText("Transport exception occured");
			transportErrorBox.setMessage(e.getMessage());
			logger.log(Priority.ERROR, e.getMessage());
		} catch (URISyntaxException e) {
			MessageBox uriErrorBox = new MessageBox(getSite().getShell());
			uriErrorBox.setText("Server URI malformed");
			uriErrorBox.setMessage(e.getMessage());			
			logger.log(Priority.ERROR, e.getMessage());
		}
		
		return client;
	}

	private Credentials loadCredentials(String[] selection) {
		if (selection != null && selection.length > 0) {
			credentials = getCredentials(selection[0]);
		} else {
			credentials = new Credentials("", "");
		}
		
		if (selection[0].equals(CUSTOM_URL_TEXT)) {
			SimpleCredentialsDialog credentialsDialog = new SimpleCredentialsDialog(getSite().getShell());
			int open = credentialsDialog.open();
			if(open == IDialogConstants.OK_ID){
				String user = credentialsDialog.getUser();
				String password = credentialsDialog.getPassword();
				credentials = new Credentials(user, password);
			}
		}
		return credentials;
	}

	private String getManagementURL(String[] selection) {
		String managementURL;
		if (selection != null && selection.length > 0) {
			managementURL = selection[0];
		} else {
			managementURL = "";
		}
		if (managementURL.equals(CUSTOM_URL_TEXT)) {
			managementURL = customURL.getText();
		}
		return managementURL;
	}

	private SelectionAdapter createGetFrontListener(final List list) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Client client = createClient(list);

				String myFrontRepositoryURL;
				String shortError = null;
				String message = null;
				try {
					myFrontRepositoryURL = client.getMyFrontRepositoryURL(goldRepositoryText.getText());
					
					String managementURL = getManagementURL(list.getSelection());

					URI url = new URI(managementURL);
					String scheme = url.getScheme();
					String host = url.getHost();
//					int port = url.getPort();
					
					
					frontRepoURL.setText(scheme + "://" + host + "/svn/"+myFrontRepositoryURL);
					Credentials credentials = MONDOServerView.this.credentials;
					userName.setText(credentials.getUsername());
				} catch (GoldRepoNotFound e1) {
					shortError = "Gold Repository Not Found";
					message = e1.getMessage() == null ? shortError : e1.getMessage();
				} catch (UnauthorizedRepositoryOperation e1) {
					shortError = "Unauthorized Repository Operation";
					message = e1.getMessage() == null ? shortError : e1.getMessage();
				} catch (OfflineCollaborationInternalError e1) {
					shortError = "Offline Collaboration Internal Error";
					message = e1.getMessage() == null ? shortError : e1.getMessage();
				} catch (TException e1) {
					shortError = "Transport Error";
					message = e1.getMessage() == null ? shortError : e1.getMessage();
				} catch (NullPointerException e1) {
					shortError = "Unable To Connect To Server";
					message = e1.getMessage() == null ? "Unable To Connect To Server. Make sure that the URL is correct." : e1.getMessage();
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					if(shortError != null || message != null){
						MessageBox mb = new MessageBox(getSite().getShell());
						mb.setText(shortError);
						mb.setMessage(message);
						mb.open();
						logger.error(message);
					}
				}
			}
		};
	}

	private SelectionListener createListSelectionListener(final List list) {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (String url : list.getSelection()) {
					if (CUSTOM_URL_TEXT.equals(url)) {
						userName.setEnabled(false);
						userName.setText("");
						frontRepoURL.setText("");
						return;
					}
				}
				userName.setEnabled(true);
				frontRepoURL.setText("");
				userName.setText("");
				return;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
	}



	private SelectionAdapter createResetListener(final List list) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Client client = createClient(list);

		
				try {
					client.regenerateFrontRepositories(goldRepositoryText.getText());
				} catch (Exception e1) {
					String message = "Error occured while regenerating";
					logger.error(message);
					MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
					messageBox.setText("MONDO server management error");
					messageBox.setMessage(message);
					messageBox.open();
				}
				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
				messageBox.setText("Success.");
				messageBox.setMessage("Rpository regeneration command issued.");
				messageBox.open();
			}

		};
	}

	private SelectionAdapter createOnlineCollaborationListener(final List list) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Client client = createClient(list);
				
				String managementURL = getManagementURL(list.getSelection());
				try {
					String rapPath = client.getOnlineCollaborationURL(goldRepositoryText.getText());
					URL u = new URL(managementURL);
					System.out.println(u.getHost());
					String rapURL = u.getProtocol() + "://" + u.getHost() + ":" + u.getPort() + rapPath;
					final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
							IWorkbenchBrowserSupport.AS_VIEW | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null,
							null);
					browser.openURL(new URL(rapURL));
				} catch (Exception e1) {
					String message = "Could not get online collaboration URL from server " + managementURL;
					logger.error(message);
					MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
					mb.setText("MONDO server management error");
					mb.setMessage(message);
					mb.open();
				}
			}
		};
	}

	private void loadListOfServers(List list) {
		ServerStore serverStore = new ServerStore(
				uk.ac.york.mondo.integration.api.dt.Activator.getDefault().getPreferenceStore());
		java.util.List<Server> servers = serverStore.readAllServers();

		list.removeAll();
		
		list.add(CUSTOM_URL_TEXT);

		list.setSelection(0); // by default select option "Custom URL"

		for (Server server : servers) {
			list.add(server.getBaseURL());
		}
		userName.setEnabled(false);
		userName.setText("");
		frontRepoURL.setText("");
	}

	private Credentials getCredentials(String serverURL) {
		CredentialsStore.Credentials creds = null;

		if (CUSTOM_URL_TEXT.equals(serverURL)) {
			return new Credentials("", "");
		}

		ServerStore serverStore = new ServerStore(
				uk.ac.york.mondo.integration.api.dt.Activator.getDefault().getPreferenceStore());
		java.util.List<Server> servers = serverStore.readAllServers();

		for (Server server : servers) {
			if (serverURL.equals(server.getBaseURL())) {
				try {
					creds = uk.ac.york.mondo.integration.api.dt.Activator.getDefault().getCredentialsStore()
							.get(server.getBaseURL());
				} catch (Exception e1) {
					logger.warn("Could not load credentials for server with URL " + serverURL);
				}

			}
		}

		return creds;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		@SuppressWarnings("unused")
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		@SuppressWarnings("unused")
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public class SimpleCredentialsDialog extends Dialog {
		  private Text userText;
		  private Text passwordText;
		  private String user = "";
		  private String password = "";

		  public SimpleCredentialsDialog(Shell parentShell) {
		    super(parentShell);
		  }

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			GridLayout layout = new GridLayout(2, false);
			container.setLayout(layout);

			Label userLabel = new Label(container, SWT.NONE);
			userLabel.setText("Username:");

			userText = new Text(container, SWT.BORDER);
			userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			userText.setText(user);

			Label passwordLabel = new Label(container, SWT.NONE);
			passwordLabel.setText("Password:");

			passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
			passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			passwordText.setText(password);

			return container;
		}

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		@Override
		protected void okPressed() {
			user = userText.getText();
			password = passwordText.getText();
			super.okPressed();
		}

	}
	

	
}
