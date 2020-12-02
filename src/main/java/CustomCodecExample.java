import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import software.aws.mcs.auth.SigV4AuthProvider;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class CustomCodecExample {

    private static String readLineByLine(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {

        List<InetSocketAddress> contactPoints =
                Collections.singletonList(
                        InetSocketAddress.createUnresolved("cassandra.us-east-1.amazonaws.com", 443));

        TypeCodec<String> s3Codec = new CqlUuidToTextCodec("ks", "test2");

        CqlSession session = CqlSession.builder()
                .addContactPoints(contactPoints)
                .withSslContext(SSLContext.getDefault())
                .withLocalDatacenter("us-east-1")
                .withAuthProvider(new SigV4AuthProvider("us-east-1"))
                // Add our Type Codec to integrate with Amazon S3
                .addTypeCodecs(s3Codec)
                .build();

        // Load the large file that we can to store on S3 and UUID pointer in Amazon Keyspaces.
        // Amazon Keyspaces quota is 1MB
        String object = readLineByLine(System.getProperty("user.dir")+"/src/main/resources/large-file.csv");

        // Let's write large file to S3 and insert an UUID in Amazon Keyspaces
        PreparedStatement ps = session.prepare("INSERT INTO ks.test2 (k, v) VALUES (?, ?)");

        for (int i=0; i<100; i++) {
            long startTime = System.nanoTime();
            session.execute(
                    ps.boundStatementBuilder()
                            .setInt("k", i)
                            .set("v", object, s3Codec)
                            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                            .build());
            long elapsedTime = System.nanoTime() - startTime;
            System.out.println("Elapsed time to write the file "+i+" to S3:" + TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) + "ms");
        }

        for (int i=0; i<100; i++) {
            long startTime = System.nanoTime();
            // Let's read large file from S3 by providing primary key from Amazon Keyspaces
            PreparedStatement ps1 = session.prepare("SELECT k,v FROM ks.test2 WHERE k = ?");

            ResultSet rs = session.execute(ps1.
                    boundStatementBuilder().
                    setInt("k", i).
                    build());
            String v = rs.one().get("v", s3Codec);
            long elapsedTime = System.nanoTime() - startTime;

            System.out.println("Elapsed time to read the file "+i+" to S3:" + TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) + "ms");
        }
        session.close();
    }
}