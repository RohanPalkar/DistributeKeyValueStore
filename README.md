# Distributed Key Value Store
A personal project for designing and implementing a distributed key value store. 

### Objectives

1. **CURD Operations**: Design a distributed key value store and support all CRUD operations.
2. **Membership Protocol**: Monitor the replica servers using Membership protocol - preferably Gossip protocol. 
3. **Load balancing**: Achieve load balancing via a consistent hashing ring to hash both servers and keys.
4. **Fault Tolerance**: Replicate keys on successive nodes in the ring, starting from the first node at or to the clockwise of the hashed key.
5. **Consistency Levels**: Define and implement consistency levels for Reads and Writes (preferably Quorum)
6. **Stabilization Protocol**: Recreate and balance replicas in case of failure of nodes.
7. **Coordinator Election**: To elect a node as the coordinator (via leader election / paxos)  
<br>
   
#### Membership Protocol ####
- **Protocol** : Uses a _Gossip Style Membership Protocol_.
- **Partial Membership List** : In contrast to a full-membership-list that is maintained in Virtual Synchrony mechanism, this gossip style membership maintains a partial membership list at every node/process of fixed size which is less than the total number of nodes/processes in the system.
  - `gossip.KList` - Specifies the size of the partial membership list as `K` where `K < N` and `N` is the total number of nodes/processes.

### Dependencies

Spring Boot Version: 2.7.6
Java 8 compatibility

### Logging
Following environment variables are exposed for controlling the logging configuration :
- `SERVER_HOME` _(Mandatory)_ : The server log file will be generated under the following directory `$SERVER_HOME/log/replicaXXXX.log`
- `SERVER_LOG_LEVEL` _(Default: `INFO`)_: All log4j2 supported log levels - specify level to override the default.
- `SERVER_ID` : The replica-server-Id incase of a cluster environment or a process-Id (threadId) in case of single machine environment _(emulated)_.
- `PROCESS_LOGGER` :  The logging is by default thread based logging i.e. it creates a separate log file for each thread. Setting the `PROCESS_LOGGER` for any non-null value activates the process-based logging that generates dedicated log files for each process separately. 

### Resources
- [Java Tutorials - All About Sockets](https://docs.oracle.com/javase/tutorial/networking/sockets/definition.html)
- [Java Tutorials - All About Datagrams](https://docs.oracle.com/javase/tutorial/networking/datagrams/index.html)
- [Java Concurrency - Baeldung - Guide to CompletableFuture](https://www.baeldung.com/java-completablefuture)
- [Java Concurrency - Baeldung - Guide to java.util.concurrent.Future](https://www.baeldung.com/java-future)
- [Official Spring (Boot) Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#spring-core)

#### For Building the Application Buffer

- [Baeldung - Java Concurrent Queues](https://www.baeldung.com/java-concurrent-queues)
- [Paper - Simple, Fast, and Practical Non-Blocking and Blocking Concurrent Queue Algorithms - By Maged M. Michael Michael L. Scott](chrome-extension://efaidnbmnnnibpcajpcglclefindmkaj/https://www.cs.rochester.edu/u/scott/papers/1996_PODC_queues.pdf)
- [Baeldung - LinkedBlockingQueue vs ConcurrentLinkedQueue](https://www.baeldung.com/java-queue-linkedblocking-concurrentlinked#concurrentlinkedqueue)

#### From Spring Initializr
##### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.0/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.0/gradle-plugin/reference/html/#build-image)
* [Testcontainers Cassandra Module Reference Guide](https://www.testcontainers.org/modules/databases/cassandra/)
* [Testcontainers MongoDB Module Reference Guide](https://www.testcontainers.org/modules/databases/mongodb/)
* [Testcontainers Kafka Modules Reference Guide](https://www.testcontainers.org/modules/kafka/)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#using.devtools)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#web)
* [Spring HATEOAS](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#web.spring-hateoas)
* [Thymeleaf](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#web.servlet.spring-mvc.template-engines)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#data.nosql.mongodb)
* [Spring Data for Apache Cassandra](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#data.nosql.cassandra)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#messaging.kafka)
* [Apache Kafka Streams Support](https://docs.spring.io/spring-kafka/docs/current/reference/html/#streams-kafka-streams)
* [Apache Kafka Streams Binding Capabilities of Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#_kafka_streams_binding_capabilities_of_spring_cloud_stream)
* [Testcontainers](https://www.testcontainers.org/)

##### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Building a Hypermedia-Driven RESTful Web Service](https://spring.io/guides/gs/rest-hateoas/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Spring Data for Apache Cassandra](https://spring.io/guides/gs/accessing-data-cassandra/)
* [Samples for using Apache Kafka Streams with Spring Cloud stream](https://github.com/spring-cloud/spring-cloud-stream-samples/tree/master/kafka-streams-samples)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)