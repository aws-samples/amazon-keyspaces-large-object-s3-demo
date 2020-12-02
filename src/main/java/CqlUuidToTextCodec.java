import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class CqlUuidToTextCodec extends MappingCodec<UUID, String> {

    String keyspaceName;
    String tableName;
    String bucketName;
    static AmazonS3 s3Client;

    protected CqlUuidToTextCodec(String bucketName, String keyspaceName, String tableName, AmazonS3 s3Client) {
        super(TypeCodecs.UUID, GenericType.of(String.class));
        this.keyspaceName = keyspaceName;
        this.tableName = tableName;
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    @Nullable
    @Override
    protected String innerToOuter(@Nullable UUID uuid) {

        S3Object fullObject = null;
        StringBuffer s = new StringBuffer();

        try {
            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, keyspaceName+"/"+tableName+"/"+uuid.toString()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                s.append(line);
            }
        } catch (SdkClientException|IOException e) {
            e.printStackTrace();
        }
        return s.toString();
    }

    @Nullable
    @Override
    protected UUID outerToInner(@Nullable String s) {
        UUID uuid = UUID.randomUUID();
        s3Client.putObject(bucketName,keyspaceName+"/"+tableName+"/"+uuid.toString(),s);
        return uuid;
    }

}