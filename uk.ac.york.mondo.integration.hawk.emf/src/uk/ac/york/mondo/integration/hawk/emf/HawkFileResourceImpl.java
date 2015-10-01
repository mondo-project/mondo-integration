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
package uk.ac.york.mondo.integration.hawk.emf;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

public class HawkFileResourceImpl extends ResourceImpl implements IHawkResource {

	private final IHawkResource mainResource;

	/** Only to be used from Exeed (from the createExecutableExtension Eclipse call). */
	public HawkFileResourceImpl() {
		this.mainResource = null;
	}

	/**
	 * Creates a resource as a subordinate of another. Used to indicate the
	 * repository URL and file of an {@link EObject}.
	 */
	public HawkFileResourceImpl(URI uri, IHawkResource mainResource) {
		super(uri);
		this.mainResource = mainResource;
	}

	@Override
	public boolean hasChildren(EObject o) {
		if (mainResource != null) {
			return mainResource.hasChildren(o);
		} else {
			return o.eAllContents().hasNext();
		}
	}
}
