# Makefile for CS 4390 Project 1
JAVAC = javac
JAVA  = java

all: compile

compile:
	$(JAVAC) RecursiveDescentParser.java
	$(JAVAC) MathEngine.java
	$(JAVAC) MathServer.java ClientHandler.java
	$(JAVAC) MathClient.java

server:
	$(JAVA) MathServer

client:
	$(JAVA) MathClient

clean:
	rm -f *.class
