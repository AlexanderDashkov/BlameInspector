# BlameInspector

To build:

mvn package

After build copy one-jar.properties to your target directory, in target directory run:

jar uf BlameInspector.one-jar.jar one-jar.properties

To run in one directory with git

java -jar BlameInspector.one-jar.jar -p $RepositoryName$ -t $TicketNumber$
