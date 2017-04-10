CS6378 Advanced Operating System Project 3

To run this project locally:

    $<project-root>/bin> java -Dconnection.mode="loopback" -jar dmLauncher.jar 60001 0 & \
	java -Dconnection.mode="loopback" -jar dmLauncher.jar 60002 1 & \
	java -Dconnection.mode="loopback" -jar dmLauncher.jar 60003 2 & \
	java -Dconnection.mode="loopback" -jar dmLauncher.jar 60004 3 & \
	java -Dconnection.mode="loopback" -jar dmLauncher.jar 60005 4 