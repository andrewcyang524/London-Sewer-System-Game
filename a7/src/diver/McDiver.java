package diver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import graph.FindState;
import graph.FleeState;
import graph.Node;
import graph.NodeStatus;
import graph.SewerDiver;

public class McDiver extends SewerDiver {

    /** Find the ring in as few steps as possible. Once you get there, <br>
     * you must return from this function in order to pick<br>
     * it up. If you continue to move after finding the ring rather <br>
     * than returning, it will not count.<br>
     * If you return from this function while not standing on top of the ring, <br>
     * it will count as a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the ring in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the ring at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLocation(), neighbors(), and distanceToRing() in state.<br>
     * You know you are standing on the ring when distanceToRing() is 0.
     *
     * Use function moveTo(long id) in state to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the ring, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void find(FindState state) {
        // TODO : Find the ring and return.
        // DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
        // Instead, write your method (it may be recursive) elsewhere, with a
        // good specification, and call it from this one.
        //
        // Working this way provides you with flexibility. For example, write
        // one basic method, which always works. Then, make a method that is a
        // copy of the first one and try to optimize in that second one.
        // If you don't succeed, you can always use the first one.
        //
        // Use this same process on the second method, flee.
        ArrayList<Long> visit= new ArrayList<>();
        OpdfsWalk(state, visit);
    }
    
    //** Move McDiver to reach the location of the ring in the map */
    private static void dfsWalk(FindState s, ArrayList<Long> visit) {
        if (s.distanceToRing() == 0) { return; }
        long u= s.currentLocation();
        visit.add(u);

        for (NodeStatus neighbor : s.neighbors()) {
            if (!visit.contains(neighbor.getId())) {
                s.moveTo(neighbor.getId());
                dfsWalk(s, visit);
                if (s.distanceToRing() == 0) { return; }
                s.moveTo(u);
            }

        }

    }
  //** Move McDiver to reach the location of the ring in the map with optimized distance */
    public static void OpdfsWalk(FindState s, ArrayList<Long> visit) {
        if (s.distanceToRing() == 0) { return; }
        long u= s.currentLocation();
        visit.add(u);
        ArrayList<NodeStatus> ring= new ArrayList<>();
        for (NodeStatus neighbor : s.neighbors()) {
            ring.add(neighbor);
        }
        Collections.sort(ring);

        for (NodeStatus neighbor : ring) {
            if (!visit.contains(neighbor.getId())) {
                s.moveTo(neighbor.getId());
                OpdfsWalk(s, visit);
                if (s.distanceToRing() == 0) { return; }
                s.moveTo(u);
            }

        }

    }

    /** Flee --get out of the sewer system before the steps are all used, trying to <br>
     * collect as many coins as possible along the way. McDiver must ALWAYS <br>
     * get out before the steps are all used, and this should be prioritized above<br>
     * collecting coins.
     *
     * You now have access to the entire underlying graph, which can be accessed<br>
     * through FleeState. currentNode() and exit() will return Node objects<br>
     * of interest, and getNodes() will return a collection of all nodes on the graph.
     *
     * You have to get out of the sewer system in the number of steps given by<br>
     * stepToGo(); for each move along an edge, this number is <br>
     * decremented by the weight of the edge taken.
     *
     * Use moveTo(n) to move to a node n that is adjacent to the current node.<br>
     * When n is moved-to, coins on node n are automatically picked up.
     *
     * You must return from this function while standing at the exit. Failing <br>
     * to do so before steps run out or returning from the wrong node will be<br>
     * considered a failed run.
     *
     * Initially, there are enough steps to get from the starting point to the<br>
     * exit using the shortest path, although this will not collect many coins.<br>
     * For this reason, a good starting solution is to use the shortest path to<br>
     * the exit. */
    @Override
    public void flee(FleeState state) {
        // TODO: Get out of the sewer system before the steps are used up.
        // DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
        // with a good specification, and call it from this one.
        // shortest(state.currentNode(), state.exit());
        Opcoins(state);
    }
    /** Find the shortest path for McDiver to exit from the ring location */
    private static void scram(FleeState s) {
        List<Node> shortest= A6.shortest(s.currentNode(), s.exit());
        for (int i= 1; i < shortest.size(); i++ ) {
            s.moveTo(shortest.get(i));
        }
    }
    /** Move McDiver from the ring location to exit while picking up as many coins */
    private static void coins(FleeState s) {
        Collection<Node> nodes= s.allNodes();
        Heap<Node> coin= new Heap<>(true);
        for (Node coins : nodes) {
            if (coins.hashCode() > 0) {
                coin.insert(coins, A6.pathSum(A6.shortest(s.currentNode(), s.exit())));
            }
        }
        for (int i= 0; i < coin.size(); i++ ) {
            Node min= coin.poll();
            int total= A6.pathSum(A6.shortest(s.currentNode(), min));
            if (total + A6.pathSum(A6.shortest(min, s.exit())) > s.stepsToGo()) {
                scram(s);
            } else {
                List<Node> path= A6.shortest(s.currentNode(), min);
                for (int b= 1; b < path.size(); b++ ) {
                    s.moveTo(path.get(b));
                }

                for (int a= 0; a < coin.size; a++ ) {
                    coin.changePriority(coin.b[a].value,
                        A6.pathSum(A6.shortest(s.currentNode(), s.exit())));
                }
            }
        }
    }
    /** Move McDiver from the ring location to exit while picking up as many coins */
    private static void Opcoins(FleeState s) {

        if (s.currentNode() == s.exit()) { return; }
        Collection<Node> nodes= s.allNodes();
        Heap<Node> coin= new Heap<>(false);

        for (Node coins : nodes) {
            if (coins.getTile().coins() > 0) {
                int maxv= coins.getTile().coins();
                for (Node smallcoins : coins.getNeighbors()) {
                    maxv= maxv + smallcoins.getTile().coins();
                }

                coin.insert(coins, maxv / A6.pathSum(A6.shortest(s.currentNode(), coins)));
            }

        }

        for (int i= 0; i < coin.size(); i++ ) {
            Node max= coin.poll();
            int total= A6.pathSum(A6.shortest(s.currentNode(), max));
            if (total + A6.pathSum(A6.shortest(max, s.exit())) > s.stepsToGo()) {
                scram(s);
            } else {
                List<Node> path= A6.shortest(s.currentNode(), max);
                for (int b= 1; b < path.size(); b++ ) {
                    s.moveTo(path.get(b));
                    Opcoins(s);
                    if (s.currentNode() == s.exit()) { return; }
                }
                for (int a= 0; a < coin.size; a++ ) {
                    coin.changePriority(coin.b[a].value,
                        A6.pathSum(A6.shortest(s.currentNode(), s.exit())));
                }
            }
        }

    }

}
