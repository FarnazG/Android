# Fall Detection Project

# Project contents:
* Client side or Android Application
* Server side
* Creating Machine learning model 
* SQLite data base
* Directory of files

# Project steps:
1. Use a dataset for training a Deep Nueral Network model to classify the data into 0 and 1
2. Define a server
3. Create an android application to get the accelerometer and gyroscope sensors data:
   * Use the DNN classifier result file which classifies the sensor data as 0(not fall) or 1(fall)
   * In case of fall :
     - get the location from the gps
     - connect to a server and send the message "patient fell", "Longtitude", "Latitude"
     - send an SMS to a specific phone number "patient fell", "Longtitude", "Latitude"
4. Define a SQLit database
5. Save the sensors data into the database

# Project Google slide presentation
https://docs.google.com/presentation/d/1wj2Z6ADtlN2GYrKSlUC5Lwms0PyHHR-1znAOkIHks_w/edit?usp=sharing

