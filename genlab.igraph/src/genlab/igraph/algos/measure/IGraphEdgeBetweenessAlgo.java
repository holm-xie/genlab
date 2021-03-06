package genlab.igraph.algos.measure;

import genlab.core.exec.IExecution;
import genlab.core.model.exec.IAlgoExecution;
import genlab.core.model.instance.AlgoInstance;
import genlab.core.model.meta.InputOutput;
import genlab.core.model.meta.basics.flowtypes.SimpleGraphFlowType;
import genlab.core.model.meta.basics.graphs.IGenlabGraph;
import genlab.core.parameters.StringParameter;
import genlab.igraph.algos.generation.lcffamous.AbstractLCFFamousGraph;
import genlab.igraph.commons.GenlabProgressCallback;
import genlab.igraph.commons.IgraphLibFactory;
import genlab.igraph.natjna.IGraphNativeLibrary;
import genlab.igraph.natjna.IGraphRawLibrary;
import genlab.igraph.natjna.IIGraphProgressCallback;
import genlab.igraph.parameters.ChoiceOfImplementationParameter.EIgraphImplementation;

/**
 * Groups everything related to components in the igraph library
 * 
 * TODO warning this is a edge betweeness 
 * TODO extract to an indenpendant class
 * 
 * @author Samuel Thiriot
 *
 */
public class IGraphEdgeBetweenessAlgo extends AbstractIGraphMeasure {


	public static final StringParameter PARAM_ATTRIBUTE_NAME = new StringParameter(
			"attribute_name", 
			"attribute name", 
			"the name of the attribute of edges which will store the value", 
			"igraph_edge_betweeness"
			); 
	
	
	public static final InputOutput<IGenlabGraph> OUTPUT_GRAPH = new InputOutput<IGenlabGraph>(
			SimpleGraphFlowType.SINGLETON, 
			"out_graph", 
			"graph", 
			"the graph with betweeness"
	);
	
	
	public IGraphEdgeBetweenessAlgo() {
		super(
				"edge betweeness (igraph)", 
				"measure edge betweeness centrality using the igraph implementation",
				null // no preference for the implementation
				);

		outputs.add(OUTPUT_GRAPH);
		
		registerParameter(PARAM_ATTRIBUTE_NAME);

	}

	@Override
	public IAlgoExecution createExec(IExecution execution,
			final AlgoInstance algoInstance) {
		
		return new IGraphEdgeBetweenessExec(execution, algoInstance, algoInstance);
	}

}
