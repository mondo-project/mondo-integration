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
package uk.ac.york.mondo.integration.hawk.emf.dt.editors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.transport.TTransportException;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.epsilon.common.dt.util.ListContentProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkInstance;
import uk.ac.york.mondo.integration.api.HawkState;
import uk.ac.york.mondo.integration.api.Repository;
import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.utils.APIUtils;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor.LoadingMode;
import uk.ac.york.mondo.integration.hawk.emf.dt.Activator;
import uk.ac.york.mondo.integration.hawk.emf.impl.HawkResourceFactoryImpl;
import uk.ac.york.mondo.integration.hawk.remote.thrift.ui.LazyCredentials;

/**
 * Editor for <code>.hawkmodel</code> files. The first page is a form-based UI
 * for editing the raw text on the second page.
 */
public class HawkMultiPageEditor extends FormEditor	implements IResourceChangeListener {

	private class DetailsFormPage extends FormPage {
		private InstanceSection instanceSection;
		private ContentSection contentSection;
		private SubscriptionSection subscriptionSection;

		private DetailsFormPage(String id, String title) {
			super(HawkMultiPageEditor.this, id, title);
		}

		@Override
		public boolean isEditor() {
			return true;
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			super.createFormContent(managedForm);
			managedForm.getForm().setText("Remote Hawk Descriptor");

			final FormToolkit toolkit = managedForm.getToolkit();
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 1;
			final Composite formBody = managedForm.getForm().getBody();
			formBody.setLayout(layout);

			final FormText formText = toolkit.createFormText(formBody, true);
			formText.setText("<form><p>"
					+ "<a href=\"reopenEcore\">Open with Exeed</a> "
					+ "<a href=\"copyShortURL\">Copy short URL to clipboard</a> "
					+ "<a href=\"copyLongURL\">Copy long URL to clipboard</a>"
					+ "</p></form>", true, true);
			formText.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					final String href = e.getHref().toString();
					switch (href) {
					case "reopenEcore":
						HawkMultiPageEditorContributor.reopenWithExeed(HawkMultiPageEditor.this);
						break;
					case "copyLongURL":
					case "copyShortURL":
						try {
							final String url = HawkResourceFactoryImpl.generateHawkURL(buildDescriptor(), "copyShortURL".equals(href));
							final Clipboard cb = new Clipboard(getSite().getShell().getDisplay());
							cb.setContents(new Object[]{url}, new Transfer[]{TextTransfer.getInstance()});
							cb.dispose();
						} catch (UnsupportedEncodingException ex) {
							Activator.getDefault().logError(ex);
						}
						break;
					}
				}
			});

			this.instanceSection = new InstanceSection(toolkit, formBody) {
				@Override protected void instanceNameChanged() { refreshRawText(); }
				@Override protected void serverURLChanged()    { refreshRawText(); }
				@Override protected void thriftProtocolChanged() { refreshRawText(); }
				@Override protected void usernameChanged() { refreshRawText(); }
				@Override protected void passwordChanged() { refreshRawText(); }
				@Override protected void selectInstance() {
					final HawkModelDescriptor d = buildDescriptor();
					try {
						Hawk.Client client = connectToHawk(d);
						final List<HawkInstance> instances = client.listInstances();
						Collections.sort(instances);
						client.getInputProtocol().getTransport().close();

						final Shell shell = formText.getShell();
						final ListDialog dlg = new ListDialog(shell);
						dlg.setInput(instances);
						dlg.setContentProvider(new ListContentProvider());
						dlg.setLabelProvider(new LabelProvider(){
							@Override
							public String getText(Object o) {
								if (o instanceof HawkInstance) {
									final HawkInstance hi = (HawkInstance)o;
									return hi.name;
								}
								return super.getText(o);
							}

							@Override
							public Image getImage(Object o) {
								if (o instanceof HawkInstance) {
									final HawkInstance hi = (HawkInstance) o;
									return Activator.getImageDescriptor(hi.state != HawkState.STOPPED
										? "/icons/nav_go.gif" : "/icons/nav_stop.gif")
										.createImage();
								}
								return super.getImage(o);
							}
						});
						dlg.setMessage("Select a Hawk instance:");
						dlg.setTitle("Hawk instance selection");
						if (dlg.open() == IDialogConstants.OK_ID) {
							final Object[] selected = dlg.getResult();
							if (selected.length > 0) {
								setInstanceName(((HawkInstance)selected[0]).name);
								instanceNameChanged();
							}
						}
					} catch (Exception ex) {
						Activator.getDefault().logError(ex);
					}
				}
			};
			this.contentSection = new ContentSection(toolkit, formBody) {
				@Override protected void filePatternsChanged()  { refreshRawText(); }
				@Override protected void repositoryURLChanged() { refreshRawText(); }
				@Override protected void loadingModeChanged() { refreshRawText(); }
				@Override protected void queryLanguageChanged() { refreshRawText(); }
				@Override protected void queryChanged() { refreshRawText(); }
				@Override protected void defaultNamespacesChanged() { refreshRawText(); }
				@Override protected void splitChanged() { refreshRawText(); }

				@Override protected void selectQueryLanguage() {
					final HawkModelDescriptor d = buildDescriptor();
					try {
						Hawk.Client client = connectToHawk(d);
						final String[] languages = client.listQueryLanguages(
								d.getHawkInstance()).toArray(new String[0]);
						client.getInputProtocol().getTransport().close();

						final Shell shell = formText.getShell();
						final ListDialog dlg = new ListDialog(shell);
						dlg.setInput(languages);
						dlg.setContentProvider(new ArrayContentProvider());
						dlg.setLabelProvider(new LabelProvider());
						dlg.setMessage("Select a query language:");
						dlg.setTitle("Query language selection");
						dlg.setInitialSelections(new String[]{d.getHawkQueryLanguage()});
						if (dlg.open() == IDialogConstants.OK_ID) {
							final Object[] selected = dlg.getResult();
							if (selected.length > 0) {
								setQueryLanguage(selected[0].toString());
							} else {
								setQueryLanguage("");
							}
							queryLanguageChanged();
						}
					} catch (Exception ex) {
						Activator.getDefault().logError(ex);
					}
				}

				@Override
				protected void selectRepository() {
					final HawkModelDescriptor d = buildDescriptor();
					try {
						Hawk.Client client = connectToHawk(d);
						List<Repository> repositories = client.listRepositories(d.getHawkInstance());
						final String[] repos = new String[1 + repositories.size()];
						repos[0] = "*";
						int iRepo = 1;
						for (Repository r : repositories) {
							repos[iRepo++] = r.uri;
						}
						Arrays.sort(repos);
						client.getInputProtocol().getTransport().close();

						final Shell shell = formText.getShell();
						final ListDialog dlg = new ListDialog(shell);
						dlg.setInput(repos);
						dlg.setContentProvider(new ArrayContentProvider());
						dlg.setLabelProvider(new LabelProvider());
						dlg.setMessage("Select a repository:");
						dlg.setTitle("Repository selection");
						dlg.setInitialSelections(new String[]{d.getHawkRepository()});
						if (dlg.open() == IDialogConstants.OK_ID) {
							final Object[] selected = dlg.getResult();
							if (selected.length > 0) {
								setRepositoryURL(selected[0].toString());
							} else {
								setRepositoryURL("*");
							}
							repositoryURLChanged();
						}
					} catch (Exception ex) {
						Activator.getDefault().logError(ex);
					}
				}

				@Override
				protected void selectFiles() {
					final HawkModelDescriptor d = buildDescriptor();
					try {
						Hawk.Client client = connectToHawk(d);
						final String[] files = client.listFiles(
								d.getHawkInstance(), Arrays.asList(d.getHawkRepository()),
								Arrays.asList("*")).toArray(new String[0]);
						Arrays.sort(files);
						client.getInputProtocol().getTransport().close();

						final Shell shell = formText.getShell();
						final ListSelectionDialog dlg = new ListSelectionDialog(
								shell, files, new ArrayContentProvider(),
								new LabelProvider(), "Select files (zero files = all files):");
						dlg.setTitle("File selection");
						dlg.setInitialSelections(d.getHawkFilePatterns());
						if (dlg.open() == IDialogConstants.OK_ID) {
							final Object[] selected = dlg.getResult();
							if (selected.length > 0) {
								final String[] sFiles = new String[selected.length];
								for (int i = 0; i < selected.length; i++) {
									sFiles[i] = selected[i].toString();
								}
								setFilePatterns(sFiles);
							} else {
								setFilePatterns("*");
							}
							filePatternsChanged();
						}
					} catch (Exception ex) {
						Activator.getDefault().logError(ex);
					}
				}
			};
			this.subscriptionSection = new SubscriptionSection(toolkit, formBody) {
				@Override protected void subscribeChanged() { refreshRawText(); }
				@Override protected void clientIDChanged() { refreshRawText(); }
				@Override protected void durabilityChanged() { refreshRawText(); }
			};
		}

		public InstanceSection getInstanceSection() {
			return instanceSection;
		}

		public ContentSection getContentSection() {
			return contentSection;
		}

		public SubscriptionSection getSubscriptionSection() {
			return subscriptionSection;
		}
	}

	/**
	 * Form section with the ability to mask modification notifications temporarily.
	 */
	private static abstract class FormSection {
		protected Composite cContents;

		public FormSection(FormToolkit toolkit, Composite parent, String title, String description) {
		    final Section sectionContent = toolkit.createSection(parent, Section.TITLE_BAR|Section.DESCRIPTION);
		    sectionContent.setText(title);
		    sectionContent.setDescription(description);
		    TableWrapData tdSectionContent = new TableWrapData(TableWrapData.FILL_GRAB);
		    sectionContent.setLayoutData(tdSectionContent);

		    this.cContents =  toolkit.createComposite(sectionContent, SWT.WRAP);
		    sectionContent.setClient(cContents);
		}
	}

	/**
	 * Paired label and text field.
	 */
	private static class FormTextField {
		private final FormText label;
		private final Text text;

		public FormTextField(FormToolkit toolkit, Composite sectionClient, String labelText, String defaultValue) {
			this(toolkit, sectionClient, labelText, defaultValue, SWT.BORDER);
		}

		public FormTextField(FormToolkit toolkit, Composite sectionClient, String labelText, String defaultValue, int textStyle) {
		    label = toolkit.createFormText(sectionClient, true);
		    label.setText("<form><p>" + labelText + "</p></form>", true, false);
		    label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		    final TableWrapData layoutData = new TableWrapData();
			layoutData.valign = TableWrapData.MIDDLE;
			label.setLayoutData(layoutData);

			text = toolkit.createText(sectionClient, defaultValue, textStyle);
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}

		public Text getText() {
			return text;
		}

		public void setTextWithoutListener(String newText, ModifyListener disabledListener) {
			text.removeModifyListener(disabledListener);
			text.setText(newText);
			text.addModifyListener(disabledListener);
		}

		public FormText getLabel() {
			return label;
		}
	}

	/**
	 * Combo box field that takes up two columns in the grid.
	 */
	private static class FormComboBoxField {
		private final Label label;
		private final Combo combobox;

		public FormComboBoxField(FormToolkit toolkit, Composite sectionClient, String labelText, String[] options) {
		    label = toolkit.createLabel(sectionClient, labelText, SWT.WRAP);
		    label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		    final TableWrapData layoutData = new TableWrapData();
			layoutData.valign = TableWrapData.MIDDLE;
			label.setLayoutData(layoutData);

			combobox = new Combo(sectionClient, SWT.READ_ONLY);
			combobox.setItems(options);
			combobox.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}

		public Combo getCombo() {
			return combobox;
		}
	}

	/**
	 * Paired label and checkbox field.
	 */
	private static class FormCheckBoxField {
		private final Label label;
		private final Button checkbox;

		public FormCheckBoxField(FormToolkit toolkit, Composite sectionClient, String labelText, boolean defaultValue) {
		    label = toolkit.createLabel(sectionClient, labelText, SWT.WRAP);
		    label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		    final TableWrapData layoutData = new TableWrapData();
			layoutData.valign = TableWrapData.MIDDLE;
			label.setLayoutData(layoutData);

			checkbox = toolkit.createButton(sectionClient, "", SWT.CHECK);
			checkbox.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			checkbox.setSelection(defaultValue);
		}

		public Button getCheck() {
			return checkbox;
		}
	}

	private static abstract class ContentSection extends FormSection implements ModifyListener, SelectionListener {
		private final FormTextField fldFilePatterns;
		private final FormTextField fldRepositoryURL;
		private final FormComboBoxField fldLoadingMode;
		private final FormTextField fldQueryLanguage;
		private final FormTextField fldQuery;
		private final FormTextField fldDefaultNamespaces;
		private final FormCheckBoxField fldSplit;

		public ContentSection(FormToolkit toolkit, Composite parent) {
			super(toolkit, parent, "Contents", "Filters on the contents of the index to be read as a model");
		    cContents.setLayout(createTableWrapLayout(2));

		    this.fldRepositoryURL = new FormTextField(toolkit, cContents, "<a href=\"selectRepository\">Repository URL</a>:", HawkModelDescriptor.DEFAULT_REPOSITORY);
		    this.fldFilePatterns = new FormTextField(toolkit, cContents, "<a href=\"selectFiles\">File pattern(s)</a>:", HawkModelDescriptor.DEFAULT_FILES);
		    this.fldLoadingMode = new FormComboBoxField(toolkit, cContents, "Loading mode:", HawkModelDescriptor.LoadingMode.strings());
		    this.fldQueryLanguage = new FormTextField(toolkit, cContents, "<a href=\"selectQueryLanguage\">Query language</a>:", HawkModelDescriptor.DEFAULT_QUERY_LANGUAGE);
		    this.fldQuery = new FormTextField(toolkit, cContents, "Query:", HawkModelDescriptor.DEFAULT_QUERY);
		    this.fldDefaultNamespaces = new FormTextField(toolkit, cContents, "Default namespaces:", HawkModelDescriptor.DEFAULT_DEFAULT_NAMESPACES);
		    this.fldSplit = new FormCheckBoxField(toolkit, cContents, "Split by file:", HawkModelDescriptor.DEFAULT_IS_SPLIT);

		    this.fldRepositoryURL.getText().setToolTipText(
		        "Pattern for the URL repositories to be fetched (* means 0+ arbitrary characters).");
		    this.fldFilePatterns.getText().setToolTipText(
		        "Comma-separated patterns for the files repositories to be fetched (* means 0+ arbitrary characters).");
		    this.fldQueryLanguage.getText().setToolTipText(
		        "Language in which the query will be written. If empty, the entire model will be retrieved.");
		    this.fldQuery.getText().setToolTipText(
		        "Query to be used for the initial contents of the model. If empty, the entire model will be retrieved.");
		    this.fldDefaultNamespaces.getText().setToolTipText(
			"Comma-separated list of namespaces used to disambiguate types if multiple matches are found.");
		    this.fldSplit.getCheck().setToolTipText(
		    	"If checked, the contents of the index will be split by file, using surrogate resources. Otherwise, the entire contents of the index will be under this resource (needed for CloudATL).");

		    fldRepositoryURL.getText().addModifyListener(this);
		    fldFilePatterns.getText().addModifyListener(this);
		    fldLoadingMode.getCombo().addSelectionListener(this);
		    fldQueryLanguage.getText().addModifyListener(this);
		    fldQuery.getText().addModifyListener(this);
		    fldDefaultNamespaces.getText().addModifyListener(this);
		    fldSplit.getCheck().addSelectionListener(this);

		    final HyperlinkAdapter hyperlinkListener = new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					switch (e.getHref().toString()) {
					case "selectQueryLanguage":
						selectQueryLanguage();
						break;
					case "selectRepository":
						selectRepository();
						break;
					case "selectFiles":
						selectFiles();
						break;
					}
				}
			};
			this.fldQueryLanguage.getLabel().addHyperlinkListener(hyperlinkListener);
			this.fldRepositoryURL.getLabel().addHyperlinkListener(hyperlinkListener);
			this.fldFilePatterns.getLabel().addHyperlinkListener(hyperlinkListener);
		}

		public String[] getFilePatterns() {
			return fldFilePatterns.getText().getText().split(",");
		}

		public String getRepositoryURL() {
			return fldRepositoryURL.getText().getText();
		}

		public LoadingMode getLoadingMode() {
			return LoadingMode.values()[fldLoadingMode.getCombo().getSelectionIndex()];
		}

		public String getQueryLanguage() {
			return fldQueryLanguage.getText().getText().trim();
		}

		public String getQuery() {
			return fldQuery.getText().getText();
		}

		public boolean isSplit() {
			return fldSplit.getCheck().getSelection();
		}

		public String getDefaultNamespaces() {
			return fldDefaultNamespaces.getText().getText();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == fldLoadingMode.getCombo()) {
				loadingModeChanged();
			} else if (e.widget == fldSplit.getCheck()) {
				splitChanged();
			}
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (e.widget == fldRepositoryURL.getText()) {
				repositoryURLChanged();
			} else if (e.widget == fldFilePatterns.getText()) {
				filePatternsChanged();
			} else if (e.widget == fldQueryLanguage.getText()) {
				queryLanguageChanged();
			} else if (e.widget == fldQuery.getText()) {
				queryChanged();
			} else if (e.widget == fldDefaultNamespaces.getText()) {
				defaultNamespacesChanged();
			}
		}

		public void setFilePatterns(String... patterns) {
			fldFilePatterns.setTextWithoutListener(HawkMultiPageEditor.concat(patterns, ","), this);
		}

		public void setRepositoryURL(String url) {
			fldRepositoryURL.setTextWithoutListener(url, this);
		}

		public void setLoadingMode(LoadingMode lazy) {
			fldLoadingMode.getCombo().select(lazy.ordinal());
		}

		public void setQueryLanguage(String queryLanguage) {
			fldQueryLanguage.setTextWithoutListener(queryLanguage, this);
		}

		public void setQuery(String query) {
			fldQuery.setTextWithoutListener(query, this);
		}

		public void setSplit(boolean isSplit) {
			fldSplit.getCheck().setSelection(isSplit);
		}

		public void setDefaultNamespaces(String defaultNS) {
			fldDefaultNamespaces.setTextWithoutListener(defaultNS, this);
		}

		protected abstract void filePatternsChanged();
		protected abstract void repositoryURLChanged();
		protected abstract void loadingModeChanged();
		protected abstract void queryLanguageChanged();
		protected abstract void queryChanged();
		protected abstract void defaultNamespacesChanged();
		protected abstract void splitChanged();

		protected abstract void selectQueryLanguage();
		protected abstract void selectRepository();
		protected abstract void selectFiles();
	}

	private static abstract class InstanceSection extends FormSection implements ModifyListener, SelectionListener {
		private final FormTextField fldInstanceName;
		private final FormTextField fldServerURL;
		private final FormComboBoxField fldTProtocol;
		private final FormTextField fldUsername;
		private final FormTextField fldPassword;

		public InstanceSection(FormToolkit toolkit, Composite parent) {
			super(toolkit, parent, "Instance", "Access details for the remote Hawk instance.");
		    cContents.setLayout(createTableWrapLayout(2));

		    this.fldServerURL = new FormTextField(toolkit, cContents, "Server URL:", "");
		    this.fldTProtocol = new FormComboBoxField(toolkit, cContents, "Thrift protocol:", ThriftProtocol.strings());
		    this.fldInstanceName = new FormTextField(toolkit, cContents, "<a href=\"selectInstance\">Instance name</a>:", "");
		    this.fldUsername = new FormTextField(toolkit, cContents, "Username:", "");
		    this.fldPassword = new FormTextField(toolkit, cContents, "Password:", "", SWT.BORDER | SWT.PASSWORD);

		    fldServerURL.getText().addModifyListener(this);
		    fldInstanceName.getText().addModifyListener(this);
		    fldTProtocol.getCombo().addSelectionListener(this);
		    fldUsername.getText().addModifyListener(this);
		    fldPassword.getText().addModifyListener(this);

		    fldUsername.getText().setToolTipText(
		    	"Username to be included in the .hawkmodel file, to log "
			+ "into the Hawk Thrift API. To use the Eclipse secure storage "
			+ "instead, keep blank.");
		    fldPassword.getText().setToolTipText(
			"Plaintext password to be included in the .hawkmodel file, to log "
			+ "into the Hawk Thrift API. To use the Eclipse secure storage "
			+ "instead, keep blank.");

		    fldInstanceName.getLabel().addHyperlinkListener(new HyperlinkAdapter() {
		    	public void linkActivated(HyperlinkEvent e) {
		    		switch (e.getHref().toString()) {
		    		case "selectInstance":
		    			selectInstance();
		    			break;
		    		}
		    	}
		    });
		}

		public String getInstanceName() {
			return fldInstanceName.getText().getText();
		}

		public String getServerURL() {
			return fldServerURL.getText().getText();
		}

		public ThriftProtocol getThriftProtocol() {
			return ThriftProtocol.values()[fldTProtocol.getCombo().getSelectionIndex()];
		}

		public void setInstanceName(String name) {
			fldInstanceName.setTextWithoutListener(name, this);
		}

		public void setServerURL(String url) {
			fldServerURL.setTextWithoutListener(url, this);
		}

		public void setThriftProtocol(ThriftProtocol t) {
			fldTProtocol.getCombo().select(t.ordinal());
		}

		public String getUsername() {
			return fldUsername.getText().getText();
		}

		public void setUsername(String u) {
			fldUsername.setTextWithoutListener(u, this);
		}

		public String getPassword() {
			return fldPassword.getText().getText();
		}

		public void setPassword(String p) {
			fldPassword.setTextWithoutListener(p, this);
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (e.widget == fldServerURL.getText()) {
				serverURLChanged();
			} else if (e.widget == fldInstanceName.getText()) {
				instanceNameChanged();
			} else if (e.widget == fldUsername.getText()) {
				usernameChanged();
			} else if (e.widget == fldPassword.getText()) {
				passwordChanged();
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == fldTProtocol.getCombo()) {
				thriftProtocolChanged();
			}
		}

		protected abstract void instanceNameChanged();
		protected abstract void serverURLChanged();
		protected abstract void thriftProtocolChanged();
		protected abstract void usernameChanged();
		protected abstract void passwordChanged();
		protected abstract void selectInstance();
	}

	private static abstract class SubscriptionSection extends FormSection implements SelectionListener, ModifyListener {
		private final FormCheckBoxField fldSubscribe;
		private final FormTextField fldClientID;
		private final FormComboBoxField fldDurability;

		public SubscriptionSection(FormToolkit toolkit, Composite parent) {
			super(toolkit, parent, "Subscription", "Configuration parameters for subscriptions to changes in the models indexed by Hawk.");
		    cContents.setLayout(createTableWrapLayout(2));

		    this.fldSubscribe = new FormCheckBoxField(toolkit, cContents, "Subscribe:", HawkModelDescriptor.DEFAULT_IS_SUBSCRIBED);
		    this.fldClientID = new FormTextField(toolkit, cContents, "Client ID:", HawkModelDescriptor.DEFAULT_CLIENTID);
		    this.fldDurability = new FormComboBoxField(toolkit, cContents, "Durability:", toStringArray(SubscriptionDurability.values()));

		    fldSubscribe.getCheck().addSelectionListener(this);
		    fldClientID.getText().addModifyListener(this);
		    fldDurability.getCombo().addSelectionListener(this);
		}

		private String[] toStringArray(Object[] values) {
			final String[] sArray = new String[values.length];

			int i = 0;
			for (Object v : values) {
				sArray[i++] = v + "";
			}
			return sArray;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == fldSubscribe.getCheck()) {
				subscribeChanged();
			} else if (e.widget == fldDurability.getCombo()) {
				durabilityChanged();
			}
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (e.widget == fldClientID.getText()) {
				clientIDChanged();
			}
		}

		public boolean isSubscribed() {
			return fldSubscribe.getCheck().getSelection();
		}

		public void setSubscribed(boolean subscribed) {
			fldSubscribe.getCheck().setSelection(subscribed);
		}

		public String getClientID() {
			return fldClientID.getText().getText();
		}

		public void setClientID(String s) {
			fldClientID.setTextWithoutListener(s, this);
		}

		public SubscriptionDurability getDurability() {
			return SubscriptionDurability.values()[fldDurability.getCombo().getSelectionIndex()];
		}

		public void setDurability(SubscriptionDurability s) {
			fldDurability.getCombo().select(s.ordinal());
		}

		protected abstract void subscribeChanged();
		protected abstract void clientIDChanged();
		protected abstract void durabilityChanged();
	}

	private static final int RAW_EDITOR_PAGE_INDEX = 1;

	private static String concat(final String[] elems, final String separator) {
		final StringBuffer sbuf = new StringBuffer();
		boolean bFirst = true;
		for (String filePattern : elems) {
			if (bFirst) {
				bFirst = false;
			} else {
				sbuf.append(separator);
			}
			sbuf.append(filePattern);
		}
		return sbuf.toString();
	}

	private static TableWrapLayout createTableWrapLayout(int nColumns) {
		final TableWrapLayout cContentsLayout = new TableWrapLayout();
	    cContentsLayout.numColumns = nColumns;
	    cContentsLayout.horizontalSpacing = 5;
	    cContentsLayout.verticalSpacing = 3;
		return cContentsLayout;
	}

	/** The text editor used in page 0. */
	private TextEditor editor;
	private DetailsFormPage detailsPage;
	private IDocumentListener documentListener;

	public HawkMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(RAW_EDITOR_PAGE_INDEX).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page {@link #RAW_EDITOR_PAGE_INDEX}'s tab, and updates this
	 * multi-page editor's input to correspond to the nested editor's.
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(RAW_EDITOR_PAGE_INDEX);
		editor.doSaveAs();
		setPageText(RAW_EDITOR_PAGE_INDEX, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		}
		super.init(site, editorInput);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Closes all project files on project close.
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput()).getFile().getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	private void createFormBasedEditorPage() throws PartInitException {
		detailsPage = new DetailsFormPage("details", "Remote Hawk Model Descriptor");
		int index = addPage(detailsPage, getEditorInput());
		setPageText(index, "Descriptor");
	}

	private void createRawTextEditorPage() throws PartInitException {
		editor = new TextEditor();
		int rawEditorPage = addPage(editor, getEditorInput());
		setPageText(rawEditorPage, editor.getTitle());
		setPartName(editor.getTitle());
	}

	private IDocument getDocument() {
		return editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	private void refreshForm() {
		final IDocument doc = getDocument();
		final String sContents = doc.get();

		final HawkModelDescriptor descriptor = new HawkModelDescriptor();
		try {
			descriptor.load(new StringReader(sContents));

			detailsPage.getInstanceSection().setServerURL(descriptor.getHawkURL());
			detailsPage.getInstanceSection().setInstanceName(descriptor.getHawkInstance());
			detailsPage.getInstanceSection().setThriftProtocol(descriptor.getThriftProtocol());
			detailsPage.getInstanceSection().setUsername(descriptor.getUsername());
			detailsPage.getInstanceSection().setPassword(descriptor.getPassword());
			detailsPage.getContentSection().setRepositoryURL(descriptor.getHawkRepository());
			detailsPage.getContentSection().setFilePatterns(descriptor.getHawkFilePatterns());
			detailsPage.getContentSection().setLoadingMode(descriptor.getLoadingMode());
			detailsPage.getContentSection().setQueryLanguage(descriptor.getHawkQueryLanguage());
			detailsPage.getContentSection().setQuery(descriptor.getHawkQuery());
			detailsPage.getContentSection().setDefaultNamespaces(descriptor.getDefaultNamespaces());
			detailsPage.getContentSection().setSplit(descriptor.isSplit());
			detailsPage.getSubscriptionSection().setSubscribed(descriptor.isSubscribed());
			detailsPage.getSubscriptionSection().setClientID(descriptor.getSubscriptionClientID());
			detailsPage.getSubscriptionSection().setDurability(descriptor.getSubscriptionDurability());
		} catch (IOException e) {
			Activator.getDefault().logError(e);
		}
	}

	private void refreshRawText() {
		final HawkModelDescriptor descriptor = buildDescriptor();
		final StringWriter sW = new StringWriter();
		try {
			descriptor.save(sW);

			final IDocument doc = getDocument();
			doc.removeDocumentListener(documentListener);
			doc.set(sW.toString());
			doc.addDocumentListener(documentListener);
		} catch (IOException e) {
			Activator.getDefault().logError(e);
		}
	}

	protected HawkModelDescriptor buildDescriptor() {
		final HawkModelDescriptor descriptor = new HawkModelDescriptor();
		descriptor.setHawkURL(detailsPage.getInstanceSection().getServerURL());
		descriptor.setHawkInstance(detailsPage.getInstanceSection().getInstanceName());
		descriptor.setThriftProtocol(detailsPage.getInstanceSection().getThriftProtocol());
		descriptor.setUsername(detailsPage.getInstanceSection().getUsername());
		descriptor.setPassword(detailsPage.getInstanceSection().getPassword());
		descriptor.setHawkRepository(detailsPage.getContentSection().getRepositoryURL());
		descriptor.setHawkFilePatterns(detailsPage.getContentSection().getFilePatterns());
		descriptor.setLoadingMode(detailsPage.getContentSection().getLoadingMode());
		descriptor.setSubscribed(detailsPage.getSubscriptionSection().isSubscribed());
		descriptor.setSubscriptionClientID(detailsPage.getSubscriptionSection().getClientID());
		descriptor.setSubscriptionDurability(detailsPage.getSubscriptionSection().getDurability());
		descriptor.setHawkQueryLanguage(detailsPage.getContentSection().getQueryLanguage());
		descriptor.setHawkQuery(detailsPage.getContentSection().getQuery());
		descriptor.setDefaultNamespaces(detailsPage.getContentSection().getDefaultNamespaces());
		descriptor.setSplit(detailsPage.getContentSection().isSplit());
		return descriptor;
	}

	@Override
	protected void addPages() {
		try {
			createFormBasedEditorPage();
			createRawTextEditorPage();
			documentListener = new IDocumentListener() {
				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					// ignore
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					refreshForm();
				}
			};
			getDocument().addDocumentListener(documentListener);
			refreshForm();
		} catch (Exception ex) {
			Activator.getDefault().logError(ex);
		}
	}

	protected Hawk.Client connectToHawk(final HawkModelDescriptor d) throws TTransportException {
		return APIUtils.connectTo(Hawk.Client.class,
				d.getHawkURL(), d.getThriftProtocol(),
				new LazyCredentials(d.getHawkURL()));
	}
}
