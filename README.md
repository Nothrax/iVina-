# iot - plot
First exercise


## Scenario
Friend of mine owns a multiple bee hives. He has to weight them multiple times a year to know, when to bottle the honey, or when to feed sugar to the bees. He has bought an iot weight, that every hour weights the bee hives and sends the value with temperature and humidity to a server database using gsm. Second friend has vine cellar and needs to monitor temperature, humidity and carbon dioxide concentration in that cellar. He also baught a sensor that uploads these data to server database using network. Both of the friends are BFU (Basic Franta User) and do not know what a database even is. So they asked me to create a app to visualize the data from theirs expensive sensors. The idea is, that they login to the app, and using user-friendly forms they will setup frequent graphs they want to see or values they want to know and each time they want, they will just launch the app and check the values.

## Requirements
* Simple login functionailty
* Plot a graph or show a value of measured data
* Save frequently use graphs/values for easy access
* Plot historic data
* User friendly interface and data access, usable by BFU

## Technologies
* Android
* Android studio IDE by JetBrains for development
* Kotlin for android app
* Java for android app
* InfluxDB for data storage
* Python for virtual sensors and test data generation

## Time plan

* 3h - project planning (this file) - reality 4h
* 3h - influx data model 2h
* 3h - python fake sensor for data generation 3h
* 3h - influx deployment 4h
* 4h - android ui mock - reality 5h
* 3h - user login window - reality 4h
* 4h - main menu window - reality 3h
* 4h - influx connectivity - reality 5h
* 4h - graph plot functionality and graph window - reality 2h
* 4h - consistent data storage for user settings - reality 3h
* 4h - creation of frequent graphs - reality - 5h
* 3h - user testing - reality 4h

## Questions to ask
* How big time window is needed in graph?
* How many frequent graphs want user?
* For how long are data stored in database?
* How many devices will be accesible to the user?
* Can multiple users access one databese?
* Are there different privileges for different users?