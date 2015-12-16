import java.util.*;

public class DistanceVector {
	
	public String myself;
	public int myPort;
	public String destination;
	public ArrayList<NeighborInfo> myNeighbors;
	
	public boolean cmdHost = false;
	
	public RoutingTable routingTable;
	
	public DistanceVector(ArrayList<NeighborInfo> myNeighbors, String myself, int myPort) {
		
		this.myself = myself;
		this.myPort = myPort;
		this.destination = this.myself + ":" + Integer.toString(this.myPort);
		
		this.myNeighbors = new ArrayList<>();
		this.myNeighbors = myNeighbors;
		
		routingTable = new RoutingTable();
	}
	
	public void initRoutingTable(NeighborState neighborState) {
		for (Map.Entry<String, Double> entry : neighborState.linkMap.entrySet()) {
			if (!entry.getKey().equals(destination)) {
				routingTable.addToRoutingTable(new RoutingInfo(entry.getKey(), entry.getValue(), entry.getKey(), true));
			}
			else {
				routingTable.addToRoutingTable(new RoutingInfo(entry.getKey(), entry.getValue(), entry.getKey(), false));
			}
		}
	}
	
	public int updateRoutingTable(NeighborState incomingState, NeighborState oldState) {
		
		int flag = 0;
		
		
//		if (myPort == 4118) {
//			for (Map.Entry<String, Double> m : oldState.linkMap.entrySet()) {
//				System.out.println("4117 has:" + m.getKey() + " : " + m.getValue());
//			}
//		}
		
		String other = "";
		String self = this.myself + ":" + this.myPort;
		for (Map.Entry<String, Double> me : incomingState.linkMap.entrySet()) {
			if (me.getValue() == 0.0) {
				other = me.getKey();
				break;
			} 
		}
		
		for (Map.Entry<String, Double> entry : incomingState.linkMap.entrySet()) {
			/* if the info of this entry is sender itself, do nothing 
			 * else update
			 */
			if (entry.getKey().equals(other)) {

			} else if (entry.getKey().equals(self)) {
				/*
				 * LINKDOWN or OFFLINE: determine whether the distance is set to infinity
				 */
				if (entry.getValue() == Double.POSITIVE_INFINITY && oldState.linkMap.get(other) != Double.POSITIVE_INFINITY) {
					System.out.println(other + " set me to infinity");
					oldState.linkMap.replace(other, Double.POSITIVE_INFINITY);
					
					RoutingTable.deleteRoutingInfo(other);
					
					for (NeighborInfo ni : bfclient.myNeighbors) {
						String destination = ni.dstIP + ":" + Integer.toString(ni.dstPort);
						if (destination.equals(other)) {
							ni.isAlive = false;
						}
					}
					
					/*
					 * for orphans
					 */
					for (Map.Entry<String, String> next : oldState.nextMap.entrySet()) {
						if (next.getValue().equals(other)) {
							next.setValue("");
							oldState.linkMap.replace(next.getKey(), Double.POSITIVE_INFINITY); 
							RoutingTable.deleteRoutingInfo(other);
							
						}
					}
					
					flag = 1;
				}
				else {
					for (NeighborInfo ni : myNeighbors) {
						String pattern = ni.dstIP + ":" + Integer.toString(ni.dstPort);
						if (pattern.equals(other) && ni.isAlive == false) {
							ni.isAlive = true;
							if (oldState.linkMap.get(other) > entry.getValue()) {
								oldState.linkMap.replace(other, entry.getValue());
								oldState.nextMap.replace(other, other);
								RoutingTable.deleteRoutingInfo(pattern);
								routingTable.addToRoutingTable(new RoutingInfo(pattern, entry.getValue(), pattern, true));
								flag = 1;
							}
							
						}
					}
				}
				
			} 
			else {
				
				
				if (oldState.linkMap.containsKey(entry.getKey())) {
					
					double oldToIncoming = oldState.linkMap.get(other);
					double incomingToThird = incomingState.linkMap.get(entry.getKey());
					double oldToThird = oldState.linkMap.get(entry.getKey());
					/*
					 * determine whether a link is down
					 */
					if (incomingToThird == Double.POSITIVE_INFINITY) {
						/*
						 * search its routing table, find a path self -> incoming -> third
						 */
						for (RoutingInfo ri : RoutingTable.routingTable) {
							String destination = ri.destinaton;
							String nextHop = ri.nextHop;
							
							if (destination.equals(entry.getKey()) && nextHop.equals(other)) {
								
								RoutingTable.routingTable.remove(ri);
								
								linkdownChange(destination, oldState);
								
								flag = 1;
								return flag;
							}
						}
						
					}
					
					/*
					 * update new routing table entry
					 */
					if ((oldToIncoming + incomingToThird) < oldToThird) {
						boolean isPermit = true; 
						for (Map.Entry<String, String> next : incomingState.nextMap.entrySet()) {
							if (entry.getKey().equals(next.getKey()) && self.equals(next.getValue())) {
								System.out.println("will causing cycle, ignore!!");
								
								isPermit = false;
							}
						}
						if (isPermit) {
							double newCost = oldToIncoming + incomingToThird;
							/*
							 * update routing table
							 */
//							routingTable.updataRoutingTable(entry.getKey());
							RoutingTable.deleteRoutingInfo(entry.getKey());
							routingTable.addToRoutingTable(new RoutingInfo(entry.getKey(), newCost, other, true));
							/*
							 * update distance vector
							 */
							oldState.linkMap.replace(entry.getKey(), newCost);
							
							oldState.nextMap.replace(entry.getKey(), other);
							
							flag = 1;
						}
					} 
					
				} 
				else {
					String next = other;
					double newCost = entry.getValue() + oldState.linkMap.get(other);
					for (RoutingInfo ri : RoutingTable.routingTable) {
						if (ri.cost == oldState.linkMap.get(other)) {
							next = ri.nextHop;
						}
					}
					/*
					 * update routing table
					 */
					routingTable.addToRoutingTable(new RoutingInfo(entry.getKey(), newCost, next, true));
					/*
					 * update distance vector
					 */
					oldState.linkMap.put(entry.getKey(), newCost);
					
					oldState.nextMap.put(entry.getKey(), next);
					
					flag = 1;
				}
				
			}
		}
		
		return flag;
	}
	
	public void linkdownChange(String destination, NeighborState oldState) {
		
		boolean hasDestination = false;
		
		for (Map.Entry<String, Double> entry : NeighborState.constantNeighborMap.entrySet()) {
			if (entry.getKey().equals(destination)) {
				routingTable.addToRoutingTable(new RoutingInfo(entry.getKey(), entry.getValue(), entry.getKey(), true));
				oldState.linkMap.replace(entry.getKey(), entry.getValue());
				
				oldState.nextMap.replace(entry.getKey(), entry.getKey());
				
				hasDestination = true;
			}
		}
		if (!hasDestination) {
			oldState.linkMap.replace(destination, Double.POSITIVE_INFINITY);
			oldState.nextMap.replace(destination, "");
		}
	}
	
}
