
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class PutObjectToS3 {

    static AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(Regions.US_EAST_1)
            .build();

    void putObject(String obj, String bucketName, String stringObjKeyName) {
        s3Client.putObject(bucketName, stringObjKeyName, obj);
    }
}
