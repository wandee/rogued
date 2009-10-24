@ECHO OFF
ECHO Running DHCPServer using config file
ECHO java -ea bin\DHCPServer -c settings.txt
java -ea -cp .\bin DHCPServer -c settings.txt
PAUSE
