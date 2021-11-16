package com.sprect.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public interface FileService {
    void saveAvatar(String id, MultipartFile file);

    URL getUrlForDownloadAvatar(String id);

    void upload(String path,
                String fileName,
                Optional<Map<String, String>> optionalMetaData,
                InputStream inputStream);

    byte[] downloadAvatar(String key);

    void deleteAvatar(String id);
}
