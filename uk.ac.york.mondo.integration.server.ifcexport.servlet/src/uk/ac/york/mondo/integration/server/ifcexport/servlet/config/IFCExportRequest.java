package uk.ac.york.mondo.integration.server.ifcexport.servlet.config;

import java.util.ArrayList;
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
	
	public IFCExportRequest(String hawkInstance, ArrayList<String> repoPatterns, 
			ArrayList<String> filePatterns, Map<String,Map<String,Set<String>>> includeRules, Map<String,Map<String,Set<String>>> excludeRules)
	{
		this.hawkInstance = hawkInstance;
		this.exportOptions = new IFCExportOptions(repoPatterns, filePatterns, includeRules, excludeRules);
	}
	
	public String getHawkInstance() {
		return hawkInstance;
	}
	
	public IFCExportOptions getExportOptions() {
		return exportOptions;
	}
	
	public ArrayList<String> getRepoPatterns()
	{
		return (ArrayList<String>) exportOptions.getRepoPatterns();
	}
	
	public ArrayList<String> getFilePatterns()
	{
		return (ArrayList<String>) exportOptions.getFilePatterns();
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
