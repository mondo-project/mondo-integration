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
import uk.ac.york.mondo.integration.api.HawkChangeEvent;
import uk.ac.york.mondo.integration.api.HawkFileAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkFileRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkModelElementRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceAdditionEvent;
import uk.ac.york.mondo.integration.api.HawkReferenceRemovalEvent;
import uk.ac.york.mondo.integration.api.HawkSynchronizationEndEvent;
import uk.ac.york.mondo.integration.api.HawkSynchronizationStartEvent;

/**
 * Handler for incoming events of each possible type that
 * {@link HawkChangeEvent} supports.
 */
public interface IHawkChangeEventHandler {

	void handle(HawkModelElementAdditionEvent ev);

	void handle(HawkModelElementRemovalEvent ev);

	void handle(HawkReferenceRemovalEvent ev);

	void handle(HawkReferenceAdditionEvent ev);

	void handle(HawkAttributeRemovalEvent ev);

	void handle(HawkAttributeUpdateEvent ev);

	void handle(HawkSynchronizationStartEvent syncStart);

	void handle(HawkSynchronizationEndEvent syncEnd);

	void handle(HawkFileAdditionEvent ev);

	void handle(HawkFileRemovalEvent ev);
}
