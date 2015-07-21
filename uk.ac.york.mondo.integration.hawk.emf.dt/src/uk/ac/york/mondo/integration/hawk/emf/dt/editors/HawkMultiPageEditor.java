package uk.ac.york.mondo.integration.hawk.emf.dt.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.york.mondo.integration.hawk.emf.HawkResourceImpl;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class HawkMultiPageEditor extends FormEditor	implements IResourceChangeListener {

	private static final int RAW_EDITOR_PAGE_INDEX = 1;

	/** The text editor used in page 0. */
	private TextEditor editor;

	/**
	 * Creates a multi-page editor example.
	 */
	public HawkMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates page 1 of the multi-page editor, which allows you to change the
	 * font used in page 2.
	 */
	private void createFormBasedEditorPage() {
		final FormToolkit toolkit = createToolkit(getContainer().getDisplay());
		final ScrolledForm form = toolkit.createScrolledForm(getContainer());
		form.setText("Remote Hawk Model Descriptor");

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		form.getBody().setLayout(layout);

	    final Section sectionServer = toolkit.createSection(form.getBody(), Section.TITLE_BAR|Section.DESCRIPTION);
	    sectionServer.setText("Instance");
	    sectionServer.setDescription("Access details for the remote Hawk instance.");
	    sectionServer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

	    final Composite cServer =  toolkit.createComposite(sectionServer, SWT.NONE);
		sectionServer.setClient(cServer);
	    final TableWrapLayout cServerLayout = new TableWrapLayout();
	    cServerLayout.numColumns = 2;
	    cServerLayout.horizontalSpacing = 5;
	    cServerLayout.verticalSpacing = 3;
	    cServer.setLayout(cServerLayout);

	    Label lServerURL = toolkit.createLabel(cServer, "Server URL:", SWT.WRAP);
	    lServerURL.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		final TableWrapData tdServerURL = new TableWrapData();
		tdServerURL.valign = TableWrapData.MIDDLE;
		lServerURL.setLayoutData(tdServerURL);
		Text tServerURL = toolkit.createText(cServer, "", SWT.BORDER);
		tServerURL.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Label lInstance = toolkit.createLabel(cServer, "Instance name:", SWT.WRAP);
		lInstance.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		final TableWrapData tdInstance = new TableWrapData();
		tdInstance.valign = TableWrapData.MIDDLE;
		lInstance.setLayoutData(tdInstance);
		Text tInstance = toolkit.createText(cServer, "", SWT.BORDER);
		tInstance.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

	    final Section sectionContent = toolkit.createSection(form.getBody(), Section.TITLE_BAR|Section.DESCRIPTION);
	    sectionContent.setText("Contents");
	    sectionContent.setDescription("Filters on the contents of the index to be read as a model");
	    TableWrapData tdSectionContent = new TableWrapData(TableWrapData.FILL_GRAB);
	    sectionContent.setLayoutData(tdSectionContent);

	    final Composite cContents =  toolkit.createComposite(sectionContent, SWT.WRAP);
	    sectionContent.setClient(cContents);
	    final TableWrapLayout cContentsLayout = new TableWrapLayout();
	    cContentsLayout.numColumns = 2;
	    cContentsLayout.horizontalSpacing = 5;
	    cContentsLayout.verticalSpacing = 3;
	    cContents.setLayout(cContentsLayout);
	    
		Label lRepository = toolkit.createLabel(cContents, "Repository URL:", SWT.WRAP);
		lRepository.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		final TableWrapData tdRepository = new TableWrapData();
		tdRepository.valign = TableWrapData.MIDDLE;
		lRepository.setLayoutData(tdRepository);
		Text tRepository = toolkit.createText(cContents, HawkResourceImpl.DEFAULT_REPOSITORY, SWT.BORDER);
		tRepository.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Label lFiles = toolkit.createLabel(cContents, "File pattern(s):", SWT.WRAP);
		lFiles.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		final TableWrapData tdFiles = new TableWrapData();
		tdFiles.valign = TableWrapData.MIDDLE;
		lFiles.setLayoutData(tdFiles);
		Text tFiles = toolkit.createText(cContents, HawkResourceImpl.DEFAULT_FILES, SWT.BORDER);
		tFiles.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		int index = addPage(form);
		setPageText(index, "Descriptor");
	}
	/**
	 * Creates the pages of the multi-page editor.
	 */
	@Override
	protected void addPages() {
		createFormBasedEditorPage();
		try {
			editor = new TextEditor();
			int rawEditorPage = addPage(editor, getEditorInput());
			setPageText(rawEditorPage, editor.getTitle());
			setPartName(editor.getTitle());
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(RAW_EDITOR_PAGE_INDEX).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(RAW_EDITOR_PAGE_INDEX);
		editor.doSaveAs();
		setPageText(RAW_EDITOR_PAGE_INDEX, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(1);
		IDE.gotoMarker(getEditor(RAW_EDITOR_PAGE_INDEX), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
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

}
