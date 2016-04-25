package uk.ac.york.mondo.integration.server.ifcexport.servlet.config;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

import uk.ac.york.mondo.integration.api.IFCExportJob;
import uk.ac.york.mondo.integration.api.IFCExportStatus;

public class IFCExportJobChangeListener implements IJobChangeListener{

	protected IFCExportJob job;
	
	public IFCExportJobChangeListener(IFCExportJob job) {
		// TODO Auto-generated constructor stub
		this.job = job;
	}
	
	public void setJob(IFCExportJob job) {
		this.job = job;
	}
	
	@Override
	public void aboutToRun(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void awake(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done(IJobChangeEvent event) {
		job.setStatus(IFCExportStatus.DONE);
	}

	@Override
	public void running(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		job.setStatus(IFCExportStatus.RUNNING);
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		// TODO Auto-generated method stub
		job.setStatus(IFCExportStatus.SCHEDULED);
		
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
	}

}
