@echo off
REM Script for Windows
javac HotelierClientMain.java
start /B /WAIT java HotelierClientMain
del *.class