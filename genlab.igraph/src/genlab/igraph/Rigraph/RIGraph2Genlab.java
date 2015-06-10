package genlab.igraph.Rigraph;

import genlab.core.commons.FileUtils;
import genlab.core.commons.WrongParametersException;
import genlab.core.model.meta.basics.graphs.AbstractGraphstreamBasedGraph;
import genlab.core.model.meta.basics.graphs.GraphDirectionality;
import genlab.core.model.meta.basics.graphs.IGenlabGraph;
import genlab.core.usermachineinteraction.GLLogger;
import genlab.core.usermachineinteraction.ListOfMessages;
import genlab.graphstream.algos.generators.IGenlabGraphInitializer;
import genlab.graphstream.utils.GraphstreamConvertors;
import genlab.r.rsession.Genlab2RSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGraphML;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceGML;
import org.math.R.Rsession;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;

public class RIGraph2Genlab {

	public RIGraph2Genlab() {
		// TODO Auto-generated constructor stub
	}
	
	protected static File saveGraphFromRIgraphToFile(Rsession r, String variableGraph) {

		File tmpFile = FileUtils.createTmpFile("Rsession", "graph");
		
		String targetFileName = tmpFile.getAbsolutePath();
		
		StringBuffer command = new StringBuffer();
		command.append("write.graph(");
		command.append(variableGraph).append(", \"");
		command.append(targetFileName).append("\", ");
		command.append("format=\"gml\""); // , isolates=TRUE , isolates=TRUE
		command.append(")");
		
		// ask R to write the file
		r.eval(command.toString(), true);
		
		if (r.status == Rsession.STATUS_ERROR)
			throw new RuntimeException("the file was not created as expected");
		
		// check the existence of the file
		if (!tmpFile.isFile() || !tmpFile.exists()) {
			throw new RuntimeException("the file was not created as expected");
		}
		// here we might detect if the file is empty
		// if (tmpFile.getTotalSpace() == 0) {
		
		return tmpFile;
		
	}
	
	/**
	 * Reads a file generated by igraph from a file
	 * @param graphFile
	 * @param messages
	 * @return
	 */
	protected static IGenlabGraph readGraphFromLGL(File graphFile, ListOfMessages messages, GraphDirectionality directionality) {
		
		
		// create a filesource of the right format
		final FileSource filesource = new FileSourceGML();
		
		// init our sink which will process events from the filesource
		GraphstreamConvertors.GenLabGraphSink ourSink = new GraphstreamConvertors.GenLabGraphSink(
				"opened", 
				messages, 
				directionality,
				new IGenlabGraphInitializer() {
					@Override
					public void initGraph(IGenlabGraph glGraph) {
						// ingore the graph attributes errors, as Igraph is going to create plenty
						((AbstractGraphstreamBasedGraph)glGraph).ignoreGraphAttributeErrors = true;
					}
				}
				);
		
		// that we listen to
		filesource.addSink(ourSink);

		// attempt to open the file
		InputStream is;
		try {
			is = new FileInputStream(graphFile);
		} catch (FileNotFoundException e) {
			throw new WrongParametersException("Unable to load file from " + graphFile.getAbsolutePath() +
				" (" + e.getLocalizedMessage() + ")");
		}
		
		// actually load the graph
		try {
			filesource.begin(is);
			while (filesource.nextEvents()) {
				// nothing to do
				// the file source is just transmitting events that are loaded inside our sink
			}
			filesource.end();
		} catch (IOException e) {
			throw new WrongParametersException("Error while parsing a graph from " + 
					graphFile.getAbsolutePath()+ " (" + e.getLocalizedMessage() + ")");
		} catch (Exception e) {
			throw new WrongParametersException("Error while parsing a graph from " +
					graphFile.getAbsolutePath()+ " (" + e.getLocalizedMessage() + ")");
		} catch(Throwable e) {
			throw new WrongParametersException("Error while parsing a graph from " +
					graphFile.getAbsolutePath()+ " (" + e.getLocalizedMessage() + ")");
		} 
		
		return ourSink.getGraph();
		
	}
	

	/**
	 * Reads a file generated by igraph from a file
	 * @param graphFile
	 * @param messages
	 * @return
	 */
	public static File saveGraphToGML(IGenlabGraph graph, ListOfMessages messages) {
		

		File tmpFile = null;
		
		FileSink fileSink = new FileSinkGraphML();
				
		try {

			tmpFile = FileUtils.createTmpFile("Rsession", "graph");
			
			fileSink.writeAll(
					GraphstreamConvertors.getGraphstreamGraphFromGenLabGraph(graph, messages), 
					tmpFile.getAbsolutePath()
					);
			

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("error while writing graph for transmission in R", e);
		}
	
		return tmpFile;
	}
	
	public static IGenlabGraph loadGraphFromRIgraph(
			Rsession r, String variableGraph, boolean isMultiGraph, ListOfMessages messages) {
		return loadGraphFromRIgraph(r, variableGraph, isMultiGraph, messages, null);
	}
	/**
	 * Will ask igraph to save the graph as a file, and then will read this file into a Genlab graph.
	 * @param r
	 * @param variableGraph
	 * @param isMultiGraph
	 * @param messages
	 * @return
	 */
	public static IGenlabGraph loadGraphFromRIgraph(
			Rsession r, String variableGraph, boolean isMultiGraph, ListOfMessages messages, GraphDirectionality directionality) {
		
		//long timestampStart = System.currentTimeMillis();

		
		// evaluate the basic characteristics of the graph so we can check it is not all wrong
		int nVertices;
		int nEdges;
		boolean directed = false;
		try {
			REXP exp = r.eval("vcount("+variableGraph+")");
			nVertices = exp.asInteger();
			exp = r.eval("ecount("+variableGraph+")");
			nEdges = exp.asInteger();
			if (directionality == null) {
				// automatic detection from the graph
				exp = r.eval("is.directed("+variableGraph+")");
				directed = ((REXPLogical)exp).isTRUE()[0];
			}
		} catch (REXPMismatchException e) {
			throw new RuntimeException("error while attempting to get the size of the graph from R", e);
		}
		
		GraphDirectionality directi = null;
		if (directionality == null)
			directi = directed?GraphDirectionality.DIRECTED:GraphDirectionality.UNDIRECTED;
		else 
			directi = directionality;
		
		// write to a file
		File fileGraph = saveGraphFromRIgraphToFile(r, variableGraph);
		
		// read from this file
		IGenlabGraph readGraph = readGraphFromLGL(fileGraph, messages, directi);
		
		// delete this file which is now useless
		fileGraph.delete();
		
		if (readGraph.getVerticesCount() != nVertices) {
			throw new WrongParametersException("wrong number of vertices after conversion: we expected "+nVertices+" but we got "+readGraph.getVerticesCount()+" after conversion");
		}
		if (readGraph.getEdgesCount() != nEdges) {
			throw new WrongParametersException("wrong number of vertices after conversion: we expected "+nEdges+" but we got "+readGraph.getEdgesCount()+" after conversion");
		}
		
		//long totaltime = System.currentTimeMillis() - timestampStart;
		//System.err.println("R-igraph -> genlab: "+totaltime+" for "+nEdges+" edges");
		
		
		return readGraph;
	}
	

	protected static void loadGraphInRIgraphFromFile(Rsession r, String variableGraph, File fileGraph) {
		
		String targetFileName = fileGraph.getAbsolutePath();
		
		
		StringBuffer command = new StringBuffer();
		command.append(variableGraph).append(" <- ");
		command.append("read.graph(\"");
		command.append(targetFileName).append("\", ");
		command.append("format=\"graphml\""); 
		command.append(")");
		
		// ask R to write the file
		r.eval(command.toString(), true);
		Genlab2RSession.checkStatus(r);
		
		
		
	}
	
	private static int BUFFER_EDGES_SIZE = 100;
	
	/**
	 * A copy which is not using a file but is doing everything by command line.
	 * Not really more rapid than writing a file. 
	 * @param graph
	 * @param r
	 * @param variableGraph
	 */
	private static void copyGraphToGraph(IGenlabGraph graph, Rsession r, String variableGraph) {
		
		// create empty graph
		{
			StringBuffer command = new StringBuffer();
			command
				.append(variableGraph).append(" <- ").append("graph.empty(n=")
				.append(graph.getVerticesCount())
				.append(", directed=").append(graph.getDirectionality()==GraphDirectionality.DIRECTED?"TRUE":"FALSE").append(")");
			
			r.eval(command.toString());
		}
		
		// create edges
		{
			final String cmdStart = (new StringBuffer()).append(variableGraph).append(" <- add.edges(").append(variableGraph).append(", ").toString();
			final String cmdEnd = ") )";
			StringBuffer cmdEdges = new StringBuffer(BUFFER_EDGES_SIZE*6);
			int nbEdgesBuffered = 0;
			for (String edgeId: graph.getEdges()) {
				if (nbEdgesBuffered > 0)
					cmdEdges.append(",");
				cmdEdges.append(graph.getEdgeVertexFrom(edgeId));
				cmdEdges.append(",");
				cmdEdges.append(graph.getEdgeVertexTo(edgeId));
				
				// empty buffer
				if (nbEdgesBuffered >= BUFFER_EDGES_SIZE) {
					StringBuffer cmd = new StringBuffer();
					cmd.append(cmdStart);
					cmd.append(cmdEdges);
					cmd.append(cmdEnd);
					r.eval(cmd.toString());
					nbEdgesBuffered = 0;
					cmdEdges = new StringBuffer(BUFFER_EDGES_SIZE*6);
				}
			}
			// empty buffer
			if (nbEdgesBuffered > 0) {
				StringBuffer cmd = new StringBuffer(nbEdgesBuffered*6);
				cmd.append(cmdStart);
				cmd.append(cmdEdges);
				cmd.append(cmdEnd);
				r.eval(cmd.toString());
			}
		}
		
	}
	
	public static void loadGraphToRIgraph(IGenlabGraph graph, Rsession r, String variableGraph, ListOfMessages messages) {
		
		//long timestampStart = System.currentTimeMillis();
		
		// write the graph to a file
		File fileGraph = saveGraphToGML(graph, messages);
		
		// ask R to load this graph from the file
		loadGraphInRIgraphFromFile(r, variableGraph, fileGraph);

		// delete this file which is now useless
		fileGraph.delete();
			
		//copyGraphToGraph(graph, r, variableGraph);
		
		// evaluate the basic characteristics of the graph so we can check it is not all wrong
		int nVertices;
		int nEdges;
		try {
			REXP exp = r.eval("vcount("+variableGraph+")");
			Genlab2RSession.checkStatus(r);
			nVertices = exp.asInteger();
			exp = r.eval("ecount("+variableGraph+")");
			Genlab2RSession.checkStatus(r);
			nEdges = exp.asInteger();
		} catch (REXPMismatchException e) {
			throw new RuntimeException("error while attempting to get the size of the graph from R", e);
		}
		
		if (graph.getVerticesCount() != nVertices) {
			throw new WrongParametersException("wrong number of vertices after conversion: we expected "+graph.getVerticesCount()+" but we got "+nVertices+" after conversion");
		}
		if (graph.getEdgesCount() != nEdges) {
			throw new WrongParametersException("wrong number of vertices after conversion: we expected "+graph.getEdgesCount()+" but we got "+nEdges+" after conversion");
		}
		
		// TODO report to execution ?
		//long totaltime = System.currentTimeMillis() - timestampStart;
		//System.err.println("genlab -> R-igraph: "+totaltime+" for "+nEdges+" edges");
		
	}

}
