					CSEE W4118
			     Computer Network Assignment No.3
					 Rui Lu

PROJECT DOCUMENTATION
=====================
In this assignment, I implement a simple version of the distributed Bellman-Ford algorithm. The algorithm will operate using a set of distributed client process. The clients perform the distributed distance computation and support a user interface which can operate on the console. UI allows users to edit links to the neighbors and view the routing table. Every client is identified by the <IP address, Port> tuple. As the requirement, I implemented this client program and accomplished almost functions.

PROGRAM FEATURE
===============
1. In order to distinguish Distance Vector from Routing Table, although they have close connection, I use 4 classes to realize this program. NeighborInfo.class and NeighborState.class are responsible to store and modify distance vector information. RoutingInfo.class and RoutingTable.class are responsible to store and update routing information. All 4 classes work closely.
2. The protocol I adopt is referred to lsphate from GitHub.(url: https://github.com/lsphate/2014_W4119_PA3). Packet adopting this protocol has 20-byte header, and the total length equals to the length of header plus the length of data, which is changeable.
3. Every client has two threads, one for sending and another for receiving. Sending TIMEOUT is set by command line. Receiving thread waits on its socket until its distance vector changes or until TIMEOUT seconds pass.
4. There are two kinds of sending functions. One is called updatePacket(), which is used for sending updating messages to its neighbors immediately. The other one is called broadcastPacket, just as its name, it is used to broadcast DV information every TIMEOUT interval.


USER SCENARIOS:
===============
Step 1 - Boot clients:

(1)client 192.168.0.101:4115:
java bfclient 4115 3 192.168.0.101 4116 5.0 192.168.0.101 4118 30.0
Welcome to bfclient!
IP: 192.168.0.101; PORT: 4115
SHOWRT
<Wed Dec 16 02:40:46 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4116, Cost = 5.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4118, Cost = 10.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4117, Cost = 15.0, Link = (192.168.0.101:4116)

(2)client 192.168.0.101:4116:
java bfclient 4116 3 192.168.0.101 4115 5.0 192.168.0.101 4117 10 192.168.0.101 4118 5.0
Welcome to bfclient!
IP: 192.168.0.101; PORT: 4116
SHOWRT
<Wed Dec 16 02:40:49 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4115, Cost = 5.0, Link = (192.168.0.101:4115)
Destination = 192.168.0.101:4118, Cost = 5.0, Link = (192.168.0.101:4118)
Destination = 192.168.0.101:4117, Cost = 10.0, Link = (192.168.0.101:4117)

(3)client 192.168.0.101:4118
java bfclient 4118 3 192.168.0.101 4115 30 192.168.0.101 4116 5
Welcome to bfclient!
IP: 192.168.0.101; PORT: 4118
SHOWRT
<Wed Dec 16 02:40:47 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4116, Cost = 5.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4117, Cost = 15.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4115, Cost = 10.0, Link = (192.168.0.101:4116)

(4)client 192.168.0.101:4117:
java bfclient 4117 3 192.168.0.101 4116 10
Welcome to bfclient!
IP: 192.168.0.101; PORT: 4117
SHOWRT
<Wed Dec 16 02:40:51 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4116, Cost = 10.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4118, Cost = 15.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4115, Cost = 15.0, Link = (192.168.0.101:4116)

Step 2: test LINKDOWN 192.168.0.101 4116:
(1)client 192.168.0.101:4115:
LINKDOWN 192.168.0.101 4116
SHOWRT
<Wed Dec 16 02:41:02 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4118, Cost = 30.0, Link = (192.168.0.101:4118)
Destination = 192.168.0.101:4117, Cost = 45.0, Link = (192.168.0.101:4118)
Destination = 192.168.0.101:4116, Cost = 35.0, Link = (192.168.0.101:4118)

(2)client 192.168.0.101:4116:
192.168.0.101:4115 set me to infinity
showrt
<Wed Dec 16 02:41:06 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4118, Cost = 5.0, Link = (192.168.0.101:4118)
Destination = 192.168.0.101:4117, Cost = 10.0, Link = (192.168.0.101:4117)
Destination = 192.168.0.101:4115, Cost = 35.0, Link = (192.168.0.101:4118)

Step 3: test LINKUP 192.168.0.101:4116:
(1)client 192.168.0.101:4115:
LINKUP 192.168.0.101:4116:
SHOWRT
<Wed Dec 16 02:41:21 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4116, Cost = 5.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4118, Cost = 10.0, Link = (192.168.0.101:4116)
Destination = 192.168.0.101:4117, Cost = 15.0, Link = (192.168.0.101:4116)
(2)client 192.168.0.101:4116:
192.168.0.101:4115 set me to infinity
SHOWRT
<Wed Dec 16 02:41:26 EST 2015> Distance vector list is:
Destination = 192.168.0.101:4118, Cost = 5.0, Link = (192.168.0.101:4118)
Destination = 192.168.0.101:4117, Cost = 10.0, Link = (192.168.0.101:4117)
Destination = 192.168.0.101:4115, Cost = 5.0, Link = (192.168.0.101:4115)

DATA STRUCTURE
==============
1. ConcurrentHashMap<String, Double> linkMap = new ConcurrentHashMap<>();
   LinkMap is used to store current distance vectors of a client.
2. HashMap<String, Double> constantNeighborMap = new HashMap<>();
   constantNeighborMap is used to store the original neighbor information, it is useful    when LINKUP is called.
3. ConcurrentHashMap<String, String> nextMap = new ConcurrentHashMap<>(); 
   nextMap is used to store a client’s information of destination and its corresponding nextHop, which is very helpful to handle the cycle problem in graph.
4. NeighborState neighborState = new NeighborState();
   This class is used for sending. There’s function to wrap it and send it through output stream.
5. DistanceVector distanceVector = new DistanceVector();
   This class is the kernel of this assignment. In this class we initialize routing table and update routing table using Bellman Ford algorithm.

NOTICE
======
According to the requirement, LINKUP and LINKDOWN have different syntaxes:
(1) LINKDOWN 192.168.0.101 4116
(2) LINKUP 192.168.0.101:4116

BUG
===
1. Sometime a client will face cycle problem, while other clients act just fine.
2. Stilling need to test…

HOW TO USE
==========
1. Extract rl2784_java.zip, 
2. Input make in command line
3. Go to the classes directory
4. Input java bfclient <PORT> <TIMEOUT> <IP> <PORT> <COST> …