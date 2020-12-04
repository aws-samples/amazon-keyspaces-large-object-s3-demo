# Amazon Keyspaces (for Apache Cassandra) and S3 Codec demo.

### Description

Custom S3 Codec supports transparent, user-configurable mapping of UUID pointers to S3 objects.

Practical use cases that justify such a feature:

1. Your data is larger than 1MB, which exceeds the Amazon Keyspaces row size quota  
2. Your data needs to be accessible via Amazon Keyspaces (the driver side)  
3. You want to reduce storage costs by moving large objects to a different location for storage
4. You need to access your data infrequently 

### Prerequisites

````
    JDK 1.7.0u71 or better
    Maven 3.3.9 or better
    AWS SDK S3 1.11.907 or better
    DataStax java driver 4.9.0 or better
    Authentication Plugin for the DataStax Java Driver 4.0.3 or better
````
	
If you want to build everything at once, from the top directory run

````
    mvn install
````

### Let's create one keyspace and one table

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

### Usage

You can define S3 Codec in the following way:

````
    TypeCodec<String> s3Codec = new CqlUuidToTextCodec("your-bucket-name", "keyspace-name", "table-name", your-s3-client)
````

Once you have your S3 Codec, register it when building your session. The following is an example of how to register S3 Codec.

````
    CqlSession session = CqlSession.builder()
                    .addContactPoints(contactPoints)
                    .withSslContext(SSLContext.getDefault())
                    .withLocalDatacenter("us-east-1")
                    .withAuthProvider(new SigV4AuthProvider("us-east-1"))
                    // Add our Type Codec to integrate with Amazon S3
                    .addTypeCodecs(s3Codec)
                    .build();
````
In the above example, the driver will look up S3 codec for CQL String and Java UUID in the codec registry and will transparently pick S3 Codec for that.

You can now use the new mappings in your code:

````
     PreparedStatement ps = session.prepare("INSERT INTO ks.test2 (k, v) VALUES (?, ?)");
     session.execute(ps.boundStatementBuilder()
                            .setInt("k", i)
                            .set("v", object, s3Codec) // write String object to S3 and persist UUID pointer into ks.test2 
                            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                            .build());
````

In the above example, the driver writes your large file into Amazon S3 and inserts an uuid pointer into Amazon Keyspaces.

````
      PreparedStatement ps1 = session.prepare("SELECT k,v FROM ks.test2 WHERE k = ?");
                ResultSet rs = session.execute(ps1.
                        boundStatementBuilder().
                        setInt("k", i).
                        build());
                String v = rs.one().get("v", s3Codec); // Read S3 Object from Amazon S3
    
````
In the last example, the driver reads your large file from Amazon S3 by providing the uuid pointer in the CQL statement.

Enjoy! Feedback and PR's welcome!

## License

This library is licensed under the MIT-0 License. See the LICENSE file.