package genlab.core.model.exec;

import java.util.Set;

import genlab.core.commons.NotImplementedException;
import genlab.core.commons.ProgramException;
import genlab.core.exec.ITask;
import genlab.core.model.instance.IConnection;

/**
 * A connection instance in the execution world: links two algo executions.
 * In practice, the connection is in charge of storing and making the result accessible for the 
 * next algo.
 * 
 * This connection relates an iteration and a reduce algorithm.
 * Once the computation is finished in the "from" part, the "reduce" destination algo
 * receives the corresponding data.
 * 
 * @author Samuel Thiriot
 *
 */
public class ConnectionExecFromIterationToReduce extends AbstractConnectionExec<IAlgoExecution, IReduceAlgoExecution> {


	/**
	 * For serialization only
	 */
	public ConnectionExecFromIterationToReduce() {}

	public ConnectionExecFromIterationToReduce(IConnection c, IAlgoExecution from, IReduceAlgoExecution to) {

		super(c, from, to);
		
		// TODO check ?
		
	}
	
	/* (non-Javadoc)
	 * @see genlab.core.model.exec.IConnectionExecution#computationStateChanged(genlab.core.model.exec.IComputationProgress)
	 */
	@Override
	public void computationStateChanged(IComputationProgress progress) {
		
		if (progress != from.getProgress())
			return;	// no reason for this case...

		final ComputationState state = progress.getComputationState();

		// propagate failure / cancel
		if (state == ComputationState.FINISHED_CANCEL || state == ComputationState.FINISHED_FAILURE) {
			value = null;
			to.cancel();
			return;
		}
		
		if (state != ComputationState.FINISHED_OK) {
			//System.out.println("clean value because parent "+from+" is "+state+": "+this);
			value = null;
			return;
		}
		
		// there should be a value
		
		// retrieve the value
		try {
			value = from.getResult().getResults().get(c.getFrom());
		} catch (NullPointerException e) {
			throw new ProgramException("an executable announced a finished with success, but does not publish results.");
		}
		
		// ensure we got one
		if (value == null)
			exec.getListOfMessages().errorUser("received a null value...", getClass());

		// warn children
		to.receiveInput(
				(IAlgoExecution)from.getParent(), 
				this, 
				value
				);

		
	}
	
	
	/* (non-Javadoc)
	 * @see genlab.core.model.exec.IConnectionExecution#forceValue(java.lang.Object)
	 */
	@Override
	public void forceValue(Object value) {
		
		throw new NotImplementedException();
		
	}
	
	@Override
	public void propagateRank(Integer rank, Set<ITask> visited) {
		// don't propagate
	}


}
