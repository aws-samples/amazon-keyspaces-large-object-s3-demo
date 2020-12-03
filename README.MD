# Amazon Keyspaces (for Apache Cassandra) and S3 Codec demo.

### Description

Custom S3 Codec supports transparent, user-configurable mapping of UUID pointers to S3 objects.

Practical use cases that justify such a feature:

1. if your data is larger than 1MB that exceeds the Amazon Keyspaces row size quota  
2. if data should be available transparently for Amazon Keyspaces and other analytical frameworks like EMR, Athena, and etc. 
3. if you want to reduce storage costs by moving large objects to a different storage
4. if your data needs only infrequent access

### Prerequisites

    JDK 1.7.0u71 or better
    Maven 3.3.9 or better
    AWS SDK S3 1.11.907 or better
    DataStax java driver 4.9.0 or better
    Authentication Plugin for the DataStax Java Driver 4.0.3 or better
	
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