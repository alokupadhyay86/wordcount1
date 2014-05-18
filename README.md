wordcount
=========

Build : 

mvn clean install

Run Server :

mvn exec:java -Dexec.mainClass="in.freecharge.wordcount.server.FCHttpServer" -Dexec.args="<port> <corpusDirectoryPath>"
mvn exec:java -Dexec.mainClass="in.freecharge.wordcount.server.FCHttpServer" -Dexec.args="8070 /home/alok/fc"

Run Client -

mvn exec:java -Dexec.mainClass="in.freecharge.wordcount.client.FCClient" -Dexec.args=8070

Or Browser - http://localhost:8070/?query=java
