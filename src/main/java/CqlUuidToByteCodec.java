import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public class CqlUuidToByteCodec extends MappingCodec<UUID, byte[]> {

    protected CqlUuidToByteCodec() {
        super(TypeCodecs.UUID, GenericType.of(byte[].class));
    }

    @Nullable
    @Override
    protected byte[] innerToOuter(UUID uuid) {
        System.out.println("0");
        GetObjectFromS3 getObjectFromS3 = new GetObjectFromS3();
        byte[] result = new byte[0];
        try {
            System.out.println("1");
            result = String.valueOf(getObjectFromS3.getObject("company-keyspaces-large-objects", "keyspace_large_objects/table_large_objects/"+uuid.toString())).getBytes();
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Nullable
    @Override
    protected UUID outerToInner(byte[] bytes) {
        PutObjectToS3 putObjectToS3 = new PutObjectToS3();
        UUID uuid = UUID.randomUUID();
        putObjectToS3.putObject(new String(bytes), "company-keyspaces-large-objects","keyspace_large_objects/table_large_objects/"+uuid.toString());
        return uuid;
    }
}
