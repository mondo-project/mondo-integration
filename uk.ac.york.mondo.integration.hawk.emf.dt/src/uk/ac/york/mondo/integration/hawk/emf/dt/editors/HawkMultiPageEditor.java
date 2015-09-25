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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
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

import uk.ac.york.mondo.integration.api.SubscriptionDurability;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor.LoadingMode;
import uk.ac.york.mondo.integration.hawk.emf.HawkResourceFactoryImpl;
import uk.ac.york.mondo.integration.hawk.emf.dt.Activator;

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
			};
			this.contentSection = new ContentSection(toolkit, formBody) {
				@Override protected void filePatternsChanged()  { refreshRawText(); }
				@Override protected void repositoryURLChanged() { refreshRawText(); }
				@Override protected void loadingModeChanged() { refreshRawText(); }
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
		private final Label label;
		private final Text text;

		public FormTextField(FormToolkit toolkit, Composite sectionClient, String labelText, String defaultValue) {
		    label = toolkit.createLabel(sectionClient, labelText, SWT.WRAP);
		    label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		    final TableWrapData layoutData = new TableWrapData();
			layoutData.valign = TableWrapData.MIDDLE;
			label.setLayoutData(layoutData);

			text = toolkit.createText(sectionClient, defaultValue, SWT.BORDER);
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}

		public Text getText() {
			return text;
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
		}

		public Button getCheck() {
			return checkbox;
		}
	}

	private static abstract class ContentSection extends FormSection implements ModifyListener, SelectionListener {
		private final FormTextField fldFilePatterns;
		private final FormTextField fldRepositoryURL;
		private final FormComboBoxField fldLoadingMode;

		public ContentSection(FormToolkit toolkit, Composite parent) {
			super(toolkit, parent, "Contents", "Filters on the contents of the index to be read as a model");
		    cContents.setLayout(createTableWrapLayout(2));

		    this.fldRepositoryURL = new FormTextField(toolkit, cContents, "Repository URL:", HawkModelDescriptor.DEFAULT_REPOSITORY);
		    this.fldFilePatterns = new FormTextField(toolkit, cContents, "File pattern(s):", HawkModelDescriptor.DEFAULT_FILES);
		    this.fldLoadingMode = new FormComboBoxField(toolkit, cContents, "Loading mode:", HawkModelDescriptor.LoadingMode.strings());

		    fldRepositoryURL.getText().addModifyListener(this);
		    fldFilePatterns.getText().addModifyListener(this);
		    fldLoadingMode.getCombo().addSelectionListener(this);
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

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.widget == fldLoadingMode.getCombo()) {
				loadingModeChanged();
			}
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (e.widget == fldRepositoryURL.getText()) {
				repositoryURLChanged();
			} else if (e.widget == fldFilePatterns.getText()) {
				filePatternsChanged();
			}
		}

		public void setFilePatterns(String[] patterns) {
			final Text text = fldFilePatterns.getText();
			text.removeModifyListener(this);
			text.setText(HawkMultiPageEditor.concat(patterns, ","));
			text.addModifyListener(this);
		}

		public void setRepositoryURL(String url) {
			final Text text = fldRepositoryURL.getText();
			text.removeModifyListener(this);
			text.setText(url);
			text.addModifyListener(this);
		}

		public void setLoadingMode(LoadingMode lazy) {
			fldLoadingMode.getCombo().select(lazy.ordinal());
		}

		protected abstract void filePatternsChanged();
		protected abstract void repositoryURLChanged();
		protected abstract void loadingModeChanged();
	}

	private static abstract class InstanceSection extends FormSection implements ModifyListener, SelectionListener {
		private final FormTextField fldInstanceName;
		private final FormTextField fldServerURL;
		private final FormComboBoxField fldTProtocol;

		public InstanceSection(FormToolkit toolkit, Composite parent) {
			super(toolkit, parent, "Instance", "Access details for the remote Hawk instance.");
		    cContents.setLayout(createTableWrapLayout(2));

		    this.fldServerURL = new FormTextField(toolkit, cContents, "Server URL:", "");
		    this.fldInstanceName = new FormTextField(toolkit, cContents, "Instance name:", "");
		    this.fldTProtocol = new FormComboBoxField(toolkit, cContents, "Thrift protocol:", ThriftProtocol.strings());

		    fldServerURL.getText().addModifyListener(this);
		    fldInstanceName.getText().addModifyListener(this);
		    fldTProtocol.getCombo().addSelectionListener(this);
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
			final Text text = fldInstanceName.getText();
			text.removeModifyListener(this);
			text.setText(name);
			text.addModifyListener(this);
		}

		public void setServerURL(String url) {
			final Text text = fldServerURL.getText();
			text.removeModifyListener(this);
			text.setText(url);
			text.addModifyListener(this);
		}

		public void setThriftProtocol(ThriftProtocol t) {
			fldTProtocol.getCombo().select(t.ordinal());
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (e.widget == fldServerURL.getText()) {
				serverURLChanged();
			} else if (e.widget == fldInstanceName.getText()) {
				instanceNameChanged();
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
			final Text text = fldClientID.getText();
			text.removeModifyListener(this);
			text.setText(s);
			text.addModifyListener(this);
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
			detailsPage.getContentSection().setRepositoryURL(descriptor.getHawkRepository());
			detailsPage.getContentSection().setFilePatterns(descriptor.getHawkFilePatterns());
			detailsPage.getContentSection().setLoadingMode(descriptor.getLoadingMode());
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
			doc.set(sW.toString());
		} catch (IOException e) {
			Activator.getDefault().logError(e);
		}
	}

	protected HawkModelDescriptor buildDescriptor() {
		final HawkModelDescriptor descriptor = new HawkModelDescriptor();
		descriptor.setHawkURL(detailsPage.getInstanceSection().getServerURL());
		descriptor.setHawkInstance(detailsPage.getInstanceSection().getInstanceName());
		descriptor.setThriftProtocol(detailsPage.getInstanceSection().getThriftProtocol());
		descriptor.setHawkRepository(detailsPage.getContentSection().getRepositoryURL());
		descriptor.setHawkFilePatterns(detailsPage.getContentSection().getFilePatterns());
		descriptor.setLoadingMode(detailsPage.getContentSection().getLoadingMode());
		descriptor.setSubscribed(detailsPage.getSubscriptionSection().isSubscribed());
		descriptor.setSubscriptionClientID(detailsPage.getSubscriptionSection().getClientID());
		descriptor.setSubscriptionDurability(detailsPage.getSubscriptionSection().getDurability());
		return descriptor;
	}

	@Override
	protected void addPages() {
		try {
			createFormBasedEditorPage();
			createRawTextEditorPage();
			getDocument().addDocumentListener(new IDocumentListener() {
				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					// ignore
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					refreshForm();
				}
			});
			refreshForm();
		} catch (Exception ex) {
			Activator.getDefault().logError(ex);
		}
	}

}
