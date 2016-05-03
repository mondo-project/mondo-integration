package org.mondo.collaboration.offline.management.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.crypto.interfaces.PBEKey;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.thrift.TException;
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import uk.ac.york.mondo.integration.api.GoldRepoNotFound;
import uk.ac.york.mondo.integration.api.OfflineCollaboration.Iface;
import uk.ac.york.mondo.integration.api.OfflineCollaborationInternalError;
import uk.ac.york.mondo.integration.api.UnauthorizedRepositoryOperation;
import uk.ac.york.mondo.integration.api.utils.APIUtils.ThriftProtocol;

/*******************************************************************************
 * Copyright (c) 2016 Budapest University of Technology and Economics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/
public class OfflineCollaborationHandler implements Iface {


	public String getScriptsFolder() {
		String retBundle = Activator.getContext().getBundle().getBundleContext().getProperty("mondo.scripts.folder");
		String retSystem = System.getProperty("mondo.scripts.folder");
		return retBundle == null ? retSystem : retBundle;
	}
	

	@Override
	public void regenerateFrontRepositories(String goldRepoURL) throws GoldRepoNotFound,
			UnauthorizedRepositoryOperation, OfflineCollaborationInternalError, TException {
		try {
			String scriptsFolder = getScriptsFolder();
			ProcessBuilder builder0 = new ProcessBuilder(scriptsFolder + "lookup-gold-repository.sh", goldRepoURL);
			Process lookupRepoName = builder0.start();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(lookupRepoName.getInputStream()));
			String goldRepoName = bufferedReader.readLine();
			ProcessBuilder builder1 = new ProcessBuilder(scriptsFolder + "/reset-front-repositories.sh", goldRepoName);
			Process regenerateRepos = builder1.start();
//			regenerateRepos.waitFor();
		} catch (IOException e) {
			throw new OfflineCollaborationInternalError(e.getMessage());
		}
//		catch (InterruptedException e) {
//			throw new OfflineCollaborationInternalError(e.getMessage());
//		}
		
	}

	@Override
	public String getMyFrontRepositoryURL(String goldRepoURL) throws GoldRepoNotFound,
			UnauthorizedRepositoryOperation, OfflineCollaborationInternalError, TException {
		String frontRepoURL = null;
		try {
			String userName = SecurityUtils.getSubject().getPrincipal().toString();
			String scriptsFolder = getScriptsFolder();
			ProcessBuilder builder0 = new ProcessBuilder(scriptsFolder + "lookup-gold-repository.sh", goldRepoURL);
			Process lookupRepoName = builder0.start();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(lookupRepoName.getInputStream()));
			String goldRepoName = bufferedReader.readLine();
			ProcessBuilder builder1 = new ProcessBuilder(scriptsFolder + "/get-front-repository.sh", goldRepoName, userName);
			Process getFrontRepo = builder1.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(getFrontRepo.getInputStream()));
			frontRepoURL = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			throw new OfflineCollaborationInternalError(e.getMessage());
		}
		
		return frontRepoURL;
	}

	private String getRAPPathFromExtensionRegistry() {

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint poi;

		String rapPath = null;

		if (reg != null) {
			poi = reg.getExtensionPoint("org.eclipse.rap.ui.entrypoint");
			if (poi != null) {
				IExtension[] exts = poi.getExtensions();

				for (IExtension ext : exts) {
					IConfigurationElement[] els = ext.getConfigurationElements();
					for (IConfigurationElement el : els) {
						String pathAttribute = el.getAttribute("path");
						if(pathAttribute != null){
							rapPath = pathAttribute;
							break;
						}
					}
				}
			}
		}
		return rapPath;
	}
	@Override
	public List<String> listGoldRepositories()
			throws UnauthorizedRepositoryOperation, OfflineCollaborationInternalError, TException {
		// For the time being there is only one gold repository on the server
		return null;
	}
	@Override
	public String getOnlineCollaborationURL(String goldRepoURL)
			throws GoldRepoNotFound, UnauthorizedRepositoryOperation, OfflineCollaborationInternalError, TException {
		
		
		String rapPath = getRAPPathFromExtensionRegistry();
		System.out.println(rapPath);
		return rapPath;
	}
	

}