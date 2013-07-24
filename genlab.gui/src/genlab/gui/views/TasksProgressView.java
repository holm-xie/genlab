package genlab.gui.views;

import genlab.core.exec.ContainerTask;
import genlab.core.exec.IContainerTask;
import genlab.core.exec.ITask;
import genlab.core.exec.ITaskManagerListener;
import genlab.core.exec.TasksManager;
import genlab.core.model.exec.ComputationState;
import genlab.core.model.exec.IAlgoExecution;
import genlab.core.model.exec.IComputationProgress;
import genlab.core.model.exec.IComputationProgressSimpleListener;
import genlab.core.usermachineinteraction.GLLogger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

/**
 * TODO progress view
 * TODO optimisation: réduire la fréquence d'appel si on n'a plus rien en cours de progres
 * 
 * @author Samuel Thiriot
 *
 */
public class TasksProgressView extends ViewPart implements ITaskManagerListener, IComputationProgressSimpleListener {

	private Display display = null;
	private Tree treeWidget = null;
	
	public final static String VIEW_ID = "genlab.gui.views.ProgressView";
	
	/**
	 * Associates each task with its counterpart in the table/tree
	 */
	private Map<ITask,TreeItem> task2item = new HashMap<ITask,TreeItem>(200);
	
	/**
	 * Contains the tasks which require an update during the next update cycle.
	 * Typically, it contains these tasks which have not yet an item associated.
	 */
	private Set<ITask> tasksToUpdate = new HashSet<ITask>(100);
	
	public final static long REFRESH_PERIOD = 500;
	
	private class ProgressThread extends Thread {

		private final TasksProgressView view;
		private boolean canceled = false;
		
		public ProgressThread(TasksProgressView view) {
			this.view = view;
			
			// configure thread
			setName("refresh tasks view");
			setDaemon(true);
			setPriority(NORM_PRIORITY);
			
			GLLogger.traceTech("created.", getClass());
		}
		
		@Override
		public void run() {
			
			while (!canceled && (display == null || !display.isDisposed())) {
				
				//GLLogger.traceTech("refresh ?", getClass());
				
				if (view.hasSomethingToUpdate()) {
					//GLLogger.traceTech("refresh !.", getClass());
					view.getDisplay().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							view.updateWidgets();		
						}
					});
					
				}

				try {
					//GLLogger.traceTech("sleeping", getClass());
					Thread.sleep(REFRESH_PERIOD);
				} catch (InterruptedException e) {
				}
			}
		}

		public void cancel() {
			canceled = true;
		}
		
	}
	
	private ProgressThread thread = null;
	
	public TasksProgressView() {

		// listen !
		TasksManager.singleton.addListener(this);
		

	}

	
	
	@Override
	public void dispose() {
		
		thread.cancel();
		
		TasksManager.singleton.removeListener(this);
		
		treeWidget.dispose();
		
		super.dispose();
	}



	public Display getDisplay() {
		return display;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		display = parent.getDisplay();
		
		treeWidget = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		treeWidget.setHeaderVisible(true);
		TreeColumn columnName = new TreeColumn(treeWidget, SWT.LEFT);
		columnName.setText("name");
		columnName.setWidth(100);
		TreeColumn columnState = new TreeColumn(treeWidget, SWT.LEFT);
		columnState.setText("state");
		columnState.setWidth(100);
		
		thread = new ProgressThread(this);
		thread.start();
	}

	@Override
	public void setFocus() {
		treeWidget.setFocus();
	}
	

	private int findPositionForTask(ITask t) {
		
		if (task2item.isEmpty())
			return 0;
		
		GLLogger.debugTech("displaying task "+t+" "+t.getProgress().getTimestampCreation(), getClass());

		if (t.getPrerequires().isEmpty())
			return 0;	// quick solution
		
		LinkedList<ITask> dependenciesNotSatisfied = new LinkedList<ITask>(t.getPrerequires());
	
		TreeItem[] items = null;
		if (t.getParent() == null)
			items = treeWidget.getItems();
		else 
			items = task2item.get(t.getParent()).getItems();
		
		if (items.length == 0)
			return 0; // quick solution
		
		int i = 0;
		for (i=0; i<items.length; i++) {
			ITask currentTask  = (ITask)items[i].getData();
			dependenciesNotSatisfied.remove(currentTask);
			if (dependenciesNotSatisfied.isEmpty()) {
				//GLLogger.debugTech("displaying task "+t+" at index "+i+" among "+Arrays.toString(items), getClass());
				return i+1;
			}
		}
		
		// TODO not working now
		//GLLogger.debugTech("displaying task "+t+" at index "+(i-1)+" among "+Arrays.toString(items), getClass());
		
		return Math.max(0, i-1);
			
	}
	
	private TreeItem getOrCreateItemForTask(ITask t) {
		
		
		TreeItem item = task2item.get(t);
		
		if (item == null) {
			// create the item !
			if (t.getParent() == null)
				item = new TreeItem(
						treeWidget, 
						SWT.NONE, 
						findPositionForTask(t)
						);
			else 
				item = new TreeItem(
						getOrCreateItemForTask(t.getParent()), 
						SWT.NONE, 
						findPositionForTask(t)
						);
			item.setText(0, t.getName());
			item.setData(t);
			
			task2item.put(t, item);	
			
			// configure the item
			
			// ... its image...
			if (t instanceof IAlgoExecution) {
				IAlgoExecution ae = (IAlgoExecution)t;
				try {
				String imagePath = ae.getAlgoInstance().getAlgo().getImagePath();
				if (imagePath != null) {
				
					try {
						// TODO avoid to reload this image each time
						Image img = new Image(display, imagePath);
						item.setImage(img);
					} catch (RuntimeException e) {
						GLLogger.warnTech("unable to find image "+imagePath, getClass());
					}
				}
				} catch (NullPointerException e) {
					GLLogger.warnTech("unable to find an image", getClass());
				}
				
			}
			
			// ... its expanded state
			item.setExpanded(t instanceof IContainerTask);

		} 
		
		return item;
		
	}
	
	/**
	 * to be called from the swt thread.
	 * @param t
	 */
	private void updateWidget(ITask t) {
		
		//GLLogger.debugTech("updating for task "+t, getClass());
		
		
		TreeItem item = getOrCreateItemForTask(t);
		
		if (item.isDisposed()) {
			task2item.remove(t);
			return;
		}
		
		// update its state ? 
		final ComputationState state = t.getProgress().getComputationState();
		String txt = null;
		switch (state) {
		case FINISHED_OK: {
			StringBuffer sb = new StringBuffer();
			sb.append("finished (").append(getHumanReadableTimeRepresentation(t.getProgress().getDurationMs())).append(")");
			txt = sb.toString();
		} break;
		case STARTED: { StringBuffer sb = new StringBuffer();
			sb.append("running (").append(t.getProgress().getProgressDone()).append("/").append(t.getProgress().getProgressTotalToDo()).append(")");
			txt = sb.toString();
		} break;
		default:
			txt  = state.toString();
			break;
		}
		item.setText(1, txt);
		
		// TODO progress bar :-)
	
		
	}
	
	protected boolean hasSomethingToUpdate() {
		
		if (!task2item.isEmpty())
			return true;
		
		synchronized (tasksToUpdate) {
			return !tasksToUpdate.isEmpty();
		}
	}
	
	protected String getHumanReadableTimeRepresentation(long durationMs) {
	
		StringBuffer sb = new StringBuffer();
		
		if (durationMs < 1000)
			sb.append(durationMs).append(" ms");
		else {
			final long durationS = durationMs/1000;
			if (durationS < 60) {
				sb.append(durationS).append(" s");
			} else {
				final int durationM = (int)Math.round(durationS / 60.0);
				sb.append(durationM).append(" m");
			}
		}
		return sb.toString();
		
	}
	
	
	/**
	 * SHould be called from the SWT thread
	 */
	protected void updateWidgets() {
				
		/*
		// first copy the collection of tasks updates to avoid concurrent modifications
		Collection<ITask> tasksUpdating = null;
		synchronized (tasksToUpdate) {
			if (tasksToUpdate.isEmpty())
				return; // quick exit (as quick as possible, at least)
			tasksUpdating = new LinkedList<ITask>(tasksToUpdate);
			tasksToUpdate.clear();
		}
		
		// now update each task / widget
		for (ITask t : tasksUpdating) {
			updateWidget(t);
		}
		*/
		
		if (treeWidget == null || treeWidget.isDisposed()) {
			thread.cancel();
			return;
		}
		
		// first copy the collection of tasks updates to avoid concurrent modifications
		Set<ITask> tasksUpdating = new HashSet<ITask>(task2item.size()*2);
		synchronized (task2item) {
			tasksUpdating.addAll(task2item.keySet());	
		}
		synchronized (tasksToUpdate) {
			tasksUpdating.addAll(tasksToUpdate);
		}
		
		// now update each task / widget
		for (ITask t : tasksUpdating) {
			updateWidget(t);
		}
		
		
	}

	private void manageTaskChanged(ITask task) {

		GLLogger.debugTech("a task was added: "+task, getClass());
		synchronized (tasksToUpdate) {
			tasksToUpdate.add(task);	
			
			if (task instanceof IContainerTask) {
				IContainerTask ct = (IContainerTask)task;
				for (ITask subTask : ct.getTasks()) {
					tasksToUpdate.add(subTask);
				}
			}
		}
		thread.interrupt();
		
	}

	@Override
	public void notifyTaskAdded(ITask task) {
		
		manageTaskChanged(task);
		task.getProgress().addListener(this);
		
	}

	@Override
	public void notifyTaskRemoved(ITask task) {
		GLLogger.debugTech("a task was removed: "+task, getClass());
		
		task.getProgress().removeListener(this);

		// TODO !!! task2item.remove(task);
	}



	@Override
	public void computationStateChanged(IComputationProgress progress) {
		notifyTaskAdded(progress.getAlgoExecution());
	}

}
