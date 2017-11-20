import java.io.*;

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
   }
 }
 
 // initialize the forwarding costs table
 
 costs[0] = lkcost.clone();
 
 for (int i = 0; i <4; i++) {
   if (costs[i][0] != INFINITY) { // the node is reachable
     costs[i][1] = i;
   }
   else {
    costs[i][1] = -1; 
   }
 }
 
 
 // send link costs to all neighboring nodes
 
 for (int i = 0; i < 4; i++) {
  
   if (lkcost[i] != INFINITY && i != nodename) { // if the node is reachable (a neighbor) and it's not this node
     
     Packet packetSend = new Packet(this.nodename, i, this.nodename, lkcost, 0); // creates a packet to send to other nodes
     NetworkSimulator.tolayer2(packetSend); // sends packet over layer 2
     
   }
   
 }
 
 }    

 void rtupdate(Packet rcvdpkt) { }

 /* called when cost from the node to linkid changes from current value to newcost*/
 void linkhandler(int linkid, int newcost) { }  

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
