package com.example.diagramtest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;

import org.vaadin.diagrambuilder.Connector;
import org.vaadin.diagrambuilder.DiagramBuilder;
import org.vaadin.diagrambuilder.DiagramStateEvent;
import org.vaadin.diagrambuilder.Node;
import org.vaadin.diagrambuilder.NodeType;
import org.vaadin.diagrambuilder.Transition;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@Title("DiagramBuilder Add-on Demo")
@SuppressWarnings("serial")
@JavaScript({//"http://cdn.alloyui.com/3.0.1/aui/aui-min.js",
			"js/alloy-ui-master/.*",
			"js/alloy-ui-master/cdn.alloyui.com_3.0.1_aui_aui-min.js",
//			"js/cdn.alloyui.com_2.5.0_aui_aui-min.js"
//			"js/aui-diagram-builder/js/aui-diagram-builder.js",
//			"js/aui-diagram-builder/js/aui-diagram-builder-connector.js",
//			"js/aui-diagram-builder/js/aui-diagram-node.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-condition.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-end.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-fork.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-join.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-manager-base.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-start.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-state.js",
//			"js/aui-diagram-builder/js/aui-diagram-node-task.js",
			})
//@StyleSheet("js/cdn.alloyui.com_3.0.1_aui-css_css_bootstrap.min.css")
@StyleSheet("http://cdn.alloyui.com/3.0.1/aui-css/css/bootstrap.min.css")
@Theme("diagramtest")
@PreserveOnRefresh
public class DiagramtestUI extends UI {

    final VerticalLayout layout = new VerticalLayout();
    final TextField outFileField = new TextField();
    final TextField inFileField = new TextField();
    final TextField diagramTitleField = new TextField();
    DiagramBuilder diagramBuilder; 
    Button resetDiagramButton = new Button("Reset Diagram Builder");
    Button saveDiagramButton = new Button("Save to .dot file");
    Button loadDiagramButton = new Button("Load .dot file");
    Button renameNodesButton = new Button("Rename Nodes");
    
    List<String> nName = new ArrayList<String>();
    List<String> nType = new ArrayList<String>();
    List<int[]> nXy = new ArrayList<int[]>();
    List<Transition> nTransitions = new ArrayList<Transition>();
    List<Connector> nConnectorName = new ArrayList<Connector>();
    DBuilderUtils diagramUtils = new DBuilderUtils();
    
    @WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DiagramtestUI.class, widgetset = "com.example.diagramtest.widgetset.DiagramtestWidgetset")
	public static class Servlet extends VaadinServlet {

	}

	@Override
    protected void init(VaadinRequest request) {
    	// set main content
        //final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);
                		
        HorizontalLayout saveLayout = new HorizontalLayout();
        saveLayout.setSpacing(true);
        outFileField.setWidth("15em");
        outFileField.setInputPrompt("C:/dev/output/dot-output.dot");
        outFileField.setValue("C:/dev/output/dot-output.dot");
        saveLayout.addComponent(outFileField);
        saveLayout.addComponent(saveDiagramButton);
		
        HorizontalLayout loadLayout = new HorizontalLayout();
        loadLayout.setSpacing(true);
        inFileField.setWidth("15em");
        inFileField.setInputPrompt("C:/dev/output/dot-output.dot");
        inFileField.setValue("C:/dev/output/dot-output.dot");
        loadLayout.addComponent(inFileField);
        loadLayout.addComponent(loadDiagramButton);
        
        VerticalLayout v = new VerticalLayout();
        v.setSpacing(true);
        v.addComponent(saveLayout);
        v.addComponent(loadLayout);
//        v.setComponentAlignment(saveLayout, Alignment.BOTTOM_RIGHT);

        HorizontalLayout dFunctionsLayout = new HorizontalLayout();
        dFunctionsLayout.setWidth("100%");
        dFunctionsLayout.setSpacing(true);
        diagramTitleField.setWidth("15em");
        diagramTitleField.setCaption("Graph title");
        diagramTitleField.setInputPrompt("default_use_case");
        dFunctionsLayout.addComponent(diagramTitleField);
        dFunctionsLayout.addComponent(renameNodesButton);
        dFunctionsLayout.addComponent(v);
        dFunctionsLayout.setComponentAlignment(diagramTitleField, Alignment.BOTTOM_LEFT);
        dFunctionsLayout.setComponentAlignment(renameNodesButton, Alignment.BOTTOM_LEFT);
        dFunctionsLayout.setComponentAlignment(v, Alignment.BOTTOM_RIGHT);
		
        diagramBuilder = new DiagramBuilder();
        diagramBuilder.setImmediate(true);
        diagramUtils.initDiagram(diagramBuilder);

        layout.addComponent(new Label("<h1>DiagramBuilder example</h1>", ContentMode.HTML));
        layout.addComponents(resetDiagramButton,
//        							saveLayout,
//        							loadLayout, 
        							dFunctionsLayout);
        							//new HorizontalLayout(resetDiagramButton, saveLayout, loadLayout));        
        layout.addComponent(diagramBuilder);
        //initDiagram();

        
        resetDiagramButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (diagramBuilder != null) {
					layout.removeComponent(diagramBuilder); 
				}
				
		        diagramBuilder = new DiagramBuilder();
		        diagramUtils.initDiagram(diagramBuilder);
    	        diagramTitleField.setValue(diagramUtils.getGraphTitle());
		        layout.addComponent(diagramBuilder);
			}
		});
        
        saveDiagramButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // Using asynchronous API to lazily fetch the current state of the diagram.
                diagramBuilder.getDiagramState(new DiagramBuilder.StateCallback() {
                    @Override
                    public void onStateReceived(DiagramStateEvent event) {
                        // DO SOMETHING with received state information. e.g. parse data for .dot files
                    	//diagramUtils.reportStateBack(event);
                    	if (diagramBuilder != null) {
							layout.removeComponent(diagramBuilder); 
						}
                    	//save data to file
                    	List<Node> nodes = event.getNodes();
						diagramUtils.saveToFile(nodes, outFileField.getValue(), diagramTitleField.getValue());
                    	
						// redraw diagram if nodes need to be renamed
//                    	nodes = diagramUtils.renameNodes(nodes);
						diagramUtils.generateDiagramAfterRename(diagramBuilder = new DiagramBuilder(), nodes);
						layout.addComponent(diagramBuilder);
                    	
                    }
                });
            }
        });
        
        loadDiagramButton.addClickListener(new Button.ClickListener() {
        	@Override
			public void buttonClick(ClickEvent event) {
        		// call method for parsing .dot data
        		try {
        			if (diagramBuilder != null) {
    					layout.removeComponent(diagramBuilder); 
    				}
//        	        diagramBuilder = new DiagramBuilder();
        	        diagramUtils.readFromDotFile(diagramBuilder = new DiagramBuilder(), inFileField.getValue());
        	        diagramTitleField.setValue(diagramUtils.getGraphTitle());
    		        layout.addComponent(diagramBuilder);    		        
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getClass());
				}
			}
		});
        
        
        renameNodesButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                // Using asynchronous API to lazily fetch the current state of the diagram.
                diagramBuilder.getDiagramState(new DiagramBuilder.StateCallback() {
                    @Override
                    public void onStateReceived(DiagramStateEvent event) {
                    	if (diagramBuilder != null) {
							layout.removeComponent(diagramBuilder); 
						}
                    	List<Node> nodes = event.getNodes();
                    	nodes = diagramUtils.renameNodes(nodes);	//event, diagramBuilder = new DiagramBuilder()
						diagramUtils.generateDiagramAfterRename(diagramBuilder = new DiagramBuilder(), nodes);
						layout.addComponent(diagramBuilder);
                    }
                });
            }
        });
        
//        layout.addComponent(new Button("Get State", new Button.ClickListener() {
//			@Override
//			public void buttonClick(ClickEvent event) {
//				// TODO Auto-generated method stub
//				ObjectMapper mapper = new ObjectMapper();
//		        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//		        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//		     
//				try {
//					String writeStateAsString = mapper.writeValueAsString(diagramBuilder.getState());
//					Notification.show(writeStateAsString);
//				} catch (JsonProcessingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}));
        
    }
	


//	Button stateButton = new Button("Get state to server and report as JSON", new Button.ClickListener() {	
//        @Override
//        public void buttonClick(Button.ClickEvent event) {
//            // Using asynchronous API to lazily fetch the current state of the diagram.
//            diagramBuilder.getDiagramState(new DiagramBuilder.StateCallback() {
//                @Override
//                public void onStateReceived(DiagramStateEvent event) {
//                    // DO SOMETHING with received state information. e.g. parse data for .dot files
//                	reportStateBack(event);
//                }
//            });
//        }
//    });

	
	
	
	/**

    private void initDiagram() {

        // Initialize diagram builder component
        diagramBuilder = new DiagramBuilder();
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

    }

	public void reportStateBack(DiagramStateEvent event) {
        List<Node> nodes = event.getNodes();
        
        // Normally you'd do something with the nodes, in this 
        // demo, just report it back to browser
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String writeValueAsString = mapper.writeValueAsString(nodes);	//event.getNodes()
            
            nName.clear();
            nType.clear();
            nXy.clear();
            nTransitions.clear();
            nConnectorName.clear();
            for (int n=0; n < nodes.size(); n++) {
            	// this String list can be used to pass node name info
            	nName.add(nodes.get(n).getName());
            	nType.add(nodes.get(n).getType());
            	nXy.add(nodes.get(n).getXy());
            	System.out.println("THE XY VALUE WAS" + mapper.writeValueAsString(nodes.get(n).getXy()));
            	List<Transition> t = nodes.get(n).getTransitions();
                	System.out.println("t is: " + mapper.writeValueAsString(t));
        		nTransitions.addAll(t);
                for (int c=0; c < t.size(); c++) {
                	nConnectorName.add(t.get(c).getConnector());
                	System.out.println("connector is: " + mapper.writeValueAsString(nConnectorName));
                }
            }
            // call method to parse data to dot file
            parseToDotFile();

            // for testing. print to console
            String writeNodeNames = mapper.writeValueAsString(nName);
            System.out.println(nName);
            System.out.println(mapper.writeValueAsString(nType));
            System.out.println("this is the List<int[]> xy" + mapper.writeValueAsString(nXy));
            System.out.println("transitions: " + mapper.writeValueAsString(nTransitions));
            System.out.println("transitions names: " + mapper.writeValueAsString(nConnectorName));
            
            // write out to browser screen
            System.out.println("Node Names:\n" + writeNodeNames);
            System.out.println("States:\n" + writeValueAsString);
            Notification.show(
                    "State reported: ",
                    "Node Names:\n" + writeNodeNames +
                    "\n\nStates:\n" + writeValueAsString,
                    Notification.Type.ERROR_MESSAGE);
            
            
            
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

	public void generateDiagramFromFile(List<Integer> states, List<List<Integer>> transitions, List<String> labels) {
//		List<Integer> states = new ArrayList<Integer>();
//	    List<List<Integer>> transitions = new ArrayList<>();
//	    List<String> labels = new ArrayList<String>();
	    
   
        // Initialize diagram builder component
        diagramBuilder = new DiagramBuilder();
        
        // Initialize field types
        diagramBuilder.setAvailableFields(
		        new NodeType("diagram-node-start-icon", "Start", "start"),
		        new NodeType("diagram-node-state-icon", "State", "state"),
		        new NodeType("diagram-node-end-icon", "End", "end")
        );
        
        // Initialize states (nodes)
        int x = 10;
        int y = 10;
//        int states = 4;
        Node[] nodes = new Node[states.size()];
        for (int n=0; n<states.size(); n++) {	//(int n=0; n<nodes.length; n++) {
        	Node node;
        	if (n == 0) {
            	node = new Node(states.get(n).toString(), "start",(x+=10),(y+=10));	//Integer.toString(n+1)
        	} else if (n == nodes.length-1) {
            	node = new Node(states.get(n).toString(), "end",(x+=120),(y+=100));
        	} else if (n % 2 == 0) { 
        		// odd
        		node = new Node(states.get(n).toString(), "state",(x*=3),(y/=2));        		
        	} else { 
        		// even
        		node = new Node(states.get(n).toString(), "state",(x+=70),(y+=110));        		
        	}
        	nodes[n] = node;
        }
        diagramBuilder.setFields(
        		nodes
        		);
        
        // Initialize transitions and connection labels
        Transition[] trans = new Transition[transitions.size()];
        for (int i=0; i<transitions.size(); i++) {	//(int n=0; n<nodes.length; n++) {
        	// get xy pair in List form
        	List<Integer> xy = transitions.get(i);
        	
        	// create transition
        	Transition t = new Transition(
        			xy.get(0).toString(), 	//Integer.toString(n+1)
        			xy.get(1).toString(), 
        			labels.get(i));	
        	
        	// add to array of transitions
        	trans[i] = t;
        }
        diagramBuilder.setTransitions(
        		trans
        		);

        diagramBuilder.setSizeFull();
	}
	
	

	private void parseFromDotFile() throws FileNotFoundException {
		String dotString = "";
		BufferedReader br = new BufferedReader(new FileReader("C:/dev/output/dot-output.dot"));
		try {//(BufferedReader br = new BufferedReader(new FileReader("C:/dev/output/dot-output.dot"))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				dotString = dotString.concat(line);
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		System.out.println("br: " + br.toString());
		System.out.println("\nThe result from parsing the inputted dot file:\n" + dotString);
//		Notification.show(dotString);
		
//		InputStream inStream = ByteArrayInputStream();
//		DOTParser dParser = new DOTParser(br);
//		System.out.println("dParser: " + dParser.toString());
		
		Scanner sc = new Scanner(new FileReader("C:/dev/output/dot-output.dot"));//.useDelimiter("\\D+");
//		Scanner s = new Scanner("Str87uyuy232").useDelimiter("\\D+");

		System.out.println("\nSearching in the while loop");
		List<Integer> states = new ArrayList<Integer>();
	    List<List<Integer>> transitions = new ArrayList<>();
	    List<String> labels = new ArrayList<String>();
	    
	    // loop to parse input for states and transitions
		while (sc.hasNext()) {
		    String nextToken = sc.next();
		    
		    // get states
		    if (nextToken.equalsIgnoreCase("//states") || nextToken.equalsIgnoreCase("states")){
		    	System.out.println("STATES:");
		    	int count = 0;
		    	while (sc.hasNextInt()){
		    		// add states to states list
		    		states.add(sc.nextInt());
		    		System.out.println(states.get(count));	//(s.nextInt());
		    		count ++;
		    	}
		    	System.out.println("states were: " + states);
			} // end of STATES
		    
		    // get transitions
		    if (nextToken.equalsIgnoreCase("//transitions") || nextToken.equalsIgnoreCase("transitions")){
				System.out.println("TRANSITIONS:");
		    	while (sc.hasNextInt()) {
		    		
		    		// get x values
		    		int x = sc.nextInt();
		    		List<Integer> t = new ArrayList<>();	// temp storage for current transition pair
		    		t.add(x);
		    		System.out.println("x is: "+ x);	//s.nextInt());
		    		
		    		// get y values after "->" sign
		    		if (sc.next().equals("->")) {
		    			int y = sc.nextInt();
		    			t.add(y);
		    			System.out.println("y is: " + y);
		    			
		    			// add x,y value to transitions list
			    		transitions.add(t);
		    			System.out.println("this transition is: " + t);
		    			System.out.println("transition(s) are: " + transitions);
		    			
		    			// if the line has more (i.e. label info) get the label data
		    			if (!sc.hasNextInt()) {		//(s.hasNext()) { 
		    				System.out.println("\nthere is more..."); 
		    				
		    				while (!(sc.hasNextInt())){		//sc.hasNext()
		    					
		    					// get label data after '=' character
		    					char c = sc.findInLine(".").charAt(0);
		    					if ( c == ';') break;
		    					if ( c == '=' ) {
		    						String label = "";
//		    						System.out.println("the last character was: '" + c + "'. Now fetching label data..."); //to print out every char in the scanner
		    						boolean firstRound = true;
		    						while (!( c == ']' )) {
				    					c = sc.findInLine(".").charAt(0);
	
				    					if ( c == ']') break;	// break if label data is complete and proceed to next line
				    					
				    					if (firstRound == true && (c == ' ')) {
				    						// skip adding leading space to label
				    					} else{
				    						label += c;
				    					}
				    					firstRound = false;
				    									    								    					
		    						}
		    						// add label data to connection label list
		    						labels.add(label);
		    						
		    						System.out.println("The Transition label is: " + label);
		    						System.out.println("The Transition label(s) are: " + labels + "\n");
		    						
		    					}
		    				}
		    			}
		    		}
		    	}
			} // end of TRANSTIONS
		
		} // end of Scanner loop
		
		sc.close(); 
    	System.out.println("STATES are: " + states);
    	System.out.println("TRANSITIONS are: " + transitions);
    	System.out.println("LABELS are: " + labels);

	     
    	// generate diagram from parsed dot file
    	generateDiagramFromFile(states, transitions, labels);
    	
//		try {
////			dParser.open();
////			System.out.println("open: " + dParser);
//
//			if (dParser.next()) {
//				Token t = dParser.getNextToken();
//				System.out.println(t.getValue());
//			}
//			dParser.all();
////			System.out.println("all: " + dParser);
//
//			dParser.close();
//			System.out.println("close: " + dParser);
//
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
		
	private void parseToDotFile() throws JsonProcessingException, FileNotFoundException {
//		ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        String writeValueAsString = mapper.writeValueAsString(nodes);	//event.getNodes()
		
		
        // Parsing to dot file
        File file = new File("C:/dev/output/dot-output.dot");
        PrintWriter writer = new PrintWriter(file);	//"test-dot-file.dot"   System.out
            writer.println("digraph aggressive_user_behavior{");
            writer.println("\t// States");
            for (int n=0; n < nName.size(); n++) {
                writer.println("\t" + nName.get(n));
            }            
            
            writer.println("\n\t//Transitions\n");
            for (int n=0; n < nTransitions.size(); n++) {
            	Transition t = nTransitions.get(n);
                writer.println("\t" + t.getSource() + " -> " + t.getTarget() + 
                				" [label = " + t.getConnector().getName().toString() + "];");	//mapper.writeValueAsString()
            }     
//            writer.println("b -> d;");
            writer.println("}");
     
        writer.close();
        
        //end of dot parser
        Notification.show("dot file was created");
	}
	 */
}
