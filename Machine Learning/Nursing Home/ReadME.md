# Fall Detection Project

# Project contents:
* Client side or Android Application
* Server side
* Creating Machine learning model 
* SQLite data base
* Directory of files


# Project steps:
1. using a dataset to train a Deep Nueral Network model 
2. define a server
3. creating an android application to get the sensors data:
   * use the DNN classifier result file and classifies the sensor data as 0(not fall) or 1(fall)
   * in case of fall:
     - get the location from the gps
     - connect to a server and send the message "patient fell", "Longtitude", "Latitude"
     - send an SMS to a specific phone number "patient fell", "Longtitude", "Latitude"
4. define a SQLit database
5. save the sensors data into the database

# Project Google slide presentation
https://docs.google.com/presentation/d/1wj2Z6ADtlN2GYrKSlUC5Lwms0PyHHR-1znAOkIHks_w/edit?usp=sharing

