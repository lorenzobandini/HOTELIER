@echo off
REM Script for Windows
javac HotelierServer.java
start /B /WAIT java HotelierServer
del *.class