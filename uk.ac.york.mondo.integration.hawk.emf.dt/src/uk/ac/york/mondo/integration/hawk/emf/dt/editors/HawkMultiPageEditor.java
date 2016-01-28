/*******************************************************************************
 * Copyright (c) 2015-2016 University of York.
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

import org.apache.thrift.transport.TTransportException;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.utils.APIUtils;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor;
import uk.ac.york.mondo.integration.hawk.emf.dt.Activator;
import uk.ac.york.mondo.integration.hawk.remote.thrift.ui.LazyCredentials;

/**
 * Editor for <code>.hawkmodel</code> files. The first page is a form-based UI
 * for editing the raw text on the second page.
 */
public class HawkMultiPageEditor extends FormEditor	implements IResourceChangeListener {

	private static final int RAW_EDITOR_PAGE_INDEX = 1;

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
		detailsPage = new DetailsFormPage(this, "details", "Remote Hawk Model Descriptor");
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

	void refreshRawText() {
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
