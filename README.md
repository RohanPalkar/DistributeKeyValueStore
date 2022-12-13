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

### Logging
Following environment variables are exposed for controlling the logging configuration :
- `SERVER_HOME` : The server log file will be generated under the following directory `$SERVER_HOME/log/replica.log`
- `SERVER_LOG_LEVEL` : All log4j2 supported log levels. By default `ALL`
### Resources
- [Java Tutorials - All About Sockets](https://docs.oracle.com/javase/tutorial/networking/sockets/definition.html)
- [Java Tutorials - All About Datagrams](https://docs.oracle.com/javase/tutorial/networking/datagrams/index.html)