# CS6378 Advanced operating system, Spring 2017 
### Project 3: Implemtation of distributed mutual exclusion algorithm

##### Build maven project
    > mvn clean package assembly:single

The build-in configuration file is as follow

    # Number of nodes | mean inter request delay | mean critical section delay | number of total critical section per node 
    5 20 10 1000 
    0 dc02 60001
    1 dc03 60002
    2 dc04 60003
    3 dc05 60004
    4 dc06 60005

##### Run Lamport mutual exclusion algorithm
    > java -Dconnection.mode="loopback" -jar dmLauncher.jar 60001 0 & \
      java -Dconnection.mode="loopback" -jar dmLauncher.jar 60002 1 & \
      java -Dconnection.mode="loopback" -jar dmLauncher.jar 60003 2 & \
      java -Dconnection.mode="loopback" -jar dmLauncher.jar 60004 3 & \
      java -Dconnection.mode="loopback" -jar dmLauncher.jar 60005 4 
      
##### Run Ricart and Algrawals's mutual exclusion algorithm
    > java -Dconnection.mode="loopback" -Dmutex.algorithm="mutex.ricart.and.agrawala" -jar dmLauncher.jar 60001 0 & \
      java -Dconnection.mode="loopback" -Dmutex.algorithm="mutex.ricart.and.agrawala" -jar dmLauncher.jar 60002 1 & \
      java -Dconnection.mode="loopback" -Dmutex.algorithm="mutex.ricart.and.agrawala" -jar dmLauncher.jar 60003 2 & \
      java -Dconnection.mode="loopback" -Dmutex.algorithm="mutex.ricart.and.agrawala" -jar dmLauncher.jar 60004 3 & \
      java -Dconnection.mode="loopback" -Dmutex.algorithm="mutex.ricart.and.agrawala" -jar dmLauncher.jar 60005 4 
##### Run remotely
On dcxx linux server 

    > ./launch.sh
