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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import uk.ac.york.mondo.integration.api.FailedQuery;
import uk.ac.york.mondo.integration.api.HawkInstanceNotFound;
import uk.ac.york.mondo.integration.api.HawkInstanceNotRunning;
import uk.ac.york.mondo.integration.api.InvalidQuery;
import uk.ac.york.mondo.integration.api.UnknownQueryLanguage;

public interface HawkResource extends Resource {
	/**
	 * Reports whether an object has children or not. In lazy modes, uses the LazyResolver to
	 * answer this question without hitting the network.
	 */
	boolean hasChildren(EObject o);

	Map<EObject, Object> fetchValuesByEStructuralFeature(EStructuralFeature feature)
			throws HawkInstanceNotFound, HawkInstanceNotRunning, TException, IOException;

	EList<EObject> fetchNodesByType(EClass eClass)
			throws HawkInstanceNotFound, HawkInstanceNotRunning, TException, IOException;

	List<Object> fetchValuesByEClassifier(EClassifier dataType) throws HawkInstanceNotFound, HawkInstanceNotRunning,
			UnknownQueryLanguage, InvalidQuery, FailedQuery, TException, IOException;

}
