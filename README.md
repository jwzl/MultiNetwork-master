# MultiNetwork-master
Android Networking 

								Android network API
			---	Control/Monitor	API ---		|	 		--- Data communication API ---
		ConnectivityManager					    |
		ConnectivityService					    |	  	Java Socket / Android Jni socket/ packaged API based on socket(
		NetworkFactory						      |      	such as http/ssl and so on.)
		Netd								            |     	Linux Socket
		Linux Network API					      |
											              |

Control/Monitor	API: like the control bus of a device. The primary responsibilities of these API:
	1) Monitor network connections (Wi-Fi, mobile, bt , Ethernet etc.)
	2) Send broadcast intents when network connectivity changes
	3) Attempt to "fail over" to another network when connectivity to a network is lost
	4) Provide an API that allows applications to query the coarse-grained or fine-grained state of the available networks
	5) Provide an API that allows applications to request and select networks for their data traffic
	6) start/stop the assigned network, etc.

Data communication API: The primary responsibilities of these API:
	
	1) Provide some API for an application to communicate with other peer in this network.
