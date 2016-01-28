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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.thrift.TException;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import uk.ac.york.mondo.integration.api.Hawk;
import uk.ac.york.mondo.integration.api.HawkQueryOptions;
import uk.ac.york.mondo.integration.api.ModelElementType;
import uk.ac.york.mondo.integration.api.QueryResult;
import uk.ac.york.mondo.integration.api.SlotMetadata;
import uk.ac.york.mondo.integration.hawk.emf.EffectiveMetamodel;
import uk.ac.york.mondo.integration.hawk.emf.EffectiveMetamodelStore;
import uk.ac.york.mondo.integration.hawk.emf.HawkModelDescriptor;
import uk.ac.york.mondo.integration.hawk.emf.dt.Activator;
import uk.ac.york.mondo.integration.hawk.emf.impl.HawkResourceImpl;

public class EffectiveMetamodelFormPage extends FormPage {

	private final class MetamodelTreeViewer extends ContainerCheckedTreeViewer {
		private MetamodelTreeViewer(Composite parent) {
			super(parent);
		}

		public TreeItem findTreeItem(Object o) {
			return (TreeItem)this.findItem(o);
		}
	}

	private final class MetamodelCheckStateListener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			if (event.getElement() instanceof MetamodelNode) {
				final MetamodelNode mn = (MetamodelNode)event.getElement();
				if (event.getChecked()) {
					for (Node child : mn.children) {
						store.addType(mn.label, child.label);
					}
				} else {
					for (Node child : mn.children) {
						store.removeType(mn.label, child.label);
					}
				}
			}
			else if (event.getElement() instanceof TypeNode) {
				final TypeNode tn = (TypeNode)event.getElement();
				if (event.getChecked()) {
					store.addType(tn.parent.label, tn.label);
				} else {
					store.removeType(tn.parent.label, tn.label);
				}
			} else if (event.getElement() instanceof SlotNode) {
				final SlotNode sn = (SlotNode)event.getElement();
				final TypeNode tn = (TypeNode)sn.parent;
				final MetamodelNode mn = (MetamodelNode)tn.parent;
				ImmutableSet<String> slots = store.getIncludedSlots(mn.label, tn.label);
				ImmutableSet<String> newSlots;
				if (slots == null) {
					slots = ImmutableSet.of();
				}

				if (event.getChecked()) {
					newSlots = new ImmutableSet.Builder<String>().addAll(slots).add(sn.label).build();
				} else {
					final Builder<String> builder = new ImmutableSet.Builder<>();
					for (Node n : tn.children) {
						if (treeViewer.getChecked(n)) {
							builder.add(n.label);
						}
					}
					newSlots = builder.build();
				}

				if (newSlots.size() != tn.children.size()) {
					store.addType(mn.label, tn.label, newSlots);
				} else {
					store.addType(mn.label, tn.label);
				}
			}

			// We *don't* want to check anything that's not visible - too expensive for large metamodels!
			final TreeItem treeItem = treeViewer.findTreeItem(event.getElement());
			checkItemRecursively(treeItem, event.getChecked());
			getEditor().setDirty(true);
		}

		private void checkItemRecursively(TreeItem treeItem, boolean newState) {
			treeItem.setChecked(newState);
			for (TreeItem child : treeItem.getItems()) {
				checkItemRecursively(child, newState);
			}
		}
	}

	public class MetamodelCheckStateProvider implements ICheckStateProvider {

		@Override
		public boolean isChecked(Object element) {
			if (element instanceof MetamodelNode) {
				MetamodelNode mn = (MetamodelNode)element;
				return store.getMetamodel(mn.label) != null;
			} else if (element instanceof TypeNode) {
				TypeNode tn = (TypeNode)element;
				MetamodelNode mn = (MetamodelNode)tn.parent;
				return !store.isEverythingIncluded() && store.isTypeIncluded(mn.label, tn.label);
			} else if (element instanceof SlotNode) {
				SlotNode sn = (SlotNode)element; 
				TypeNode tn = (TypeNode)sn.parent;
				MetamodelNode mn = (MetamodelNode)tn.parent;
				return !store.isEverythingIncluded() && store.isSlotIncluded(mn.label, tn.label, sn.label);
			}

			return false;
		}

		@Override
		public boolean isGrayed(Object element) {
			if (element instanceof MetamodelNode) {
				MetamodelNode mn = (MetamodelNode) element;
				EffectiveMetamodel emm = store.getMetamodel(mn.label);
				if (emm != null) {
					for (Node child : mn.children) {
						if (!emm.isTypeIncluded(child.label) || isGrayed(child)) {
							return true;
						}
					}
				}
			} else if (element instanceof TypeNode) {
				final TypeNode tn = (TypeNode)element;
				final MetamodelNode mn = (MetamodelNode)tn.parent;
				ImmutableSet<String> slots = store.getIncludedSlots(mn.label, tn.label);
				if (slots == null) {
					return false;
				} else if (slots.contains(EffectiveMetamodel.ALL_FIELDS)) {
					return false;
				} else if (slots.isEmpty()) {
					return false;
				} else {
					for (Node child : tn.children) {
						if (!slots.contains(child.label)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	private EffectiveMetamodelStore store = new EffectiveMetamodelStore();
	private MetamodelTreeViewer treeViewer;
	private MetamodelCheckStateProvider checkStateProvider;

	private static final class MetamodelLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO: add icons
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return "" + element;
		}
	}

	private abstract static class Node {
		public final Node parent;
		public final List<Node> children = new ArrayList<>();
		public final String label;

		public Node(Node parent, String label) {
			this.parent = parent;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static class MetamodelNode extends Node {
		public MetamodelNode(Node parent, String mmURI) {
			super(parent, mmURI);
		}
	}

	private static class TypeNode extends Node {
		public TypeNode(Node parent, String name) {
			super(parent, name);
		}
	}

	private static class SlotNode extends Node {
		public SlotNode(Node parent, String name) {
			super(parent, name);
		}
	}

	private final class MetamodelContentProvider implements ITreeContentProvider {
		protected Object[] roots = null;

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			computeRoots();
		}

		protected void computeRoots() {
			if (roots == null) {
				final HawkMultiPageEditor editor = getEditor();
				try {
					final HawkModelDescriptor descriptor = editor.buildDescriptor();
					final Hawk.Client client = editor.connectToHawk(descriptor);
					final List<QueryResult> results = client.query(descriptor.getHawkInstance(),
						"return Model.types;", HawkResourceImpl.EOL_QUERY_LANG,
						new HawkQueryOptions());

					final Map<String, MetamodelNode> mmNodes = new TreeMap<>();
					for (QueryResult qr : results) {
						if (qr.isSetVModelElementType()) {
							final ModelElementType met = qr.getVModelElementType();
							MetamodelNode mn = mmNodes.get(met.getMetamodelUri());
							if (mn == null) {
								mn = new MetamodelNode(null, met.getMetamodelUri());
								mmNodes.put(met.getMetamodelUri(), mn);
							}

							final TypeNode tn = new TypeNode(mn, met.getTypeName());
							mn.children.add(tn);
							if (met.isSetAttributes()) {
								for (SlotMetadata attr : met.getAttributes()) {
									final SlotNode sn = new SlotNode(tn, attr.getName());
									tn.children.add(sn);
								}
							}
							if (met.isSetReferences()) {
								for (SlotMetadata ref : met.getReferences()) {
									final SlotNode sn = new SlotNode(tn, ref.getName());
									tn.children.add(sn);
								}
							}
						}
					}

					roots = new ArrayList<>(mmNodes.values()).toArray();
				} catch (TException e) {
					Activator.getDefault().logError(e);
				}
			}
		}

		@Override
		public void dispose() {
			roots = null;
		}

		@Override
		public Object getParent(Object element) {
			return ((Node)element).parent;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			computeRoots();
			if (roots == null) {
				return new Object[0];
			} else {
				return roots;
			}
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return ((Node)parentElement).children.toArray();
		}

		@Override
		public boolean hasChildren(Object element) {
			return !((Node)element).children.isEmpty();
		}
	}

	public EffectiveMetamodelFormPage(HawkMultiPageEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	public HawkMultiPageEditor getEditor() {
		return (HawkMultiPageEditor) super.getEditor();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		managedForm.getForm().setText("Effective Metamodel");
		
		final FormToolkit toolkit = managedForm.getToolkit();
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		final Composite formBody = managedForm.getForm().getBody();
		formBody.setLayout(layout);
		formBody.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		final FormText formText = toolkit.createFormText(formBody, true);
		formText.setText(
				"<p>"
				+ "<p>This page allows for limiting the types and slots that should be retrieved."
				+ " By default (with everything unchecked), all types and slots are retrieved.</p>"
				+ "<p>The shown metamodels are those registered in the Hawk server: please make sure "
				+ "the Instance section of the descriptor has been setup correctly before using this page.</p>"
				+ "</p>",
				true, false);

		final Composite cTable = toolkit.createComposite(formBody, SWT.FILL);
		cTable.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		cTable.setLayout(Utils.createTableWrapLayout(2));

		treeViewer = new MetamodelTreeViewer(cTable);
		final MetamodelContentProvider contentProvider = new MetamodelContentProvider();
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(new MetamodelLabelProvider());
		checkStateProvider = new MetamodelCheckStateProvider();
		treeViewer.setCheckStateProvider(checkStateProvider);
		treeViewer.addCheckStateListener(new MetamodelCheckStateListener());
		treeViewer.setInput(store);
		treeViewer.getTree().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		final Composite cButtons = toolkit.createComposite(cTable, SWT.WRAP);
		final FillLayout cButtonsLayout = new FillLayout(SWT.VERTICAL);
		cButtonsLayout.spacing = 7;
		cButtonsLayout.marginWidth = 3;
		cButtons.setLayout(cButtonsLayout);

		final Button btnSelectAll = new Button(cButtons, SWT.NONE);
		btnSelectAll.setText("Select all");
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("deprecation")
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Object on : contentProvider.roots) {
					final MetamodelNode mn = (MetamodelNode)on;
					for (Node tn : mn.children) {
						store.addType(mn.label, tn.label);
					}
				}
				treeViewer.setAllChecked(true);
				getEditor().setDirty(true);
			}
		});

		final Button btnDeselectAll = new Button(cButtons, SWT.NONE);
		btnDeselectAll.setText("Deselect all");
		btnDeselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEffectiveMetamodelStore(new EffectiveMetamodelStore());
			}
		});

		// TODO add UI for Analyze... button
	}

	public EffectiveMetamodelStore getEffectiveMetamodelStore() {
		return store;
	}

	public void setEffectiveMetamodelStore(EffectiveMetamodelStore newStore) {
		this.store = newStore;
		if (treeViewer != null) {
			treeViewer.setInput(this.store);
		}
		getEditor().setDirty(true);
	}
}
