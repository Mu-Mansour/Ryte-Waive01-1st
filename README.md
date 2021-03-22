# Ryte-Waive01-1st
Waive 01--Uber Clone
# An android application provides a based Taxi booking Services like Uber.its related to google maps "for only training purpose" 

## Features:
	- provides a Login for Admins/Captins/Customers.
	
	Captin: 
		-You can recieve available requests from other 			riders, which is displayed in an Android 		Dialog, which is essentially has two 			options: Accepting/Canceling.
		Once a captain click Accept,he will see the 			rider's postition on a map to pick him up.
		- you have 5 diffrent states each have its own 			  functionality  :
		 -Busy with ride
		 -online and can recieve rides 
		 -offline 
		 -waiting for the customer
		 -riding with customer 
		
		
		Customer:
		You can request a ride while looking at your 			location (which is updated very frequently) on a 		map. Once you click the 'Request Ride' button, 			your request is stored on the online database and 			if there is no availabe captains it will be also 		removed automatically . You also have the option 		to cancel your Ride, which will in turn remove it 			from the database.The closest driver in the area 		within 5km can recieve your request.
		- you have 4 diffrent states each have its own 			  functionality :
		 -Ryte and can book a ride
		 -Waiting for the captain arrival 
		 -Riding with Captain 
		 -WithCap and about to finish the ride
		 
		 
		Admin:
		-add new captains 
		-track both captains and customers details 
			"not implemented yet" 
		
		
Technologies: Kotlin, retrofit, Google Maps, REST APIs, Firebase(Auth, FCM,RealTime DataBase,Storage),
dependency injection(Hilt), MVVM pattern, Navigation Component.
