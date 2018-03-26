###############################################################
# Course 	: CSCI 4780/6780 Distributed Computing Systems
# Project 3	: Persistent and Asynchronous Multicast System
# Team Members  : Chirag Jain, Shubhi Jain
# Date Submitted: 04/21/17
###############################################################

Instructions:
1. There are two packages inside src folder - multicastcoordinator and participant. 
   The multicastcoordinator package contains following java files:
	MulticastCoordinator.java	
	ParticipantDetails.java 
 
   The participant package contains following java files:
	Participant.java
	ParticipantAsClient.java
	ParticipantAsServer.java
	ParticipantMediator.java

2. To compile and run the classes:
	  i. Make sure that you are in src folder.
 	 ii. Run command javac multicastcoordinator/*.java
	iii. Run command javac participant/*.java
	 iv. For running the server, run command java multicastcoordinator.MulticastCoordinator 'configuration-file-name' 
			Example: java multicastcoordinator.MulticastCoordinator PP3-coordinator-conf.txt
	  v. For running the client, run command java participant.Participant 'configuration-file-name'
			Example: java participant.Participant PP3-participant-conf.txt

This project was done in its entirety by Chirag Jain and Shubhi Jain. We hereby state that we have not received unauthorized help of any form.
