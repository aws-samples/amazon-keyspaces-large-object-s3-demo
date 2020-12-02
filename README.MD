# Amazon Keyspaces (for Apache Cassandra) and S3 Codec demo.

Prerequisites

    JDK 1.7.0u71 or better
    Maven 3.3.9 or better
    Snappy for java 1.1.7.3 or better
    DataStax java driver 4.9.0 or better
    Authentication Plugin for the DataStax Java Driver 4.0.3 or better
    json-simple 1.1.1 or better
	
If you want to build everything at once, from the top directory run

	````mvn install````

### Let's first create one keyspace and one tables

````
    CREATE KEYSPACE ks WITH replication = {'class':
    'com.amazonaws.cassandra.DefaultReplication'} AND durable_writes = true;
````

````
    CREATE TABLE ks.test2 (
       k int PRIMARY KEY,
       v uuid
       );
````

Enjoy! Feedback and PR's welcome!

## License

This library is licensed under the MIT-0 License. See the LICENSE file.