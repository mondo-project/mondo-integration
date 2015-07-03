/*******************************************************************************
 * Copyright (c) 2011-2015 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Konstantinos Barmpis - initial API and implementation
 *     Antonio Garcia-Dominguez - move to servlet project, switch to SLF4J
 ******************************************************************************/
package uk.ac.york.mondo.integration.hawk.servlet.util;

import org.hawk.core.IAbstractConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JConsole implements IAbstractConsole {
	private Logger LOGGER = LoggerFactory.getLogger(SLF4JConsole.class);

	@Override
	public void println(String s) {
		LOGGER.info(s);		
	}

	@Override
	public void printerrln(String s) {
		LOGGER.error(s);	
	}

	@Override
	public void print(String s) {
		println(s);
	}
}
