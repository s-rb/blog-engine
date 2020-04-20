package main.controller.fileController;

import main.controller.FileController;
import main.services.interfaces.FileSystemService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    private FileController fileController;

    private static final String TEMP_FILE_NAME = "temp.jpg";
    private static final String URL_TO_IMAGE_GET_AVATAR_IMAGE_POSTS_PATH = "/posts/images/upload/avatars/";
    private static final String URL_TO_IMAGE_GET_AVATAR_IMAGE = "/images/upload/avatars/";
    private static final String URL_TO_GET_MY_AVATAR_IMAGE = "/my/images/upload/avatars/";
    private static final String URL_TO_GET_IMAGE = "/images/upload/";
    private static final String URL_TO_GET_IMAGE_BY_POST_PATH = "/post/images/upload/";
    private static final String PATH_TO_STORE_AVATARS = "images/upload/avatars";
    private static final String PATH_TO_STORE_IMAGES = "images/upload";
    private static final String PLACEHOLDER_IMAGENAME = "{imageName}";
    private static final String PATH_TO_SOURCE_IMAGE = "src/test/resources/Sample__image.jpg";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAvatarImage_ValidPath() throws Exception {
        executeMockTests(URL_TO_IMAGE_GET_AVATAR_IMAGE, PATH_TO_STORE_AVATARS);
    }

    @Test
    public void testGetAvatarImagePostsPath_ValidPath() throws Exception {
        executeMockTests(URL_TO_IMAGE_GET_AVATAR_IMAGE_POSTS_PATH, PATH_TO_STORE_AVATARS);
    }

    @Test
    public void testGetMyAvatarImage_ValidPath() throws Exception {
        executeMockTests(URL_TO_GET_MY_AVATAR_IMAGE, PATH_TO_STORE_AVATARS);
    }

    @Test
    public void testGetImage_ValidPath() throws Exception {
        executeMockTests(URL_TO_GET_IMAGE, PATH_TO_STORE_IMAGES);
    }

    @Test
    public void testGetImageByPostPath_ValidPath() throws Exception {
        executeMockTests(URL_TO_GET_IMAGE_BY_POST_PATH, PATH_TO_STORE_IMAGES);
    }

    @Test
    public void testGetImage_WrongPath() throws Exception {
        Files.deleteIfExists(Paths.get(PATH_TO_STORE_AVATARS + "/" + TEMP_FILE_NAME));
        // Файл не создаем, соответственно пытаемся получить по валидному адресу отсутствующий файл
        MvcResult result = mockMvc.perform(
                get(URL_TO_GET_IMAGE
                        + PLACEHOLDER_IMAGENAME, TEMP_FILE_NAME)
                        .accept(MediaType.IMAGE_JPEG)).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals("Incorrect Response Status", HttpStatus.NOT_FOUND.value(), status);
    }

    private File createAndGetFileByPath(String path) throws IOException {
        Files.deleteIfExists(Path.of(path));
        return Files.createFile(Path.of(path)).toFile();
    }

    private void createDirectoryByPath(String pathToDir) throws IOException {
        if (!Files.exists(Path.of(pathToDir))) {
            Files.createDirectories(Paths.get(pathToDir));
        }
    }

    private void cleanUpFiles(File... files) throws IOException {
        for (File f : files) {
            Files.deleteIfExists(f.toPath());
        }
    }

    private void executeMockTests(String urlForGetRequest, String pathToStore) throws Exception {
        // Создаем пустой результирующий файл
        createDirectoryByPath(pathToStore);
        File resultImageFile = createAndGetFileByPath(pathToStore + "/" + "result-" + TEMP_FILE_NAME);
        // Временный исходный файл, копируеv его туда, откуда получаем позже по тестовому GET запросу
        File sourceFile = new File(PATH_TO_SOURCE_IMAGE);
        File fileToGet = createAndGetFileByPath(pathToStore + "/" + TEMP_FILE_NAME);
        Files.copy(sourceFile.toPath(), fileToGet.toPath(), StandardCopyOption.REPLACE_EXISTING);
        // Execute
        MvcResult result = mockMvc.perform(
                get(urlForGetRequest
                        + PLACEHOLDER_IMAGENAME, TEMP_FILE_NAME)
                        .accept(MediaType.IMAGE_JPEG)).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals("Incorrect Response Status", HttpStatus.OK.value(), status);

        Files.write(resultImageFile.toPath(), result.getResponse().getContentAsByteArray());

        assertNotNull(resultImageFile);
        Assert.assertArrayEquals("Files are not equal", Files.readAllBytes(sourceFile.toPath()),
                Files.readAllBytes(resultImageFile.toPath()));
        // Clean-up
        cleanUpFiles(fileToGet, resultImageFile);
    }
}
