package genlab.graphstream.algos.writers;

import genlab.core.commons.WrongParametersException;
import genlab.core.exec.IExecution;
import genlab.core.model.exec.AbstractAlgoExecution;
import genlab.core.model.exec.ComputationProgressWithSteps;
import genlab.core.model.exec.ComputationResult;
import genlab.core.model.exec.ComputationState;
import genlab.core.model.instance.IAlgoInstance;
import genlab.core.model.meta.basics.graphs.IGenlabGraph;
import genlab.graphstream.utils.GraphstreamConvertors;

import java.io.File;
import java.io.IOException;

import org.graphstream.stream.file.FileSink;

public class AbstractGraphstreamGraphWriterExecution extends
		AbstractAlgoExecution {
	
	protected IGenlabGraph inputGraph = null;
	protected final FileSink fileSink;

	public AbstractGraphstreamGraphWriterExecution(
			IExecution exec, 
			IAlgoInstance algoInst,
			FileSink fileSink
			) {
		super(exec, algoInst, new ComputationProgressWithSteps());
		this.fileSink = fileSink;
	}

	@Override
	public void run() {

		try {
		
			// retrieve the graph
			inputGraph = AbstractGraphStreamGraphWriter.PARAM_GRAPH.decodeFromParameters(
					getInputValueForInput(
							AbstractGraphStreamGraphWriter.PARAM_GRAPH
							)
					);
			if (inputGraph == null)
				 throw new WrongParametersException("input graph expected");
	
					
			// notify start
			progress.setProgressMade(0);
			progress.setProgressTotal(1);
			progress.setComputationState(ComputationState.STARTED);
			
			ComputationResult result = new ComputationResult(algoInst, progress);
	
			File tmpFile = null;
			
			try {
	
				tmpFile = File.createTempFile("genlab_tmp_", ".net");
				
				fileSink.writeAll(
						GraphstreamConvertors.getGraphstreamGraphFromGenLabGraph(inputGraph, result.getMessages()), 
						tmpFile.getAbsolutePath()
						);
	
			} catch (IOException e) {
				e.printStackTrace();
				// TODO !!!
			}
			
			// ended !
			result.setResult(AbstractGraphStreamGraphWriter.OUTPUT_FILE, tmpFile);
	
			progress.setProgressMade(1);
			progress.setComputationState(ComputationState.FINISHED_OK);
	
			setResult(result);
			
		} catch (Exception e) {
			e.printStackTrace();
			progress.setComputationState(ComputationState.FINISHED_FAILURE);
			// TODO store info exception
		}
	}

	
	

}