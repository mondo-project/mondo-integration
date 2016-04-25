package uk.ac.york.mondo.integration.server.ifcexport.servlet.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.york.mondo.integration.api.IFCExportOptions;

public class IFCExportRequest {
	
	protected String hawkInstance;
	protected IFCExportOptions exportOptions;
	
	public IFCExportRequest(String hawkInstance, IFCExportOptions exportOptions) {
		this.hawkInstance = hawkInstance;
		this.exportOptions = exportOptions;
	}
	
	public String getHawkInstance() {
		return hawkInstance;
	}
	
	public IFCExportOptions getExportOptions() {
		return exportOptions;
	}
	
	public String getRepositoryPattern()
	{
		return exportOptions.getRepositoryPattern();
	}
	
	public List<String> getFilePatterns()
	{
		return exportOptions.getFilePatterns();
	}
	
	public Map<String,Map<String,Set<String>>> getIncludeRules()
	{
		return exportOptions.getIncludeRules();
	}
	
	public Map<String,Map<String,Set<String>>> getExcludeRules()
	{
		return exportOptions.getExcludeRules();
	}
}
