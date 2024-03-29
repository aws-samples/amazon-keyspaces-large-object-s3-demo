import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public class CqlUuidToTextCodec extends MappingCodec<UUID, String> {

    String keyspaceName;
    String tableName;
    String bucketName;
    AmazonS3 s3Client;

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

        try (S3Object fullObject = s3Client.getObject(new GetObjectRequest(bucketName, keyspaceName+"/"+tableName+"/"+uuid.toString())))
        {
            return IOUtils.toString(fullObject.getObjectContent());

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    protected UUID outerToInner(@Nullable String s) {
        UUID uuid = UUID.randomUUID();
        s3Client.putObject(bucketName,keyspaceName+"/"+tableName+"/"+uuid.toString(),s);
        return uuid;
    }

}