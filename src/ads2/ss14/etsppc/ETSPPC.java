package ads2.ss14.etsppc;

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

        upperBound = instance.getThreshold();
        locations = instance.getAllLocations();
        distances = createMatrix(locations);
        constraints = getConstraints(instance.getConstraints());

        Problem p = new Problem(new TreeSet<Integer>(constraints[0].keySet()),
                new TreeSet<Integer>(constraints[0].keySet()),
                new LinkedList<Location>());

        p.lowerBound = getMinimum(p);
        int startingPoint = getStartingPoint(instance.getConstraints());
        p.pathTaken.add(locations.get(startingPoint));
        p.travelTo.remove(startingPoint);


        problems = new TreeSet<Problem>();
        problems.add(p);
    }

    private int getStartingPoint(List<PrecedenceConstraint> cons) {
        // TODO: Look for better start node
        int startingPoint = 0;
        boolean found;

        do {
          found = true;
          startingPoint++;
          for (PrecedenceConstraint c : cons) {
            if (c.getSecond() == startingPoint) {
                found = false;
                break;
            }
          }
        } while (!found);

        return startingPoint;
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

                p.setNewDestination(locations.get(p.travelTo.pollFirst()));
                p.setNewDestination(p.pathTaken.get(0));

                if (p.alreadyTraveled < upperBound) {
                    upperBound = p.alreadyTraveled;
                    boolean okay = setSolution(upperBound,p.pathTaken);
                    removeBadSolutions();
                }
            } else {

                for (Integer destination : p.travelTo) {
                   // if (p.alreadyVisited(locations.get(destination))) continue;

                    Problem newProblem = p.subProblem(locations.get(destination));

                    if (constraints[1].containsKey(destination)) {
                        int newAvailableDest = (Integer)constraints[1].get(destination);
                        newProblem.travelFrom.add(newAvailableDest);
                        newProblem.travelTo.add(newAvailableDest);
                    }

                    newProblem.setLowerBound(getMinimum(newProblem));

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

        for (Integer x : p.travelFrom) {
            double minimum = Double.POSITIVE_INFINITY;

            for (Integer y : p.travelTo) {
                // TODO:  if location is already visited skip ... (emulate setting infinity on already taken routes)
               // if (p.alreadyVisited(locations.get(y))) continue;
                if (getDistance(x,y) < minimum)
                    minimum = getDistance(x,y);
            }

            if (minimum != Double.POSITIVE_INFINITY)
                lowerBound += minimum;
        }

        return lowerBound;
    }
}

class Problem implements Comparable<Problem>{

    public double lowerBound;
    public double alreadyTraveled;

    public TreeSet<Integer> travelFrom;
    public TreeSet<Integer> travelTo;
    public List<Location> pathTaken;

    Problem(TreeSet<Integer> travelFrom, TreeSet<Integer> travelTo, List<Location> pathTaken) {
        this.travelFrom = travelFrom;
        this.travelTo = travelTo;
        this.pathTaken = pathTaken;
    }

    public Problem subProblem(Location nextLocation) {
        Problem p = new Problem(new TreeSet<Integer>(this.travelFrom),
                new TreeSet<Integer>(this.travelTo),
                new LinkedList<Location>(this.pathTaken));

        p.travelFrom.remove(getLastVisitedLocation().getCityId());
        p.alreadyTraveled = alreadyTraveled;
        p.setNewDestination(nextLocation);
        p.travelTo.remove(nextLocation.getCityId());

        return p;
    }

    public void setNewDestination(Location loc) {
        Location lastVisited = getLastVisitedLocation();
        alreadyTraveled += lastVisited == null ? 0 : lastVisited.distanceTo(loc);
        pathTaken.add(loc);
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = alreadyTraveled + lowerBound;
    }

    public Location getLastVisitedLocation() {
        return this.pathTaken.size() == 0 ? null : this.pathTaken.get(this.pathTaken.size() - 1);
    }

    public boolean alreadyVisited(Location loc) {
        return this.pathTaken.contains(loc);
    }

    @Override
    public int compareTo(Problem o) {
        return roundAwayFromZero(this.lowerBound - o.lowerBound);
    }

    @Override
    public String toString() {
        return "lowerBound=" + lowerBound + ";visited="+pathTaken.size();
    }

    private int roundAwayFromZero(double x) {
        return (int) (x > 0 ? Math.ceil(x) : Math.floor(x));
    }
}