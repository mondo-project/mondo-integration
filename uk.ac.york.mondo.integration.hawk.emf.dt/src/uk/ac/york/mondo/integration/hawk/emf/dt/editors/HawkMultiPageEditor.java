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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor;
import uk.ac.york.mondo.integration.hawk.emf.dt.Activator;

/**
 * Editor for <code>.hawkmodel</code> files. The first page is a form-based UI
 * for editing the raw text on the second page.
 */
public class HawkMultiPageEditor extends FormEditor	implements IResourceChangeListener {

	private static abstract class ContentSection implements ModifyListener {
		private final FormField fldFilePatterns;
		private final FormField fldRepositoryURL;

		public ContentSection(FormToolkit toolkit, Composite parent) {
		    final Section sectionContent = toolkit.createSection(parent, Section.TITLE_BAR|Section.DESCRIPTION);
		    sectionContent.setText("Contents");
		    sectionContent.setDescription("Filters on the contents of the index to be read as a model");
		    TableWrapData tdSectionContent = new TableWrapData(TableWrapData.FILL_GRAB);
		    sectionContent.setLayoutData(tdSectionContent);

		    final Composite cContents =  toolkit.createComposite(sectionContent, SWT.WRAP);
		    sectionContent.setClient(cContents);
		    cContents.setLayout(createTableWrapLayout(2));

		    this.fldRepositoryURL = new FormField(toolkit, cContents, "Repository URL:", HawkModelDescriptor.DEFAULT_REPOSITORY);
		    this.fldFilePatterns = new FormField(toolkit, cContents, "File pattern(s):", HawkModelDescriptor.DEFAULT_FILES);

		    fldRepositoryURL.getText().addModifyListener(this);
		    fldFilePatterns.getText().addModifyListener(this);
		}

		public String[] getFilePatterns() {
			return fldFilePatterns.getText().getText().split(",");
		}

		public String getRepositoryURL() {
			return fldRepositoryURL.getText().getText();
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
			// Avoid triggering filePatternsChanged unnecessarily
			final String newText = HawkMultiPageEditor.concat(patterns, ",");
			final String oldText = fldFilePatterns.getText().getText();
			if (!isEqual(oldText, newText)) {
				fldFilePatterns.getText().setText(newText);
			}
		}

		public void setRepositoryURL(String url) {
			// Avoid triggering repositoryURLChanged unnecessarily
			if (!isEqual(getRepositoryURL(), url)) {
				fldRepositoryURL.getText().setText(url);
			}
		}

		protected abstract void filePatternsChanged();
		protected abstract void repositoryURLChanged();
	}

	private static abstract class InstanceSection implements ModifyListener {
		private final FormField fldInstanceName;
		private final FormField fldServerURL;

		public InstanceSection(FormToolkit toolkit, Composite parent) {
		    final Section section = toolkit.createSection(parent, Section.TITLE_BAR|Section.DESCRIPTION);
		    section.setText("Instance");
		    section.setDescription("Access details for the remote Hawk instance.");
		    section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		    final Composite client =  toolkit.createComposite(section, SWT.NONE);
			section.setClient(client);
		    client.setLayout(createTableWrapLayout(2));

		    this.fldServerURL = new FormField(toolkit, client, "Server URL:", "");
		    this.fldInstanceName = new FormField(toolkit, client, "Instance name:", "");
		    fldServerURL.getText().addModifyListener(this);
		    fldInstanceName.getText().addModifyListener(this);
		}

		public String getInstanceName() {
			return fldInstanceName.getText().getText();
		}

		public String getServerURL() {
			return fldServerURL.getText().getText();
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (e.widget == fldServerURL.getText()) {
				serverURLChanged();
			} else if (e.widget == fldInstanceName.getText()) {
				instanceNameChanged();
			}
		}

		public void setInstanceName(String name) {
			if (!isEqual(getInstanceName(), name)) {
				fldInstanceName.getText().setText(name);
			}
		}

		public void setServerURL(String url) {
			if (!isEqual(getServerURL(), url)) {
				fldServerURL.getText().setText(url);
			}
		}

		protected abstract void instanceNameChanged();
		protected abstract void serverURLChanged();
	}


	/**
	 * Paired label and text field.
	 */
	private static class FormField {
		private final Label label;
		private final Text text;

		public FormField(FormToolkit toolkit, Composite sectionClient, String labelText, String defaultValue) {
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

	/**
	 * Equality with {@link Object#equals(Object)}, safe against null values.
	 */
	private static boolean isEqual(Object a, Object b) {
		return a == null && b == null || a != null && a.equals(b);
	}

	/** The text editor used in page 0. */
	private TextEditor editor;

	private ContentSection sectionContent;

	private InstanceSection sectionInstance;

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

	private void createFormBasedEditorPage() {
		final FormToolkit toolkit = createToolkit(getContainer().getDisplay());
		final ScrolledForm form = toolkit.createScrolledForm(getContainer());
		form.setText("Remote Hawk Model Descriptor");

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		form.getBody().setLayout(layout);

		this.sectionInstance = new InstanceSection(toolkit, form.getBody()) {
			@Override protected void instanceNameChanged() { writeFile(); }
			@Override protected void serverURLChanged()    { writeFile(); }
		};
		this.sectionContent = new ContentSection(toolkit, form.getBody()) {
			@Override protected void filePatternsChanged()  { writeFile(); }
			@Override protected void repositoryURLChanged() { writeFile(); }
		};

		int index = addPage(form);
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

			sectionInstance.setServerURL(descriptor.getHawkURL());
			sectionInstance.setInstanceName(descriptor.getHawkInstance());
			sectionContent.setRepositoryURL(descriptor.getHawkRepository());
			sectionContent.setFilePatterns(descriptor.getHawkFilePatterns());
		} catch (IOException e) {
			Activator.getDefault().logError(e);
		}
	}

	private void writeFile() {
		final HawkModelDescriptor descriptor = new HawkModelDescriptor();
		descriptor.setHawkURL(sectionInstance.getServerURL());
		descriptor.setHawkInstance(sectionInstance.getInstanceName());
		descriptor.setHawkRepository(sectionContent.getRepositoryURL());
		descriptor.setHawkFilePatterns(sectionContent.getFilePatterns());

		final StringWriter sW = new StringWriter();
		try {
			descriptor.save(sW);

			final IDocument doc = getDocument();
			doc.set(sW.toString());
		} catch (IOException e) {
			Activator.getDefault().logError(e);
		}
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

			// load initial contents
			refreshForm();
		} catch (Exception ex) {
			Activator.getDefault().logError(ex);
		}
	}
}
