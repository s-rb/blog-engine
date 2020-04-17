package main.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public interface FileSystemService {

    Boolean deleteFileByPath(String pathToFile);

    Boolean createDirectoriesByPath(String path);

    void copyMultiPartFileToPath(MultipartFile source, Path dest);

    File getFileByPath(String pathToFile) throws FileNotFoundException;
}
