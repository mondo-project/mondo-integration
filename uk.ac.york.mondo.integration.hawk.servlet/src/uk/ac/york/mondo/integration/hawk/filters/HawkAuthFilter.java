package uk.ac.york.mondo.integration.hawk.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.shiro.web.servlet.IniShiroFilter;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

@SuppressWarnings("deprecation")
public class HawkAuthFilter extends IniShiroFilter {

	public HawkAuthFilter() throws URISyntaxException, IOException {
		final Bundle bundle = FrameworkUtil.getBundle(HawkAuthFilter.class);
		final File shiroFile = bundle.getDataFile("shiro.ini");
		if (!shiroFile.exists()) {
			final URL urlDefaultShiro = bundle.getEntry("resources/shiro.ini.default");
			final File fDefaultShiro = new File(FileLocator.resolve(urlDefaultShiro).toURI());
			try (final FileInputStream fIS = new FileInputStream(fDefaultShiro)) {
				Files.copy(fIS, shiroFile.toPath());
			}
		}

		setConfigPath(shiroFile.toURI().toString());
	}

}
