package genlab.core.projects;

import genlab.core.model.instance.IGenlabWorkflowInstance;

import java.io.File;
import java.util.Collection;

public interface IGenlabProject {
	
	public String getBaseDirectory();
	
	public File getFolder();
	
	public Object getAttachedObject(String key);

	public void setAttachedObject(String key, Object o);
	
	public Collection<IGenlabWorkflowInstance> getWorkflows();
	
	public void addWorkflow(IGenlabWorkflowInstance workflow);
	
	public String getProjectSavingFilename();
	
}