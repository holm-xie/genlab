package genlab.gui.graphiti.features;

import genlab.core.model.doc.AvailableInfo;
import genlab.core.model.instance.IAlgoInstance;
import genlab.core.usermachineinteraction.GLLogger;
import genlab.gui.graphiti.GraphitiImageProvider;
import genlab.gui.views.AlgoInfoView;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * This feature opens a window which displays information about an algo.
 * 
 * @author Samuel Thiriot
 *
 */
public class SeeInfoFeature extends AbstractCustomFeature {

	
	public SeeInfoFeature(IFeatureProvider fp) {
		super(fp);

	}
	

	@Override
	public String getName() {
		return "info";
	}
	
	
	@Override
	public String getDescription() {
		return "display more details about this algorithm";
	}


	
	@Override
	public boolean canExecute(ICustomContext context) {

		if (context.getInnerPictogramElement() == null)
			return false;
		
		final Object value = getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
		
		if (!(value instanceof IAlgoInstance))
			return false;
		
		IAlgoInstance ai = (IAlgoInstance)value;
		
		return AvailableInfo.getAvailableInfo().hasAlgoDoc(ai.getAlgo().getId());
		
	}


	@Override
	public void execute(ICustomContext context) {
		
		GLLogger.debugTech("opening info view...", getClass());
		
		final IAlgoInstance algoInstance = (IAlgoInstance)getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
		
		try {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					// id of the view (provided by the genlab.gui package)
					"genlab.gui.views.AlgoInfoView"
					);
			// transmit info to enable the view to load what is required
			WorkbenchPart v = (WorkbenchPart)view;
			
			v.setPartProperty(
					AlgoInfoView.PROPERTY_ALGO_ID, 
					algoInstance.getAlgo().getId()
					);
			
		} catch (PartInitException e) {
			GLLogger.errorTech("error while attempting to open preferences: "+e.getLocalizedMessage(), getClass(), e);
		}

	}

	public String getImageId() {
		
		return GraphitiImageProvider.SEEINFO_ID;
	}
}