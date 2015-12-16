import java.net.*;
import java.util.*;
import java.io.*;

public class bfclient {
	
	public final static String C_LINKDOWN = "LINKDOWN";
	public final static String C_LINKUP = "LINKUP";
	public final static String C_SHOWRT = "SHOWRT";
	public final static String C_CLOSE = "CLOSE";
	
	public static String localHost = "";
	public static int localPort = 0;
	public static int timeOut = 0;
	
	/*
	 * Local host classes
	 */
	public static ArrayList<NeighborInfo> myNeighbors = new ArrayList<>();
	public static NeighborState neighborState;
	public static DistanceVector distanceVector;
	
	
	static listenThread lt;
	static sendThread st;
	
	Tool tool = new Tool();
	
	public static void main(String[] args) {
		
		if (args.length < 5) {
			if (args.length == 2) {
				System.out.println("routing table is empty.");
			}
			else {
				System.out.println("invalid format.\nprogram exits.");
				System.exit(0);
			}
		} else if (args.length >= 5 && (args.length - 2) % 3 == 0) {
			
			localPort = Integer.parseInt(args[0]);
			timeOut = Integer.parseInt(args[1]);
			
			/*
			 * add itself to the myNeighbors
			 */
//			myNeighbor.add(new NeighborInfo("127.0.0.1", localPort, 0.0, true));
			try {
				myNeighbors.add(new NeighborInfo(InetAddress.getLocalHost().getHostAddress(), localPort, 0.0, true));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int i = 2; i < args.length; i+=3) {
				myNeighbors.add(new NeighborInfo(args[i], Integer.parseInt(args[i+1]), Double.parseDouble(args[i+2]), false));
			}
			
		} else {
			System.out.println("invalid format.\nprogran exits.");
			System.exit(0);
		}
		
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		neighborState = new NeighborState(myNeighbors, localHost);
		distanceVector = new DistanceVector(myNeighbors, localHost, localPort);
		distanceVector.initRoutingTable(neighborState);
		
		if (localPort == 4117) {
			for (Map.Entry<String, String> str : neighborState.nextMap.entrySet()) {
				System.out.println("from:" + str.getKey() + "; to:" + str.getValue());
			}
		}
		
		/*
		 * initializing both threads
		 */
		lt = new listenThread(localPort);
		Thread ltThread = new Thread(lt);
		ltThread.start();
		
		st = new sendThread(timeOut);
		Thread stThread = new Thread(st);
		stThread.start();
		
		/*
		 * Start console
		 */
		new bfclient();
		
	}
	
	public bfclient() {
		
		this.startConsole();
	}
	
	public void startConsole() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome to bfclient!\nIP: " + localHost + "; PORT: " + localPort);
		
		while (true) {
			String userInput = sc.nextLine().trim();
			String[] tokens = userInput.split(" ");
			if (tokens.length == 0 || tokens[0].length() == 0) {
				System.out.println("bad request");
				continue;
			}
			String request = tokens[0].toUpperCase();
			switch (request) 
			{
				case C_LINKDOWN:
					doLinkDown(tokens);
					break;
				case C_LINKUP:
					doLinkUp(tokens);
					break;
				case C_SHOWRT:
					doShowRT(tokens);
					break;
				case C_CLOSE:
					doClose();
					break;
				default:
					System.out.println("unknown request: " + userInput);
					break;
			}
		}
	}
	
	/*
	 * deal with LINKDOWN
	 */
	public void doLinkDown(String[] tokens) {
	
		ArrayList<String> temp = new ArrayList<>();
		
		if (tokens.length != 3) {
			System.out.println("Wrong format");
			System.out.println("Usage: LINKDOWN <ip> <port>");
			return;
		}
		String pattern = tokens[1] + ":" + tokens[2];
		
		if (!NeighborState.constantNeighborMap.containsKey(pattern)) {
			System.out.println("no direct link between" + localHost + ":" + localPort + " and " + pattern);
			return;
		}
		
		
		if (neighborState.linkMap.containsKey(pattern)) {
			neighborState.linkMap.replace(pattern, Double.POSITIVE_INFINITY);
			neighborState.nextMap.replace(pattern, "");
			tool.updatePacket(pattern);
			
			ArrayList<String> demo = new ArrayList<>();
			
			for (Map.Entry<String, Double> entry : neighborState.linkMap.entrySet()) {
				double cost_1 = entry.getValue();
				for (RoutingInfo ri : RoutingTable.routingTable) {
					double cost_2 = ri.cost;
					String next = ri.nextHop;
					if (cost_1 == cost_2 && next.equals(pattern)) {
						demo.add(entry.getKey());
					}
				}
			}
			
			for (String str : demo) {
				neighborState.linkMap.replace(str, Double.POSITIVE_INFINITY);
				neighborState.nextMap.replace(str, "");
			}
			
			RoutingTable.deleteRoutingInfo(pattern);
			
			for (NeighborInfo info : myNeighbors) {
				if (info.toString().equals(pattern)) {
					info.isAlive = false;
					break;
				}
			}
			
			for (RoutingInfo ri : RoutingTable.routingTable) {
				if (ri.nextHop.equals(pattern)) {
					temp.add(ri.destinaton);
				}
			}
			
			distanceVector.routingTable.updataRoutingTable(pattern);
			
			for (Map.Entry<String, Double> entry : NeighborState.constantNeighborMap.entrySet()) {
				if (temp.contains(entry.getKey())) {
					neighborState.linkMap.replace(entry.getKey(), entry.getValue());
					neighborState.nextMap.replace(entry.getKey(), entry.getKey());
					distanceVector.routingTable.addToRoutingTable(new RoutingInfo(entry.getKey(), entry.getValue(), entry.getKey(), true));

				}
			}

		} else {
			System.out.println("not exist");
		}
	}
	
	/*
	 * deal with LINKUP
	 */
	public void doLinkUp(String[] tokens) {
		
		boolean findEntry = false;
		
		if (tokens.length != 2) {
			System.out.println("Wrong format");
			System.out.println("Usage: LINKUP <ip>:<port>");
		}
		
		if (!NeighborState.constantNeighborMap.containsKey(tokens[1])) {
			System.out.println("no direct link between " + localHost + ":" + localPort + " and " + tokens[1]);
			return;
		}
		
		for (NeighborInfo info : myNeighbors) {
			if (info.toString().equals(tokens[1])) {
				
				findEntry = true;
				info.isAlive = true;
				
				if (neighborState.linkMap.get(tokens[1]) > info.weight) {
					neighborState.linkMap.replace(tokens[1], info.weight);
					neighborState.nextMap.replace(tokens[1], tokens[1]);
					RoutingTable.deleteRoutingInfo(tokens[1]);
					RoutingTable.routingTable.add(new RoutingInfo(tokens[1], info.weight, tokens[1], true));
				}
				
				tool.updatePacket(tokens[1]);
				
				break;
			}
		}
		if (!findEntry) {
			System.out.println("not exist");
		}
	}
	
	/*
	 * deal with SHOWRT
	 */
	public void doShowRT(String[] tokens) {
		distanceVector.routingTable.printRoutingTable();
	}
	
	/*
	 * deal with CLOSE
	 */
	public void doClose(){
		System.out.println("client exist...");
		System.exit(0);
	}
}

/*
 * Listening for incoming messages
 */
class listenThread implements Runnable {

	DatagramSocket listenSocket;
	DatagramPacket incomingPacket;
	
	byte[] incomingPacketByte = new byte[60 * 1024];
	ObjectInputStream ois;
	
	private int listenPort;
	
	Tool tool = new Tool();
	
	public listenThread(int listenPort) {
		// TODO Auto-generated constructor stub
		this.listenPort = listenPort;
	}
	
	public void updateRT(NeighborState incomingState) {

		int isChanged = bfclient.distanceVector.updateRoutingTable(incomingState, bfclient.neighborState);
		if (isChanged == 1) {
			tool.broadcastPacket();
		}
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			listenSocket = new DatagramSocket(listenPort);
			
			while (true) {
				incomingPacket = new DatagramPacket(incomingPacketByte, incomingPacketByte.length);
				try {
					listenSocket.receive(incomingPacket);
					if (MyProtocol.CheckCheckSum(incomingPacket)) {
						MyProtocol my = new MyProtocol(incomingPacketByte);
						ois = new ObjectInputStream(new ByteArrayInputStream(my.getRcvData()));
						try {
							
							NeighborState incomingNeighborState = (NeighborState) ois.readObject();
							
							updateRT(incomingNeighborState);
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						System.out.println("Packet broken.");
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

/*
 * send messages to neighbors
 */
class sendThread implements Runnable {

	DatagramPacket outPacket;
	DatagramSocket outSocket;
	
	private int timeOut;
	private byte[] outPacketByte = new byte[60 * 1024];
	
	ObjectOutputStream oos;
	ByteArrayOutputStream baos;
	
	Tool tool = new Tool();
	
	public sendThread(int timeOut) {
		this.timeOut = timeOut;
	}
	
	public byte[] constructOutPacket() {
		MyProtocol mp = new MyProtocol();
		mp.setSendData(outPacketByte);
		return mp.constructPacket();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Thread.sleep(timeOut * 1000);
				tool.broadcastPacket();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}

class Tool {
	
	DatagramPacket outPacket;
	DatagramSocket outSocket;
	
	ObjectOutputStream oos;
	ByteArrayOutputStream baos;
	
	byte[] outPacketByte;
	
	/*
	 * sendPacket needs to be synchronized, because two threads are calling it
	 */
	public synchronized void broadcastPacket() {
		
		try {
			outSocket = new DatagramSocket();
			
			try {
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(bfclient.neighborState);
				outPacketByte = new byte[60 * 1024];
				outPacketByte = baos.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] customizedData = new byte[60 * 1024];
			customizedData = constructOutPacket(outPacketByte);
			
			for (NeighborInfo ni : bfclient.myNeighbors) {
				if (ni.isAlive && ni.isSelf == false) {
					try {
						outPacket = new DatagramPacket(customizedData, customizedData.length, InetAddress.getByName(ni.dstIP), ni.dstPort);
						outSocket.send(outPacket);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("fail to create send socket");
		} finally {
			outSocket.close();	
			try {
				oos.close();
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	public void updatePacket(String destination) {
		try {
			outSocket = new DatagramSocket();
			
			try {
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(bfclient.neighborState);
				outPacketByte = new byte[60 * 1024];
				outPacketByte = baos.toByteArray();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] customizedData = new byte[60 * 1024];
			customizedData = constructOutPacket(outPacketByte);
			
			String[] tokens = destination.split(":");
			
			try {
				outPacket = new DatagramPacket(customizedData, customizedData.length, InetAddress.getByName(tokens[0]), Integer.parseInt(tokens[1]));
				outSocket.send(outPacket);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("fail to create send socket");
		} finally {
			outSocket.close();	
			try {
				oos.close();
				baos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public byte[] constructOutPacket(byte[] outPacketByte) {
		MyProtocol mp = new MyProtocol();
		mp.setSendData(outPacketByte);
		return mp.constructPacket();
	}
	
}