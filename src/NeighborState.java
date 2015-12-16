import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;


public class NeighborState implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static HashMap<String, Double> constantNeighborMap;
	public ConcurrentHashMap<String, Double> linkMap;
	public String myself;
	
	public ConcurrentHashMap<String, String> nextMap;
	
	public NeighborState(ArrayList<NeighborInfo> neighbors, String myself) {
		this.myself = myself;
		constantNeighborMap = new HashMap<>();
		linkMap = new ConcurrentHashMap<>();
		nextMap = new ConcurrentHashMap<>();
		
		for (NeighborInfo neighbor : neighbors) {
			linkMap.put(neighbor.toString(), neighbor.weight);
			/*
			 * initialize nextMap, its values is the same as linkMap
			 */
			nextMap.put(neighbor.toString(), neighbor.toString());
		}
		for (NeighborInfo neighbor : neighbors) {
			constantNeighborMap.put(neighbor.toString(), neighbor.weight);
		}
	}
	
}
