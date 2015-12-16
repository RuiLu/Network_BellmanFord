import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoutingTable {

	public static CopyOnWriteArrayList<RoutingInfo> routingTable;
	
	public RoutingTable() {
		routingTable = new CopyOnWriteArrayList<>();
	}
	
	public void addToRoutingTable(RoutingInfo routingInfo) {
		synchronized (routingTable) {
			routingTable.add(routingInfo);
		}
	}
	
	public static void deleteRoutingInfo(String pattern) {
		synchronized (routingTable) {
			for (RoutingInfo info : routingTable) {
				if (info.destinaton.equals(pattern)) {
					routingTable.remove(info);
				}
			}
		}
	}
	
	public void printRoutingTable() {
		synchronized (routingTable) {
			Date now = new Date();
			System.out.println("<" + now + "> Distance vector list is:");
			for (RoutingInfo info : routingTable) {
				if (info.needToBeShown) {
					System.out.println(info.toString());
				}
			}
		}
	}
	
	public void updataRoutingTable(String pattern) {
		synchronized (routingTable) {
			for (RoutingInfo ri : routingTable) {
				if (ri.nextHop.equals(pattern)) {
//					System.out.println("delete " + pattern);
					routingTable.remove(ri);
				}
			}
		}
	}
	
}
