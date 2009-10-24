@ECHO OFF
ECHO Making DHCPServer
ECHO javac src\*.java
javac src\*.java
ECHO moving class files to bin dir
xcopy src\*.class bin\
del src\*.class
PAUSE
