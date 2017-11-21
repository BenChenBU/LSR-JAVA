import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 
  
  public static final int INFINITY = 9999;
  
  int[] lkcost;    /*The link cost between this node and other nodes*/
  int nodename;            /*Name of this node*/
  int[][] costs;    /*forwarding table, where index is destination node, [i][0] is cost to destination node and
   [i][1] is the next hop towards the destination node */
  
  int[][] graph;    /*Adjacency metric for the network, where (i,j) is cost to go from node i to j */
  Boolean flag = false;
  
  ArrayList<Integer> seqNums0 = new ArrayList<Integer>();
  ArrayList<Integer> seqNums1 = new ArrayList<Integer>();
  ArrayList<Integer> seqNums2 = new ArrayList<Integer>();
  ArrayList<Integer> seqNums3 = new ArrayList<Integer>();
  int seqNum;
  
  /* Class constructor */
  public Node() { }
  
  /* students to write the following two routines, and maybe some others */
  void rtinit(int nodename, int[] initial_lkcost) { 
    
    // initialize necessary data structures
    
    lkcost = new int[4]; // initalizes the lkcost variable
    lkcost = initial_lkcost.clone(); // clones initial_lkcost into the new lkcost array
    
    this.nodename = nodename; // sets the node name to the inputted node name
    this.costs = new int[4][2]; // initializes the cost array in the node
    this.graph = new int[4][4]; // initializes the adjacency matrix graph
    
    // initialize the forwarding the graph
    
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        if (i == nodename || j == nodename) {
          graph[i][j] = lkcost[j]; // initializes to the lkcost of the node to node j
        }
        else {
          graph[i][j] = INFINITY;
        }
      }
    }
    
    // initialize the forwarding costs table
    
    for (int i = 0; i < 4; i++) {
      costs[i][0] = lkcost[i];
      System.out.println("Updated costs with lkcost. Just updated: " + costs[i][0]);
    }
    
    for (int i = 0; i <4; i++) {
      if (costs[i][0] != INFINITY) { // the node is reachable
        costs[i][1] = i;
      }
      else {
        costs[i][1] = -1; 
      }
    }
    
    System.out.println("Graph and Costs for Node " + nodename + " have been initialized.");
    printdt();
    
    
    // send link costs to all neighboring nodes
    
    for (int i = 0; i < 4; i++) {
      
      if (lkcost[i] != INFINITY && i != nodename) { // if the node is reachable (a neighbor) and it's not this node
        
        Packet packetSend = new Packet(this.nodename, i, this.nodename, lkcost, seqNum); // creates a packet to send to other nodes
        NetworkSimulator.tolayer2(packetSend); // sends packet over layer 2
        seqNum++;
        NetworkSimulator.packetsSent++;
        
      }
      
    }
    
  }    
  
  void rtupdate(Packet rcvdpkt) {
    
    int nameOrig = rcvdpkt.nodename;
    int seqNumRcv = rcvdpkt.seqNo; 
    
    if (nameOrig == 0) {
      if (!seqNums0.contains(seqNumRcv)) {
        seqNums0.add(seqNumRcv); 
        flag = true;
      }
    }
    if (nameOrig == 1) {
      if (!seqNums1.contains(seqNumRcv)) {
        seqNums1.add(seqNumRcv); 
        flag = true;
      }
    }
    if (nameOrig == 2) {
      if (!seqNums2.contains(seqNumRcv)) {
        seqNums2.add(seqNumRcv); 
        flag = true;
      }
    }
    if (nameOrig == 3) {
      if (!seqNums3.contains(seqNumRcv)) {
        seqNums3.add(seqNumRcv); 
        flag = true;
      }
    }
    
    if (flag == true) {
      
      int[][] outputTable;
      int[] minArray = rcvdpkt.mincost; // stores the mincost array for the incoming packet
      int source = rcvdpkt.sourceid; // stores the source ID of the node
      int seqNum = rcvdpkt.seqNo; // seqnum of incoming packet
      int name = rcvdpkt.nodename; // name of the node who sent this packet originally
      
      // updates our graph with adjacency information from the incoming packet
      
      for (int i = 0; i < 4; i++) {
        
        if (minArray[i] != INFINITY) { // if the node can reach node i
          graph[name][i] = minArray[i]; // update our graph with the node's lkcost information
          graph[i][name] = minArray[i];
        }
      }
      
      // runs dijkstra's algorithm on the graph
      
      outputTable = dijkstra(graph, nodename);
      System.out.println("This is dijkstra's output");
      System.out.println(Arrays.deepToString(outputTable));
      
      // update the cost table
      
      for (int i = 0; i < 4; i++) {
        
        
        if (outputTable[i][0] < costs[i][0]) {
        costs[i][0] = outputTable[i][0];
        
        
          int temp = outputTable[i][0] - graph[i][outputTable[i][1]];
          if (graph[outputTable[i][1]][this.nodename] == temp) {
            costs[i][1] = outputTable[i][1];
          }
          else {
            for (int k = 0; k < 4; k++) {
              int currentVertex = graph[outputTable[i][1]][k];
              if (currentVertex != INFINITY && currentVertex != 0 && k != i) {
                temp = temp - currentVertex;
                if (graph[k][this.nodename] == temp) {
                  costs[i][1] = k;
                  break;
                }
              }
            }
          }
          
          
        }
      }
      
      System.out.println("Node " + nodename + "'s cost table has been properly updated.");
      printdt();
      
      // forwards packet to all other nodes that have not received this packet yet
      
      
      
      for (int i = 0; i < 4; i++) {
        // if the node is reachable (a neighbor) but not reachable from the received packet node and it's not this node, send packet
        
        if (lkcost[i] != INFINITY && minArray[i] == INFINITY && i != nodename) {
          Packet packetSend = new Packet(this.nodename, i, name, minArray, seqNumRcv);
          NetworkSimulator.tolayer2(packetSend); // sends packet over layer 2
          System.out.println("Node " + this.nodename + " is delivering a packet originally from " + name + " to " + i + ".");
          NetworkSimulator.packetsSent++;
        } 
      }
      
      flag = false;
      
    }
  }
  
  // implementation of dijkstra's algorithm
  
  
  int[][] dijkstra(int graph[][], int src) {
    
    int[][] output = new int[4][2]; // creates the output table
    boolean[] processedVert = new boolean[4]; // creates boolean array to track whether or not the vertex has been explored
    
    for (int i = 0; i < 4; i++) { // sets all output values to infinity except for the source node
      for (int j = 0; j < 2; j++) {
        if (i == src) {
          output[i][j] = 0; // sets source node to 0
        }
        else {
          output[i][j] = INFINITY; // sets all other nodes to infinity
        }
      }
    }
    
    for (int i = 0; i < 4; i++) { // loops through the total amount of vertexes we have
      
      int firstNode = findMinNode(output, processedVert); // marks the current vertex node we are going to traverse through its neighbors. uses findMinNode to determine lowest cost neighbor
      processedVert[firstNode] = true; // set this node to be explored
      
      
      for (int j = 0; j < 4; j++) { // loops through the total amount of vertexes we have
        
        /* Performs five checks 
         * 1) Vertex to neighbor is not 0 (this means it is ourself)
         * 2) Vertex to neighbor is not infinity (this means it's not a neighbor)
         * 3) Output table is not infinity (this means it can't be  / is not a neighbor)
         * 4) The node has not already been explored
         * 5) The new path is indeed shorter than the current path
         * */
        
        
        if (graph[firstNode][j] != 0 && graph[firstNode][j] != INFINITY && output[firstNode][0] != INFINITY && !processedVert[j] && 
            (output[firstNode][0] + graph[firstNode][j] < output[j][0])) {
          
          output[j][0] = output[firstNode][0] + graph[firstNode][j]; // sets the new cost for node j from source
          output[j][1] = firstNode; // sets output[j][1] to the value of the last hop before the destination
          
        }
        
      }
      
      
    }
    
    return output;
    
  }
  
  int findMinNode(int[][] outputDij, boolean[] processedVert) {
    
    int minCost = INFINITY; // initializes the value and vertex to INFINITY and -1, respectively
    int minIndex = -1;
    
    for (int i = 0; i < 4; i++) { // loops through all possible vertexes
      
      if (processedVert[i] == false && outputDij[i][0] <= minCost) { // finds a vertext that is unexplored and has a cost less than the current cost
        minIndex = i;
        minCost = outputDij[i][0];
      }
      
    }
    
    return minIndex; // sends the next lowest cost index to dijkstra's to explore
  }
  
  /* called when cost from the node to linkid changes from current value to newcost*/
  void linkhandler(int linkid, int newcost) { 
    
    lkcost[linkid] = newcost; // update array
    
    System.out.println("Links between 0 and 1 have changed. The new cost is: " + newcost);
    
    // send link costs to all neighboring nodes
    
    for (int i = 0; i < 4; i++) {
      
      if (lkcost[i] != INFINITY && i != nodename) { // if the node is reachable (a neighbor) and it's not this node
        
        Packet packetSend = new Packet(this.nodename, i, this.nodename, lkcost, seqNum); // creates a packet to send to other nodes
        NetworkSimulator.tolayer2(packetSend); // sends packet over layer 2
        seqNum++;
        NetworkSimulator.packetsSent++;
      }
    }
    
    
  }  
  
  /* Prints the current costs to reaching other nodes in the network */
  void printdt() {
    
    System.out.printf("                    \n");
    System.out.printf("   D%d |   cost  next-hop \n", nodename);
    System.out.printf("  ----|-----------------------\n");
    System.out.printf("     0|  %3d   %3d\n",costs[0][0],costs[0][1]);
    System.out.printf("dest 1|  %3d   %3d\n",costs[1][0],costs[1][1]);
    System.out.printf("     2|  %3d   %3d\n",costs[2][0],costs[2][1]);
    System.out.printf("     3|  %3d   %3d\n",costs[3][0],costs[3][1]);
    System.out.printf("                    \n");
  }
  
}
