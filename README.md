# BlameInspector  ![maven] (https://img.shields.io/badge/maven--version-3.3.3-blue.svg) ![junit] (https://img.shields.io/badge/junit--tests-4.1.1-orange.svg) ![build] (https://img.shields.io/badge/build-passing-green.svg)

## Purpose 

Command line utility, which allows to define(optionally set) assignees of issues with stacktraces using your git/svn repo.

## Building

Building using maven:

    mvn package

After build copy one-jar.properties to your target directory, in target directory run:

    jar uf BlameInspector-1.0-SNAPSHOT.one-jar.jar one-jar.properties

## Run

First of all you have to create a **config.properties** file with data about your repository. Have a look in **default.properties** file, for example. 

Secondly, you run BlameInspector like this, in command line in directory, where **config.properties** file situated: 

    java -jar BlameInspector.one-jar.jar -p $RepositoryName$ -t $TicketNumber$

## Usage examples

Show probable assignee for 24 ticket on MyProject:
     
    java -jar BlameInspector.one-jar.jar -p MyProject -t 24

Set assignee for tickets from 1 to 4 on MyProject:

    java -jar BlameInspector.one-jar.jar -p MyProject -r 1 4 -f -X

Set assignee for tickets from 1 until tickets end on MyProject and show exception stacktrace if occurs:

    java -jar BlameInspector.one-jar.jar -p MyProject -r 1 -f -X

## Using programm

Before running programm with appropriate arguments, you have to create config.properties file in corresponding directory. Copy the structure of config file from default.properties and insert your data.

## Running tests

If you want all tests to run succesfully on your machine, please, clone the following projects on your machine and fill info about them in config file:

* [BlameWhoTest] (https://github.com/JackSmithJunior/BlameWhoTest)
* [Kotlin] (https://github.com/JetBrains/Kotlin)
* [Guava] (https://github.com/google/guava)