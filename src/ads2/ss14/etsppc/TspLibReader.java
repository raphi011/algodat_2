package ads2.ss14.etsppc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TspLibReader {
	private static Pattern DIMENSION_PATTERN = Pattern.compile("DIMENSION:\\s+([0-9]+)");
	
	private static Pattern THRESHOLD_PATTERN = Pattern.compile("THRESHOLD:\\s+([0-9]+(\\.[0-9]+)?)");
	
	private static String CITY_HEADER = "NODE_COORD_SECTION";
	private static Pattern CITY_PATTERN = Pattern.compile("\\s*([0-9]+)\\s+([0-9]+(\\.[0-9]+)?)\\s+([0-9]+(\\.[0-9]+)?)");
	
	private static String PRECEDENCE_HEADER = "PRECEDENCE_SECTION";
	private static Pattern PRECEDENCE_PATTERN = Pattern.compile("\\s*([0-9]+)\\s+([0-9]+)");	
	
	private String filePath;

	public TspLibReader(String filePath) {
		this.filePath = filePath;
	}
	
	public ETSPPCInstance readInstance() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		
		Matcher m;
		String line;
		for(line = reader.readLine(), m = DIMENSION_PATTERN.matcher(line); !m.matches(); line = reader.readLine(), m = DIMENSION_PATTERN.matcher(line)) {
		}
		
		int numCities = Integer.parseInt(m.group(1));
		
		for(m = THRESHOLD_PATTERN.matcher(line); !m.matches(); line = reader.readLine(), m = THRESHOLD_PATTERN.matcher(line)) {
		}
		
		double threshold = Double.parseDouble(m.group(1));
		
		for(;!line.equals(CITY_HEADER); line = reader.readLine()) {
		}
		
		Map<Integer, Location> allCities = new HashMap<Integer, Location>(numCities);
		int id;
		double x, y;
		
		for(line = reader.readLine(), m = CITY_PATTERN.matcher(line); m.matches(); line = reader.readLine(), m = CITY_PATTERN.matcher(line)) {
			id = Integer.parseInt(m.group(1));
			x = Double.parseDouble(m.group(2));
			y = Double.parseDouble(m.group(4));
			
			allCities.put(id, new Location(id, x, y));
		}
		
		for(; !line.equals(PRECEDENCE_HEADER); line = reader.readLine()) {
		}
		
		List<PrecedenceConstraint> constraints = new ArrayList<PrecedenceConstraint>();
		int a, b;
		for(line = reader.readLine(), m = PRECEDENCE_PATTERN.matcher(line); m.matches(); line = reader.readLine(), m = PRECEDENCE_PATTERN.matcher(line)) {
			a = Integer.parseInt(m.group(1));
			b = Integer.parseInt(m.group(2));
			
			constraints.add(new PrecedenceConstraint(a, b));
		}
		
		reader.close();
		return new ETSPPCInstance(allCities, constraints, threshold);
	}
}
