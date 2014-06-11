package ads2.ss14.etsppc;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Klasse zum Berechnen der Tour mittels Branch-and-Bound.
 * Hier sollen Sie Ihre L&ouml;sung implementieren.
 */
public class ETSPPC extends AbstractETSPPC {

    private double[][] distances;

    private Map<Integer, Location> locations;
    private Map[] constraints;

    private TreeSet<Problem> problems;

    private double upperBound;

    public ETSPPC(ETSPPCInstance instance) {

        locations = instance.getAllLocations();
        distances = createMatrix(locations);
        constraints = getConstraints(instance.getConstraints());

        Problem p = new Problem(new TreeSet<Integer>(constraints[0].keySet()),
                new TreeSet<Integer>(constraints[0].keySet()),
                new LinkedList<Location>());

        // TODO: Look for better start node
        p.pathTaken.add(locations.get(1));
        p.lowerBound = getMinimum(p);

        problems = new TreeSet<Problem>();
        problems.add(p);
    }

    private double[][] createMatrix(Map<Integer, Location> locations) {
        int count = locations.size();

        double[][] matrix = new double[count][count];

        for (int x = 0; x < count; x++) {
            matrix[x][x] = Double.POSITIVE_INFINITY;
            for (int y = x+1; y < count; y++) {
                double entfernung = locations.get(x+1).distanceTo(locations.get(y+1));
                matrix[x][y] = entfernung;
                matrix[y][x] = entfernung;
            }
        }

        return matrix;
    }

    private Map[] getConstraints(List<PrecedenceConstraint> constraints) {

        Map[] results = new Map[2];

        int roundTripSize = locations.size();

        results[0] = new HashMap<Integer,Location>(locations);         //available locations
        results[1] = new HashMap<Integer,Integer>(roundTripSize+1,1); //constraints

        for (PrecedenceConstraint c : constraints) {
            if (results[0].containsKey(c.getSecond()))
                results[0].remove(c.getSecond());

            results[1].put(c.getFirst(), c.getSecond());
        }

        return results;
    }


    /**
     * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
     * Verf&uuml;gung gestellt um eine g&uuml;ltige Tour
     * zu finden.
     *
     * <p>
     * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
     * ein.
     * </p>
     */
    @Override
    public void run() {
        branchandbound();
    }

    private void branchandbound() {
        Problem p;

        while ((p = problems.pollFirst()) != null) {

            // we have a tour
            if (p.pathTaken.size() == locations.size() - 1) {
                // add last location to path, calculate length ("lowerbound"),
                // compare with upperbound and if lower -> set new upperbound
                // and remove invalid problems
                Location secondLastDest = p.getLastVisitedLocation();
                Location lastDest = locations.get(p.columnIndices.pollFirst());
                Location firstDest = p.pathTaken.get(0);

                p.lowerBound += getDistance(secondLastDest,lastDest);
                p.lowerBound += getDistance(lastDest, firstDest);

                if (p.lowerBound < upperBound) {
                    upperBound = p.lowerBound;
                    p.pathTaken.add(lastDest);
                    setSolution(upperBound,p.pathTaken);
                    removeBadSolutions();
                }
            } else {

                for (Integer destination : p.columnIndices) {
                    Problem newProblem = p.subProblem(locations.get(destination));

                    if (constraints[1].containsKey(destination)) {
                        int newAvailableDest = (int)constraints[1].get(destination);
                        newProblem.rowIndices.add(newAvailableDest);
                        newProblem.columnIndices.add(newAvailableDest);
                    }


                    newProblem.lowerBound = getMinimum(newProblem);

                    if (newProblem.lowerBound < upperBound) problems.add(newProblem);
                }
            }
        }
    }

    private double getDistance(Location from, Location to) {
        return getDistance(from.getCityId(), to.getCityId());
    }

    private double getDistance(int from, int to) {
        return distances[from-1][to-1];
    }

    private void removeBadSolutions() {
        Problem p;

        do {
            p = problems.pollLast();
        } while (p != null && p.lowerBound >= upperBound);

        if (p != null) problems.add(p);
    }

    private double getMinimum(Problem p) {
        double lowerBound = 0;

        for (Integer x : p.rowIndices) {
            double minimum = Double.POSITIVE_INFINITY;

            for (Integer y : p.columnIndices) {
                // TODO:  if element is in p.pathtaken skip ... (emulate setting infinity on already taken routes)
                if (distances[x][y] < minimum)
                    minimum = distances[x][y];
            }

            lowerBound += minimum;
        }

        return lowerBound;
    }
}

class Problem implements Comparable<Problem>{

    public double lowerBound;

    public TreeSet<Integer> rowIndices;
    public TreeSet<Integer> columnIndices;
    public List<Location> pathTaken;

    Problem(TreeSet<Integer> rowIndices, TreeSet<Integer> columnIndices, List<Location> pathTaken) {
        this.rowIndices = rowIndices;
        this.columnIndices = columnIndices;
        this.pathTaken = pathTaken;
    }

    public Problem subProblem(Location nextLocation) {
        Problem p = new Problem(new TreeSet<Integer>(this.rowIndices),
                new TreeSet<Integer>(this.columnIndices),
                new LinkedList<Location>(this.pathTaken));

        p.rowIndices.remove(getLastVisitedLocation().getCityId());
        p.pathTaken.add(nextLocation);
        p.columnIndices.remove(nextLocation.getCityId());

        return p;
    }

    public Location getLastVisitedLocation() {
        return this.pathTaken.get(this.pathTaken.size() - 1);
    }

    @Override
    public int compareTo(Problem o) {
        return roundAwayFromZero(this.lowerBound - o.lowerBound);
    }

    private int roundAwayFromZero(double x) {
        return (int) (x > 0 ? Math.ceil(x) : Math.floor(x));
    }
}