# BlameInspector  ![TeamCity CodeBetter](https://img.shields.io/teamcity/codebetter/bt428.svg?style=plastic) ![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven/apache-maven.svg?style=plastic)

## Purpose 

Command line utility, which allows to define(optionally set) assignees of issues with stacktraces using your git/svn repo.

## Building:

Building using maven:

    mvn package

After build copy one-jar.properties to your target directory, in target directory run:

    jar uf BlameInspector-1.0-SNAPSHOT.one-jar.jar one-jar.properties

## Run

First of all you have to create a **config.properties** file with data about your repository. Have a look in **default.properties** file, for example. 

Secondly, you run BlameInspector like this, in command line in directory, where **config.properties** file situated: 

    java -jar BlameInspector.one-jar.jar -p $RepositoryName$ -t $TicketNumber$
