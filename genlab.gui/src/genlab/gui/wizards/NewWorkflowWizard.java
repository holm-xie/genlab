package genlab.gui.wizards;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import genlab.core.model.instance.GenlabFactory;
import genlab.core.model.instance.IGenlabWorkflowInstance;
import genlab.core.usermachineinteraction.GLLogger;
import genlab.gui.Utils;

/**
 * Wizard to propose the user to create a new workflow
 * 
 * @author Samuel Thiriot
 *
 */
public class NewWorkflowWizard extends Wizard implements IWorkbenchWizard, INewWizard {

	protected NewWorkflowWizardFilePage page2 = null;
	protected NewWorkflowWizardDescPage page1 = null;

	
	protected IProject newProject = null;
	
	protected IStructuredSelection selection = null;
	
	public NewWorkflowWizard() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public void addPages() {

		page2 = new NewWorkflowWizardFilePage("toto", selection);
		page1 = new NewWorkflowWizardDescPage("workflow description", "description", null, page2);
		addPage(page1);
		addPage(page2);
	}
	
	

	@Override
	public boolean performFinish() {

		try {
			GLLogger.debugTech("Attempting to create a workflow...", getClass());
			
			IProject eclipseProject = page2.getSelectedProject();
			//IProject eclipseProject = Utils.findEclipseProjectInSelection(selection);

			String filenameAbsolute = eclipseProject.getWorkspace().getRoot().getLocation().toOSString()
										+page2.getRelativePath().toString()
										;
					
			IGenlabWorkflowInstance workflow = GenlabFactory.createWorkflow(
					page1.getWorkflowName(), 
					page1.getWorkflowDesc(), 
					filenameAbsolute
					);
			
	
			
			return true;
			
		} catch (RuntimeException e) {
			page2.setErrorMessage(e.getMessage());
			return false;
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
	
	

}
