/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.16.0.2388 modeling language!*/

package cruise.umple.compiler;
import java.io.*;
import java.util.*;
import cruise.umple.util.*;
import cruise.umple.compiler.exceptions.*;

// line 96 "../../../../src/Generator.ump"
// line 26 "../../../../src/Generator_CodeGvStateDiagram.ump"
public class GvStateDiagramGenerator implements CodeGenerator
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //GvStateDiagramGenerator Attributes
  private UmpleModel model;
  private String output;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public GvStateDiagramGenerator()
  {
    model = null;
    output = "";
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setModel(UmpleModel aModel)
  {
    boolean wasSet = false;
    model = aModel;
    wasSet = true;
    return wasSet;
  }

  public boolean setOutput(String aOutput)
  {
    boolean wasSet = false;
    output = aOutput;
    wasSet = true;
    return wasSet;
  }

  /**
   * Contains various aspects from an Umple file (.ump), such as classes, attributes, associations and methods.  Generated output is based
   * off of what's contained in here.
   */
  public UmpleModel getModel()
  {
    return model;
  }

  public String getOutput()
  {
    return output;
  }

  public void delete()
  {}
  
  //------------------------
  // DEVELOPER CODE - PROVIDED AS-IS
  //------------------------
  
  // line 29 ../../../../src/Generator_CodeGvStateDiagram.ump
  public void generate()
  {
    StringBuilder code = new StringBuilder();
    Boolean isFirstState = true;
    String className, smName, clSmName, sLabel, sName;
    String guardString;
    Guard guard;

    // Output basic gv file header
    code.append("// Code generated by Umple\n\n");
    code.append("digraph state {\n");
    code.append("  compound = true;\n");
    code.append("  size=\"10,10\"\n");

    // Determine if there are multiple classes with state machines
    // or multiple state machines in any class
    // If so, we will need to put boxes around the state machines
    int smCount = 0;
    for (UmpleClass uClass : model.getUmpleClasses()) {
      for (StateMachine sm : uClass.getStateMachines()) {
        smCount++;
      }
    }
    if(smCount == 0) {
      // Special case. No state machine.
      code.append("  node [shape=box, penwidth=0];\n");
      code.append("  message [label =\"No state machine found in the input Umple file\"];\n");
      terminateCode(code);
      return;
    }

    // We now know we have to output one or more state machines
    // Iterate through each class. 
    for (UmpleClass uClass : model.getUmpleClasses())
    {
      className = uClass.getName();
      code.append("\n  // Class: "+className+"\n");
      
      for (StateMachine sm : uClass.getStateMachines())
      {
      
        smName = sm.getName();
        clSmName = className+"_"+smName;
        code.append("\n    // StateMachine: "+smName+"\n");        

        // If there are multiple state machines in the model
        // Then we need to put the state machine in a border
        if(smCount > 1) {
          code.append("   subgraph cluster"+clSmName+" {\n");
          code.append("    label=\"sm "+className+" "+smName+"\";\n");   
          code.append("    penwidth=0.5;\n");        
        }

        // We haven't processed any states yet
        isFirstState = true;
      
        // Every state machine has a start state
        appendStartState(code, 4, clSmName);
        appendNormalStateFormat(code, 4);
 
        for (State s : sm.getStates())
        {
          sLabel=s.getName();
          sName=clSmName+"_"+sLabel;
          code.append("\n      // State: "+sLabel+"\n");   

          code.append("      "+sName+" [label = "+sLabel+"];\n");
          if(isFirstState) {
            // Output transition to first state
            isFirstState = false;
            code.append("      start_"+clSmName+" -> "+sName+";\n");
          }
          // Output all the transitions
          for (Transition t : s.getNextTransition()) {
            guard = t.getGuard();
            if (guard == null) guardString = "";
            else guardString = " ["+guard.getCondition()+"]";
            
            code.append("      "
              + getStateQualifiedName(t.getFromState()) 
              + " -> "+getStateQualifiedName(t.getNextState())
              + " [ label = \""+t.getEvent().getName()+guardString+"\" ];\n");
          }
        } // End iteration through states of a state machine
        
        // If there are multiple state machines in the model
        // Then we need to end the cluster border
        if(smCount > 1) {
          code.append("  }\n");
        }
      } // End iteration through state machines of a class
    } // End iteration through classes
    terminateCode(code);
  }
  
  private void terminateCode(StringBuilder code) {
    code.append("}\n");

    model.setCode(code.toString());
    writeModel();
  } 

  // Append the standard format for a start state into the current graph context
  private void appendStartState(StringBuilder code, int numSpaces, String clSmName) {
    appendSpaces(code, numSpaces);
    code.append("// Start state black circle\n");
    appendSpaces(code, numSpaces);
    code.append("node [shape = point, fillcolor=\"black\", width=0.2 ];\n");
    appendSpaces(code, numSpaces);
    code.append("start_"+clSmName+";\n\n");
  }

  // Append the format for normal nodes.
  private void appendNormalStateFormat(StringBuilder code, int numSpaces) {
    appendSpaces(code, numSpaces); 
    code.append("// Format for normal states\n");
    appendSpaces(code, numSpaces);
    code.append("node [shape = rectangle, width=1,style=rounded];\n");
  }

  // Used to indent code
  private void appendSpaces(StringBuilder code, int numSpaces) {
    for(int i=0; i<numSpaces; i++) {
      code.append(" ");
    }
  }

  // The state qualified name incorporates the classname, the state machine name
  // and the state name  
  private String getStateQualifiedName(State s) {
    StateMachine sm = s.getStateMachine();
    UmpleClass c = sm.getUmpleClass();
    return ""+c.getName()+"_"+sm.getName()+"_"+s.getName();
  }



  // Output the graphviz file to a file with the .gv suffix
  private void writeModel()
  {
    try
    {
      String path = model.getUmpleFile().getPath();
      File file = new File(path);
      file.mkdirs();
      String modelFilename = path + File.separator + model.getUmpleFile().getSimpleFileName() + ".gv";
      BufferedWriter bw = new BufferedWriter(new FileWriter(modelFilename));
      bw.write(model.getCode());
      bw.flush();
      bw.close();
    }
    catch (Exception e)
    {
      throw new UmpleCompilerException("There was a problem with generating GraphViz State Machine code." + e, e);
    }
  }
}