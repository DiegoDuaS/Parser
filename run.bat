@echo off
set CLASSPATH=bin
javac -d bin -cp demo/src/main/java demo/src/main/java/Yalp.java
java -cp bin Yalp
