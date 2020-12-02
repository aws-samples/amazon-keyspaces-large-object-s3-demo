import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public class CqlUuidToTextCodec extends MappingCodec<UUID, String> {

    String keyspaceName;
    String tableName;

    static GetObjectFromS3 getObjectFromS3 = new GetObjectFromS3();
    static PutObjectToS3 putObjectToS3 = new PutObjectToS3();

    protected CqlUuidToTextCodec(String keyspaceName, String tableName) {
        super(TypeCodecs.UUID, GenericType.of(String.class));
        System.out.println("Just a test");
        this.keyspaceName = keyspaceName;
        this.tableName = tableName;
    }

    @Nullable
    @Override
    protected String innerToOuter(@Nullable UUID uuid) {
        String result = "";
        try {
            result = getObjectFromS3.getObject("company-keyspaces-large-objects", keyspaceName+"/"+tableName+"/"+uuid.toString()).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Nullable
    @Override
    protected UUID outerToInner(@Nullable String s) {
        UUID uuid = UUID.randomUUID();
        putObjectToS3.putObject(s, "company-keyspaces-large-objects",keyspaceName+"/"+tableName+"/"+uuid.toString());
        return uuid;
    }

}