package main.services;

import lombok.extern.slf4j.Slf4j;
import main.services.interfaces.FileSystemService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class FileSystemServiceImpl implements FileSystemService {

    @Override
    public Boolean deleteFileByPath(String pathToFile) {
        if (pathToFile != null && !pathToFile.isBlank()) {
            try {
                boolean isDeleteSucceed = Files.deleteIfExists(Path.of(pathToFile));
                log.info("--- Успшено удален файл по пути: " + pathToFile);
                return isDeleteSucceed;
            } catch (IOException e) {
                e.printStackTrace();
                log.error("--- Не удалось удалить файл по пути: " + pathToFile, e);
                return false;
            }
        }
        log.warn("--- Не удалось удалить файл по пути: " + pathToFile);
        return false;
    }

    @Override
    public Boolean createDirectoriesByPath(String path) {
        try {
            Files.createDirectories(Path.of(path));
            log.info("--- Папка успешно создана по пути: " + path);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("--- Папка НЕ создана по пути: " + path, e);
            return false;
        }
    }

    @Override
    public void copyMultiPartFileToPath(MultipartFile source, Path dest) {
        try {
            source.transferTo(dest);
            log.info("--- Успешно скопированы данные файла: sourceFile:{" +
                    "FileName:" + source.getOriginalFilename() + "," +
                    "FileSize:" + source.getSize() + "}" +
                    "в файл: destFile:{" +
                    "FileName:" + dest.toString() + "}"
            );
        } catch (IOException e) {
            e.printStackTrace();
            log.error("--- Не удалось скопировать данные файла: sourceFile:{" +
                    "FileName:" + source.getOriginalFilename() + "," +
                    "FileSize:" + source.getSize() + "}" +
                    "в файл: destFile:{" +
                    "FileName:" + dest.toString() + "}", e
            );
        }
    }

    @Override
    public File getFileByPath(String pathToFile) throws FileNotFoundException {
        if (Files.exists(Path.of(pathToFile))) {
            File file = new File(pathToFile);
            log.info("--- Получен файл по пути: {" +
                    pathToFile + "}"
            );
            return file;
        } else {
            log.error("--- Не удалось найти файл: " + pathToFile);
            throw new FileNotFoundException("По указанному пути отсутствует файл");
        }
    }
}
