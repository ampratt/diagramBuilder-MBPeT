package com.example.diagramtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.diagrambuilder.Connector;
import org.vaadin.diagrambuilder.DiagramBuilder;
import org.vaadin.diagrambuilder.DiagramStateEvent;
import org.vaadin.diagrambuilder.Node;
import org.vaadin.diagrambuilder.NodeType;
import org.vaadin.diagrambuilder.Transition;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.ui.Notification;

@SuppressWarnings("serial")
public class DBuilderUtils implements Serializable {

    List<Integer> nRename = new ArrayList<Integer>();
    List<Integer> nName = new ArrayList<Integer>();
    List<String> nType = new ArrayList<String>();
    List<int[]> nXy = new ArrayList<int[]>();
    List<Transition> nTransitions = new ArrayList<Transition>();
    List<Connector> nConnectorName = new ArrayList<Connector>();
	String title = "input_title_name";

	public DBuilderUtils() {
		// TODO Auto-generated constructor stub
	}

    public void initDiagram(DiagramBuilder diagramBuilder) {

        // Initialize diagram builder component
//        diagramBuilder = new DiagramBuilder();
        diagramBuilder.setAvailableFields(
		        new NodeType("diagram-node-start-icon", "Start", "start"),
		        new NodeType("diagram-node-state-icon", "State", "state"),
		        new NodeType("diagram-node-end-icon", "End", "end")
        );
        
        diagramBuilder.setFields(
        		new Node("1", "start",10,10),
        		new Node("2", "state",80,120),
        		new Node("3", "state",300,55),
        		new Node("4", "state",235,163),
        		new Node("5","end", 421,185)
        		);

        diagramBuilder.setTransitions(
    			new Transition("1", "2", "first connector - 0.60 / / browse()"),
    			new Transition("1", "3", "0.50 / / bid(id,price,username,password)"),
    			new Transition("2", "3", "0.30 / / exit()")
    	);

        diagramBuilder.setSizeFull();
//        layout.addComponent(diagramBuilder);
        setGraphTitle("input_title_name");
    }
    
    public void setGraphTitle(String t) {
    	title = t;
    }
    public String getGraphTitle() {
    	return title;
    }

    /*
     * change alloyui default node names to incremented int values
     */
    public List<Node> renameNodes(List<Node> nodes) {	//, DiagramBuilder diagramBuilder   DiagramStateEvent event
//    	List<Node> nodes = event.getNodes();

//    	List<String[]> transitionsRenamed = new ArrayList<String[]>();
    	List<String> oldNNames = new ArrayList<String>();
    	List<String> newNNames = new ArrayList<String>();
    	
        nRename.clear();
        nTransitions.clear();
        nXy.clear();					// Testing only. not used

        // get nodes
		for (int n=0; n < nodes.size(); n++) {
			
			if ( !nodes.get(n).getName().equals(String.valueOf(n+1)) ) {
				if (!oldNNames.contains(nodes.get(n).getName())) {
					oldNNames.add(nodes.get(n).getName());
					newNNames.add(String.valueOf(n+1));
//				String[] oldNewTrans = new String[] {nodes.get(n).getName(), String.valueOf(n+1)};
//				transitionsRenamed.add(oldNewTrans);
				}
//				System.out.println("node.getName: " + nodes.get(n).getName()
//									+ " - valofeof(n): " + String.valueOf(n+1));
			}
			// get state and transition data for each node
			nRename.add(n+1);
			nodes.get(n).setName(String.valueOf(n+1));
			System.out.println("nodes.get(n) " + nodes.get(n).getName());
//			System.out.println("node: " + nodes.get(n) + " is named: " + nRename.get(n));
		}
		Collections.sort(nRename);  
		
		// get transitions
		for (int n=0; n < nodes.size(); n++) {
			// get transition data for each node
			List<Transition> t = nodes.get(n).getTransitions();		//System.out.println("t is: " + mapper.writeValueAsString(t));
        	
			nTransitions.addAll(t);
        	nXy.add(nodes.get(n).getXy());
		}
			
			// rewrite transition default names to numbers
			for (Transition t: nTransitions) {	//int n=0; n<nTransitions.size(); n++
				if (!t.getSource().matches("\\d+")) {		//( StringUtils.isNumeric(t.get(n).getSource()) ) {
					System.out.println("t.get(n).getSource() was: " + t.getSource() + "->" + t.getTarget());
	
					//replace auto-gen value with correct int
					if ( oldNNames.contains(t.getSource()) ) {
						int index = oldNNames.indexOf(t.getSource());
						t.setSource(newNNames.get(index));
						System.out.println("\nSOURCE\noldNNames at index: " + index + " is " + oldNNames.get(index) +
											"\nnewNName at index: " + index + " is " + newNNames.get(index) );
					}
				}
				if (!t.getTarget().matches("\\d+")) {		//( StringUtils.isNumeric(t.get(n).getSource()) ) {
					System.out.println("t.get(n).getTarget() was: " + t.getSource() + "->" + t.getTarget());
					//replace auto-gen value with correct int
					if ( oldNNames.contains(t.getTarget()) ) {
						int index = oldNNames.indexOf(t.getTarget());
						t.setTarget(newNNames.get(index));
						System.out.println("\nTARGET\noldNNames at index: " + index + " is " + oldNNames.get(index) +
											"\nnewNName at index: " + index + " is " + newNNames.get(index) );
					}
				}	
			}
//		generateDiagramAfterRename(diagramBuilder, nodes);
		return nodes;
    }
    
    
    public void generateDiagramAfterRename(DiagramBuilder diagramBuilder, List<Node> nodes) {
		
        // Initialize field types
        diagramBuilder.setAvailableFields(
		        new NodeType("diagram-node-start-icon", "Start", "start"),
		        new NodeType("diagram-node-state-icon", "State", "state"),
		        new NodeType("diagram-node-end-icon", "End", "end")
        );
        
        // Initialize states (nodes). use names from file and auto-generated location (x,y) values
        Node[] newNodes = new Node[nodes.size()];
        for (int n=0; n<nodes.size(); n++) {	//(int n=0; n<nodes.length; n++) {
//        	String type = "state";
        	if (n==0) {	//start node
        		nodes.get(n).setType("start");
//        		type = "start";
        	} else if(n == nodes.size()-1) {	//end node
        		nodes.get(n).setType("end");
//        		type = "end";
        	} else {
        		nodes.get(n).setType("state");
        	}
        	
        	Node node = new Node(nodes.get(n).getName(), 
        			nodes.get(n).getType(), 
        			nodes.get(n).getX(), 
        			nodes.get(n).getY()
        	);
        	System.out.println(nodes.get(n).getName() +
        			nodes.get(n).getType() +
        			nodes.get(n).getX() +
        			nodes.get(n).getY());
        	newNodes[n] = node;
        }
        // set fields to diagram with nodes from input
        diagramBuilder.setFields(newNodes);
        
        // Initialize transitions and connection labels
//        Transition[] trans = new Transition[nTransitions.size()];
//        for (int i=0; i<nTransitions.size(); i++) {
//        	// create transition
//        	Transition t = new Transition(
//        			nTransitions.get(i).getSource(),
//        			nTransitions.get(i).getTarget(),
//        			nTransitions.get(i).getConnector()
//        	);	
//        	System.out.println(nTransitions.get(i).getSource() +
//        			nTransitions.get(i).getTarget() +
//        			nTransitions.get(i).getConnector());
//        	// add to array of transitions
//        	trans[i] = t;
//        }
        Transition[] ts = new Transition[nTransitions.size()];
        nTransitions.toArray(ts); 
        // set transitions to diagram
        diagramBuilder.setTransitions(ts);	//trans

        diagramBuilder.setSizeFull();	// layouting		
	}

	/*
     * Retrieve data from diagram and write to file in .dot format
     */
	public void saveToFile(List<Node> ns, String fileName, String title) {	// DiagramStateEvent event    reportStateBack
        // get nodes from diagram
		List<Node> nodes = renameNodes(ns);
        
		// mapper for window notification display
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
//            String writeValueAsString = mapper.writeValueAsString(nodes);	//event.getNodes()
            
            nName.clear();
            nTransitions.clear();
            nXy.clear();					// Testing only. not used
//	            nType.clear();				// Testing only. not used
//	            nConnectorName.clear();		// Testing only. not used
            for (int n=0; n < nodes.size(); n++) {
            	// get state and transition data for each node
            	nName.add( Integer.parseInt(nodes.get(n).getName()) );
            	List<Transition> t = nodes.get(n).getTransitions();		//System.out.println("t is: " + mapper.writeValueAsString(t));
            	nTransitions.addAll(t);
            	nXy.add(nodes.get(n).getXy());
            	
//            	System.out.println("THE XY VALUE WAS" + mapper.writeValueAsString(nodes.get(n).getXy().toString()));
            	// Testing only. not used
//            	nType.add(nodes.get(n).getType());	// not used
//                for (int c=0; c < t.size(); c++) {
//                	nConnectorName.add(t.get(c).getConnector());
//                }
            }
            Collections.sort(nName);

            if (title.equals("") || title.equals(null)){
            	title = "use_case_abc";
            	System.out.println("there was no title data");
            }
            if (title.contains(" ")){
            	title.replaceAll(" ", "_");
            }
            // call method to parse data to dot file
            writeToDotFile(nName, nTransitions, nXy, fileName, title);		

            // display some confirmation to user
//            String writeNodeNames = mapper.writeValueAsString(nName);
//            Notification.show("State reported: ",
//            		"Node Names:\n" + writeNodeNames +
//            		"\n\nStates:\n" + writeValueAsString,
//            		Notification.Type.ERROR_MESSAGE);

            // Testing only. print data to console
//            System.out.println(nName);
//            System.out.println(mapper.writeValueAsString(nType));
//            System.out.println("this is the List<int[]> xy" + mapper.writeValueAsString(nXy));
//            System.out.println("transitions: " + mapper.writeValueAsString(nTransitions));
//            System.out.println("transitions names: " + mapper.writeValueAsString(nConnectorName));
//            System.out.println("Node Names:\n" + writeNodeNames);
//            System.out.println("States:\n" + writeValueAsString);
            
        } catch (JsonProcessingException ex) {
            Logger.getLogger(
                    DiagramtestUI.class.
                    getName()).
                    log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
	
	/*
	 * Actual writing to file from diagram data.
	 * Called automatically from saveToFile()
	 */
	public void writeToDotFile(List<Integer> nName, List<Transition> nTransitions, List<int[]> nXy, String fileName, String title) throws JsonProcessingException, FileNotFoundException {	
		ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String writeValueAsString = "";	// = mapper.writeValueAsString(nodes);	//event.getNodes()

        // create file
//		fileName = "C:/dev/output/dot-output.dot";
        File file = new File(fileName);
        PrintWriter writer = new PrintWriter(file);
        writer.println("digraph " + title + " {");
    
            // write state data
            writer.println("\t// States");
            for (int n=0; n < nName.size(); n++) {
                writer.print("\t" + nName.get(n));
                
                // save X,Y coordinate values
                String xy = mapper.writeValueAsString(nXy.get(n));

                // a little formatting
                xy = xy.replace("[", "[ coords ");
                xy = xy.replaceAll("[,]", " ,");
//                xy = xy.replaceAll("[\\[\\]]", "");
//                xy = xy.replaceAll("[\\s]", "");
                writer.print("\t" + xy);// "}\n");	//mapper.writeValueAsString(nXy.get(n))
                writer.print("\n");
                System.out.println("XY values: \n" + xy);		// for testing
            }            
            
            // write transition data (nodes and label info)
            writer.println("\n\t// Transitions");
            for (int n=0; n < nTransitions.size(); n++) {
            	Transition t = nTransitions.get(n);
	            	// insert '"' if not present in transition labels
	            	String label = t.getConnector().getName().toString();
	            	if (!(label.charAt(0)=='\"')) { 
	            		label = "\"" + label;
					}
					if (!(label.charAt(label.length()-1)=='\"')) {
						label = label + "\"";
					}
					t.getConnector().setName(label);
				
                writer.println("\t" + t.getSource() + " -> " + t.getTarget() + 
                			   " [label = " + t.getConnector().getName().toString() + "];");	//mapper.writeValueAsString()
            }     
        writer.println("}");     
        writer.close();
	
        //show confirmation to user
        Notification.show("dot file was saved at: " + fileName, Notification.Type.TRAY_NOTIFICATION);;
	}
	
	/*
	 * Read file data in .dot format and generate diagram
	 */
	@SuppressWarnings("resource")
	public void readFromDotFile(DiagramBuilder diagramBuilder, String fileName) throws FileNotFoundException, NullPointerException {
		// Lists to hold state (node) and transition data
		List<Integer> states = new ArrayList<Integer>();
		List<int[]> xyValues = new ArrayList<int[]>();
		List<Transition> actualTransitions = new ArrayList<Transition>();
//		List<List<Integer>> transitions = new ArrayList<>();
//		List<Node> actualNodes = new ArrayList<Node>();
//		List<String> labels = new ArrayList<String>();		// for testing output only
		int transCounter = 0;

		//TODO allow dynamic file input via e.g. text input or embedded editor
		try {
			Scanner sc = new Scanner(new FileReader(fileName));
		    
			// get graph TITLE
			String firstToken = sc.next();
		    if (!firstToken.toLowerCase().contains("graph")) {	//ArrayfirstLine.split(" ").
//		    	System.out.println("first token was: " + firstToken);	// for testing
		    	Notification.show("Heads up!", 
		    						"The first line of your dot file seems to be mis-formatted.\n" +
		    						"Please follow this format: \n\n[graphtype] [graph_name] {", 
		    						Notification.Type.WARNING_MESSAGE);
		    } else {
		    	String t = sc.next();
	    		if (t.contains(" ")) {
	    			t.replace(" ", "_");
	    		}
	    		if (t.contains("{")) {
	    			t.replace("{", "");
	    		}
		    	setGraphTitle(t);
		    }
		    
			// Parse input for states and transitions
			while (sc.hasNext()) {
			    String nextToken = sc.next();
			    
			    
			    // get STATES
			    if (nextToken.equalsIgnoreCase("//states") || nextToken.equalsIgnoreCase("states")){
	//		    	System.out.println("STATES:");				// Testing only. print to console
			    	int count = 1;		// for coord generation
			    	int x=10;
			    	int y=10;
			    	while (sc.hasNextInt()){
			    		// add states to states list
			    		int state = sc.nextInt();
			    		states.add(state);	
			    		System.out.println("state is: " + state);
//			    		Node thisNode = new Node();
//			    		thisNode.setName(Integer.toString(state));
//			    		actualNodes.add(thisNode);

			    		// the next character was not an int, so get embedded coordinates
			    		String restOfLine = sc.nextLine();
			    		if (restOfLine.contains("coords")) {	//!sc.hasNextInt()
			    			
//				    		System.out.println("restofline: " + restOfLine);
//							if ( !(restOfLine.equals("") || restOfLine.equals(null)) || restOfLine.equals(" ")) {
//			    				System.out.println("found '" + restOfLine + "' ... looking for xy");	// for Testing
			    				int[] coords = new int[2];
			    				Scanner lineScanner = new Scanner(restOfLine).useDelimiter("[^0-9]+");
			    				if (lineScanner.hasNextInt()) {		//sc.
			    					coords[0] = lineScanner.nextInt();
			    					coords[1] = lineScanner.nextInt();
				    				System.out.println("IF-IF x,y: " + coords[0] + ", " + coords[1]);		// for Testing
//			    					System.out.println("x,y: " + coords[0] + ", " + coords[1]);		// for Testing
			    				} else {
			    					// user uploaded dot file with additional data such as: [circle = doubleround]
					    			// no coordinate values existed, so generate them
//			    					System.out.println("user uploaded dot file with additional data [circle = doubleround]");	// for Testing
				    				int[] prevCoords = xyValues.get(xyValues.size()-1);
				    				coords[0] = prevCoords[0]+100;
				    				coords[1] = prevCoords[1]+150;
				    				System.out.println("IF-ELSE x,y: " + coords[0] + ", " + coords[1]);		// for Testing
//			    					coords = new int[]{prevCoords[0]+100, prevCoords[1]+150};		//int[] thisXy    {x+=100,y+=175};
//				    				System.out.println("count = " + count);				// for Testing
//				    				System.out.println("x,y amount added: 100, 150");	// for Testing			
//				    				System.out.println("removed unsupported data. thisXy: " + thisXy[0] + ", " + thisXy[1]);	// for Testing
			    				}
			    				System.out.println("count = " + count);				// for Testing
			    				xyValues.add(coords);
			    				lineScanner.close();
			    				count ++;
//							}
			    		} else {
			    			// no coordinate values existed, so generate them
			    			int[] thisXy; // = new int[2];
			    			if (count == 1) {		
			    				// first node
			    				thisXy = new int[]{x+=10,y+=10};	//int[] thisXy = {x+=10,y+=10};
//			    				xyValues.add(thisXy);
//			    				System.out.println("x,y amount added: 10, 10");		// for Testing	    				
			    			
			    			} else if (count % 3 == 0) {
			    				// every 3rd node
			    				int[] prevCoords = xyValues.get(xyValues.size()-1);
			    				thisXy = new int[]{prevCoords[0]+85, prevCoords[1]/2};	//{x+=85,y/=2};
//			    				xyValues.add(thisXy);
//			    				System.out.println("x,y amount added: 110, y/=2");	// for Testing    				
			    			} else {
			    				int[] prevCoords = xyValues.get(xyValues.size()-1);
			    				thisXy = new int[]{prevCoords[0]+100, prevCoords[1]+135};	//{x+=100,y+=135};
//			    				xyValues.add(thisXy);
//			    				System.out.println("x,y amount added: 110, 150");	// for Testing			
			    			}
	    					System.out.println("ELSE x,y: " + thisXy[0] + ", " + thisXy[1]);		// for Testing
	    					System.out.println("count = " + count);				// for Testing
	    					xyValues.add(thisXy);
		    				count ++;  					    			
//		    				System.out.println("x,y amount added: 10, 10");		// for Testing	    				
//		    				System.out.println("thisXy: " + x + ", " + y);		// for Testing		

			    		}
			    	}
//		    	System.out.println("states were: " + states);	// Testing only. print to console
//		    	System.out.println("states length: " + states.size());	// Testing only. print to console
//		    	System.out.println("xy values length: " + xyValues.size());	// Testing only. print to console
//		        int[] lastXY = xyValues.get((xyValues.size()-1));
//		        System.out.println("LAST xyValues: " + lastXY[0] + lastXY[1]);
			    	
			    } // end of STATES
			    
			   
			    
			    
			    // get TRANSTIONS
			    if (nextToken.equalsIgnoreCase("//transitions") || nextToken.equalsIgnoreCase("transitions")){
	//				System.out.println("TRANSITIONS:");			// Testing only. print to console			    	
			    	sc.useDelimiter("\\s*\\D+\\s*");	//("\\s*[^0-9]+\\s*");		"\\s*->\\s*"
			    	while (sc.hasNextInt()) {
			    		// get x values
			    		int x = sc.nextInt();
			    		System.out.println("x is: "+ x);		// Testing only. print to console s.nextInt());
//			    		List<Integer> t = new ArrayList<>();	// temp storage for current transition pair
//			    		t.add(x);
			    		
			    		// get y values after "->" sign
//			    			if (sc.next().equals("->")) {
		    			int y = sc.nextInt();
		    			System.out.println("y is: "+ y);		// Testing only. print to console s.nextInt());
//			    		t.add(y);
		    			
		    			// add x,y value to transitions list
						Transition thisTrans = new Transition();
						thisTrans.setSource(Integer.toString(x));
						thisTrans.setTarget(Integer.toString(y));
						actualTransitions.add(thisTrans);
//				    	transitions.add(t);
//		    			System.out.println("this transition is: " + t);			// Testing only. print to console
//		    			System.out.println("transition(s) are: " + transitions);// Testing only. print to console
		    			
		    			// if the line has more (i.e. label info) get the label data
//    					String restOfLine = sc.nextLine();
//				    	System.out.println("restofline: " + restOfLine + "\n");
//						if ( !(restOfLine.equals("") || restOfLine.equals(null)) || restOfLine.equals(" ")) {

						Connector conn = new Connector();

						sc.useDelimiter("\\s+");
						String nextLine = sc.nextLine();
						System.out.println("NEXTLINE (" + nextLine + ")");
						if ( (nextLine.equals(null) || nextLine.equals("") || nextLine.equals(" ")
								&& !nextLine.contains(";"))) {
							System.out.println("WE ENTERED THE FIRST IF");
							sc.useDelimiter("\\s*\\D+\\s*");
							//TODO insert automatic label
//							labels.add("\"no label data temporary label\"");
		    				conn.setName("\"no label data temporary label\"");
    						transCounter++;
    						System.out.println("The Transition label is: no label data temporary label");				// Testing only. print to console

						} else {
			    			if ((nextLine.contains("label"))) {	//!sc.hasNextInt()		//(s.hasNext()) { 
	    						System.out.println("SEARCHING THROUGH NEXTLINE");				// Testing only. print to console
			    				boolean firstRound = true;
			    				boolean endReached = false;
			    				Scanner lineScanner = new Scanner(nextLine);
			    				while (!endReached==true){		// while (!(sc.hasNextInt())){	sc.hasNext()
//			    					sc.useDelimiter("\\s+");
			    					
			    					// get label data after '=' character
			    					char c = lineScanner.findInLine(".").charAt(0);	//sc
			    					if ( c == '=' ) {
			    						String label = "";
			    						while (!( c == ']' )) {
					    					c = lineScanner.findInLine(".").charAt(0);	//sc
					    					if ( c == ']') break;	// break loop if label data is complete and proceed to next line
					    					if (firstRound == true && (c == ' ')) {
					    						// skip adding leading whitespace to label
					    					} else {
//					    						if (!(c=='\"')) {	//if (!(c=='\"'))
//					    						if (label.equals("") && !(c == '\"')) {
//					    							label += '\"';
//					    						}
					    							// add label data one char at a time
					    							label += c;					    							
//					    						}
					    					}
					    					firstRound = false;		    								    					
			    						}
			    						if (!(label.charAt(0)=='\"')) {
			    							label = "\"" + label;
			    						}
			    						if (!(label.charAt(label.length()-1)=='\"')) {
			    							label = label + "\"";
			    						}
			    						System.out.println("LABEL FORMATTED TO: " + label);
			    						// add label data to connection label list
//			    						labels.add(label);	
			    						conn.setName(label);
			    						transCounter++;
			    						System.out.println("The Transition label is: " + label);				// Testing only. print to console
			    						endReached = true;
			    					} 
			    					if ( c == ';' && firstRound==true ) {
			    						System.out.println("WE GOT INTO THE ';' CONDITION");
			    						transCounter++;
//			    						labels.add("\"the if temporary label\"");
			    						conn.setName("\"the if temporary label\"");
			    						endReached = true;
			    						break;		// break loop at the end of the line
			    					} else if (c == ';' && firstRound==false) {
			    						endReached = true;
			    						break;
		    						}	
			    					if (c =='}') break;
			    				}
			    				lineScanner.close();
			    			} else {
	    						System.out.println("ENTERED else. NEXTLINE ONLY HAD ';'");				// Testing only. print to console
//			    				labels.add("\"the else temporary label\"");
			    				conn.setName("\"the else temporary label\"");
	    						transCounter++;
	    						System.out.println("The Transition label is: the else temporary label");				// Testing only. print to console
			    			} // next token not now Int on next line
						}
			    			System.out.println("NUMBER OF TRANS: "+ transCounter + "\n");
		    				thisTrans.setConnector(conn);
	    				sc.useDelimiter("\\s*\\D+\\s*");	
//			    		}
			    	}
				} // end of TRANSTIONS
			    
			} // end of Scanner loop
			
			sc.close(); 

			// Testing only
	    	System.out.println("states were: " + states);	// Testing only. print to console
	    	System.out.println("states legth: " + states.size());	// Testing only. print to console
	    	System.out.println("xy values length: " + xyValues.size());	// Testing only. print to console
	    	System.out.println("Transition size: " + actualTransitions.size());	// Testing only. print to console
//	    	System.out.println("transition(s) coords are: " + transitions);// Testing only. print to console
//	    	System.out.println("The Transition label(s) size: " + labels.size());	// Testing only. print to console
//	    	System.out.println("The Transition label(s) are: " + labels);	// Testing only. print to console
	    	System.out.println("Connector 3 x->y is: " + actualTransitions.get(2).getSource() + "->" + actualTransitions.get(2).getTarget());// Testing only. print to console
	    	System.out.println("Connector 3 label is: " + actualTransitions.get(2).getConnector().getName());// Testing only. print to console
	        
			// Generate diagram from parsed dot file
			generateDiagramFromFile(diagramBuilder, states, xyValues, actualTransitions);	//transitions, labels
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getClass());
			e.printStackTrace();
		}
//		return title;
	}
	
	/*
	 * Generate new diagram from file input data
	 */
	public void generateDiagramFromFile(DiagramBuilder diagramBuilder, 
			List<Integer> states, List<int[]> xyValues, 
			List<Transition> actualTransitions) {	//List<List<Integer>> transitions, List<String> labels
//		List<Integer> states = new ArrayList<Integer>();
//	    List<List<Integer>> transitions = new ArrayList<>();
//	    List<String> labels = new ArrayList<String>();
	    
        // Initialize diagram builder component
//        diagramBuilder = new DiagramBuilder();
        
        // Initialize field types
        diagramBuilder.setAvailableFields(
		        new NodeType("diagram-node-start-icon", "Start", "start"),
		        new NodeType("diagram-node-state-icon", "State", "state"),
		        new NodeType("diagram-node-end-icon", "End", "end")
        );
        
        // NODES. use names from file and auto-generated location (x,y) values
        Node[] nodes = new Node[states.size()];
        int[] lastXY = xyValues.get((xyValues.size()-1));
        System.out.println("LAST xyValues: " + lastXY[0] + lastXY[1]);
        for (int n=0; n<states.size(); n++) {	//(int n=0; n<nodes.length; n++) {
        	Node node;
        	int[] coords = new int[2];
        	int x,y, prevX=0, prevY=0; 
        	if (xyValues.get(n).equals(null)) {
        		x = prevX + 100;
        		y = prevY + 135;
            	System.out.println("ALERT! XYvALUE DIDNT EXIST! GENERATING SOME...\n" +
            			"x, y = " + x +", " + y);	
			} else {
               	coords = xyValues.get(n);
               	x = coords[0];
               	y = coords[1];
            	System.out.println("coords = " + x +", " + y);
            	prevX=x;
            	prevY=y;
        	}

        	if (n == 0) {
        		// first node
            	node = new Node(states.get(n).toString(), "start", x, y);	//(x+=10),(y+=10));	//Integer.toString(n+1)
        	} else if (n == nodes.length-1) {
        		// last node
            	node = new Node(states.get(n).toString(), "end", x, y);		//(x+=100),(y+=100)
        	} else if (n % 2 == 0) { 
        		// odd
    		node = new Node(states.get(n).toString(), "state", x, y);		//(x+=110),(y/=2)    (x*=3),(y/=2)   		
        	} else { 
        		// even
    		node = new Node(states.get(n).toString(), "state", x, y);		//(x+=70),(y+=175)        		
        	}
        	nodes[n] = node;	// add to list of all nodes
        }
        // set fields to diagram with nodes from input
        diagramBuilder.setFields(nodes);
        
        // TRANSITIONS and CONNECTIONS labels
	        // write transition data (nodes and label info)
	//        Transition[] trans = new Transition[transitions.size()];
	//        for (int i=0; i<actualTransitions.size(); i++) {
	//        	Transition currentT = nTransitions.get(i);
	//        	// create transition
	//        	Transition t = new Transition(
	//        			currentT.getSource(), 	//Integer.toString(n+1)
	//        			currentT.getTarget(), 
	//        			currentT.getConnector());	
	//        	
	//        	// add to array of transitions
	//        	trans[i] = t;        	
	//        }     
	//       
	//        Transition[] trans = new Transition[transitions.size()];
	//        for (int i=0; i<transitions.size(); i++) {
	//        	// get xy pair in List form
	//        	List<Integer> xy = transitions.get(i);
	//        	System.out.println("TRANSITION: \n" + xy.get(0).toString() + 
	//        						xy.get(1).toString() +
	//    							labels.get(i));
	//        	// create transition
	//        	Transition t = new Transition(
	//        			xy.get(0).toString(), 	//Integer.toString(n+1)
	//        			xy.get(1).toString(), 
	//        			labels.get(i));	
	//        	
	//        	// add to array of transitions
	//        	trans[i] = t;
	//        }
        Transition[] ts = new Transition[actualTransitions.size()];
        actualTransitions.toArray(ts); 
        // set transitions to diagram
        diagramBuilder.setTransitions(ts);

        diagramBuilder.setSizeFull();	// layouting
	}
	

}
