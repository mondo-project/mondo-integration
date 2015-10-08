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

import uk.ac.york.mondo.integration.api.HawkAttributeRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkAttributeUpdateEvent;
import uk.ac.york.mondo.integration.api.HawkFileAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkFileRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkSynchronizationEndEvent;
import uk.ac.york.mondo.integration.api.HawkSynchronizationStartEvent;

/**
 * Adapter for {@link IHawkChangeEventHandler} that provides default no-op implementations for all operations. Useful
 * for subclassing.
 */
public class HawkChangeEventAdapter implements IHawkChangeEventHandler {

	@Override
	public void handle(HawkModelElementAdditionEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkModelElementRemovalEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkReferenceRemovalEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkReferenceAdditionEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkAttributeRemovalEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkAttributeUpdateEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkSynchronizationStartEvent syncStart) {
		// nothing
	}

	@Override
	public void handle(HawkSynchronizationEndEvent syncEnd) {
		// nothing
	}

	@Override
	public void handle(HawkFileAdditionEvent ev) {
		// nothing
	}

	@Override
	public void handle(HawkFileRemovalEvent ev) {
		// nothing
	}

}
