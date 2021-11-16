package com.sprect.service.file;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.sprect.service.user.UserService;
import com.sprect.utils.Validator;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {
    private static final String NAME_BUCKET = "sprect";
    private static final String PATH_AVATAR = "sprect/avatar";

    private final UserService userService;
    private final AmazonS3 amazonS3;
    private final Validator validator;

    public FileServiceImpl(UserService userService,
                           AmazonS3 amazonS3,
                           Validator validator) {
        this.userService = userService;
        this.amazonS3 = amazonS3;
        this.validator = validator;
    }

    @Override
    public void saveAvatar(String id, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationFailureException("Cannot upload empty file");
        }
        validator.typeFileAvatar(file);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        userService.saveAvatar(id);

        try {
            upload(PATH_AVATAR, id, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
    }

    @Override
    public URL getUrlForDownloadAvatar(String id) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = Instant.now().toEpochMilli();
        expTimeMillis += 1000 * 60;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(NAME_BUCKET, "avatar/" + id)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    @Override
    public void upload(String path,
                       String fileName,
                       Optional<Map<String, String>> optionalMetaData,
                       InputStream inputStream) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        optionalMetaData.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        try {
            amazonS3.putObject(path, fileName, inputStream, objectMetadata);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to upload the file", e);
        }
    }

    @Override
    public byte[] downloadAvatar(String key) {
        try {
            S3Object object = amazonS3.getObject(PATH_AVATAR, key);
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }
    }

    @Override
    public void deleteAvatar(String id) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(NAME_BUCKET, "avatar/" + id));
            userService.deleteAvatar(id);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to delete the file", e);
        }
    }
}