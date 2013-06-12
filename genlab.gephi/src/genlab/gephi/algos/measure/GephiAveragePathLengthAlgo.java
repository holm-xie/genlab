package genlab.gephi.algos.measure;

import genlab.core.exec.IExecution;
import genlab.core.model.exec.IAlgoExecution;
import genlab.core.model.exec.IComputationProgress;
import genlab.core.model.instance.AlgoInstance;
import genlab.core.model.meta.IInputOutput;
import genlab.core.model.meta.InputOutput;
import genlab.core.model.meta.basics.flowtypes.DoubleFlowType;
import genlab.core.model.meta.basics.flowtypes.SimpleGraphFlowType;
import genlab.core.model.meta.basics.graphs.GraphDirectionality;
import genlab.core.model.meta.basics.graphs.IGenlabGraph;
import genlab.gephi.utils.GephiConvertors;
import genlab.gephi.utils.GephiGraph;

import java.util.HashMap;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

/**
 * @see http://wiki.gephi.org/index.php/Avg_Path_Length
 * @see http://gephi.org/docs/toolkit/org/gephi/statistics/plugin/GraphDistance.html
 * 
 * @author Samuel Thiriot
 *
 */
public class GephiAveragePathLengthAlgo extends GephiAbstractAlgo {

	public static final InputOutput<IGenlabGraph> OUTPUT_GRAPH = new InputOutput<IGenlabGraph>(
			new SimpleGraphFlowType(), 
			"TODO.graph", 
			"graph", 
			"the graph with betweeness"
	);
	
	public static final InputOutput<Double> OUTPUT_AVERAGE_PATH_LENGTH = new InputOutput<Double>(
			new DoubleFlowType(), 
			"TODO.average_path_length", 
			"average path length", 
			"the average length of the shortest pathes"
	);
	

	public static final InputOutput<Double> OUTPUT_DIAMETER = new InputOutput<Double>(
			new DoubleFlowType(), 
			"TODO.diameter", 
			"diameter", 
			"the diameter, that is the longest shortest path in the graph. "
	);
	
	public GephiAveragePathLengthAlgo() {
		super(
				"Gephi Brandes", 
				"computes the average path length"
				);
		
		outputs.add(OUTPUT_AVERAGE_PATH_LENGTH);
		outputs.add(OUTPUT_DIAMETER);
		outputs.add(OUTPUT_GRAPH);
	}
	

	@Override
	public IAlgoExecution createExec(IExecution execution,
			AlgoInstance algoInstance) {
		
		return new GephiAbstractAlgoExecution(execution, algoInstance) {
			
			@Override
			protected Map<IInputOutput<?>, Object> analyzeGraph(
					IComputationProgress progress, 
					GephiGraph gephiGraph,
					IGenlabGraph genlabGraph) {
				
				final String param_betweeness_attribute = "betweeness";
				
				Map<IInputOutput<?>, Object> results = new HashMap<IInputOutput<?>, Object>();
				
				GraphDistance algo = new GraphDistance();
				algo.setDirected(genlabGraph.getDirectionality() == GraphDirectionality.DIRECTED);
				
				// TODO warning if mixed ???
				
				// TODO progress
				
				algo.execute(
						gephiGraph.graphModel,
						gephiGraph.attributeModel
						);

				
				
				System.err.println("average path length: "+algo.getPathLength());
				System.err.println("diameter: "+algo.getDiameter());
				
				results.put(OUTPUT_AVERAGE_PATH_LENGTH, algo.getPathLength());
				results.put(OUTPUT_DIAMETER, algo.getDiameter());
				
				if (isUsed(OUTPUT_GRAPH) && param_betweeness_attribute != null ) {
					
					IGenlabGraph outputGraph = genlabGraph.clone("cloned"); // TODO graph id ?!
					
					final AttributeColumn col = gephiGraph.attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
					outputGraph.declareVertexAttribute(param_betweeness_attribute, Double.class);
					
					//Iterate over values
					for (Node n : gephiGraph.graph.getNodes()) {
					   Double centrality = (Double)n.getNodeData().getAttributes().getValue(col.getIndex());
					   outputGraph.setVertexAttribute(
							   n.getNodeData().getLabel(),
							   param_betweeness_attribute,
							   centrality
							   );
					}
					
					results.put(OUTPUT_GRAPH, outputGraph);
					
				}
				
				return results;
				
			}

		};
	}

}