package ru.borshchevskiy.filestorage.integration.directories;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.integration.IntegrationTestBase;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.service.DirectoryService;
import ru.borshchevskiy.filestorage.service.FileService;
import ru.borshchevskiy.filestorage.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class DirectoriesIntegrationTest extends IntegrationTestBase {

    private MockMvc mockMvc;
    @Autowired
    private MockHttpSession session;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private UserService userService;
    @Autowired
    private DirectoryService directoryService;
    @Autowired
    private FileService fileService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MinioRepository minioRepository;
    private static final String USERNAME = "test@test.com";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_CONFIRMATION = "password";

    @BeforeEach
    public void prepareContext() throws Exception {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail(USERNAME);
        requestDto.setFirstname(FIRSTNAME);
        requestDto.setLastname(LASTNAME);
        requestDto.setPassword(PASSWORD);
        requestDto.setPasswordConfirmation(PASSWORD_CONFIRMATION);

        userService.create(requestDto);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        //         Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @AfterEach
    public void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE file_storage.file_storage.users");
        jdbcTemplate.execute("TRUNCATE TABLE file_storage.file_storage.roles");
        jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART");
        jdbcTemplate.execute("ALTER SEQUENCE roles_user_id_seq RESTART");
        minioRepository.deleteDirectory("");
    }

    @Test
    @DisplayName("Test new empty directory creation - new empty dir is created")
    public void createNewDir() throws Exception {
        final String path = "";
        final String newDirectoryName = "new dir";

        mockMvc.perform(post("/directories/create")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("newDirectoryName", newDirectoryName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/updateFilesList*"))
                .andExpect(model().attribute("path", path));

        List<FileItemDto> filesByPath = fileService.getItemsByPath(path);

        assertThatList(filesByPath).hasSize(1);
        assertThat(filesByPath.get(0).isDirectory()).isTrue();
        assertThat(filesByPath.get(0).getName()).isEqualTo(newDirectoryName + "/");
    }

    @Test
    @DisplayName("Test directory rename - expect directory has new name")
    public void renameDirectory() throws Exception {
        final String path = "";
        final String oldDirName = "dir";
        final String newDirName = "newDirName";

        // Create initial directory
        directoryService.createDirectory(path, oldDirName);

        //Assert that directory is created with required name
        List<FileItemDto> filesByPath = fileService.getItemsByPath(path);
        assertThatList(filesByPath).hasSize(1);
        assertThat(filesByPath.get(0).isDirectory()).isTrue();
        assertThat(filesByPath.get(0).getName()).isEqualTo(oldDirName + "/");

        mockMvc.perform(post("/directories/rename")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("oldName", oldDirName + "/")
                        .param("newName", newDirName))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        List<FileItemDto> files = fileService.getItemsByPath(path);

        assertThat(files.size()).isEqualTo(1);
        assertThat(filesByPath.get(0).isDirectory()).isTrue();
        assertThat(filesByPath.get(0).getName()).isEqualTo(oldDirName + "/");
    }

    @Test
    @DisplayName("Test directory delete " +
            "- expect directory is deleted and parent directory is still present despite it is empty")
    public void deleteAllDirectoriesInParentDirectory() throws Exception {
        final String path = "parentDir/";
        final String dirName = "dir";

        // Create initial directory
        directoryService.createDirectory(path, dirName);

        //Assert that directory is created with required name
        List<FileItemDto> itemsByPath = fileService.getItemsByPath(path);
        assertThatList(itemsByPath).hasSize(1);
        assertThat(itemsByPath.get(0).isDirectory()).isTrue();
        assertThat(itemsByPath.get(0).getName()).isEqualTo(dirName + "/");

        mockMvc.perform(post("/directories/delete")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("name", dirName + "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        //Check that no directory is found
        List<FileItemDto> items = fileService.getItemsByPath(path);
        assertThatList(items).isEmpty();

        //Check that parent directory is still present
        List<FileItemDto> parentItems = fileService.getItemsByPath("");
        assertThatList(parentItems).hasSize(1);
        assertEquals(path, parentItems.get(0).getName());
    }

    @Test
    @DisplayName("Test directory delete - expect directory is deleted and parent directory with other items is present")
    public void deleteSomeDirectoriesInParentDirectory() throws Exception {
        final String path = "parentDir/";
        final String dirName = "dir";
        final String anotherDirName = "anotherDir";

        // Create directories in path
        directoryService.createDirectory(path, dirName);
        directoryService.createDirectory(path, anotherDirName);

        //Assert that directory is created with required name
        List<FileItemDto> itemsByPath = fileService.getItemsByPath(path);
        assertThatList(itemsByPath).hasSize(2);

        mockMvc.perform(post("/directories/delete")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("name", dirName + "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        //Check that no directory is found
        List<FileItemDto> items = fileService.getItemsByPath(path);
        assertThatList(items).hasSize(1);
        assertEquals(anotherDirName + "/", items.get(0).getName());

        //Check that parent directory is still present
        List<FileItemDto> parentItems = fileService.getItemsByPath("");
        assertThatList(parentItems).hasSize(1);
        assertEquals(path, parentItems.get(0).getName());
    }

    @Test
    @DisplayName("Test directory delete in root - expect directory is deleted")
    public void deleteAllDirectoryInRoot() throws Exception {
        final String path = "";
        final String dirName = "dir";

        // Create directories in path
        directoryService.createDirectory(path, dirName);

        //Assert that directory is created with required name
        List<FileItemDto> itemsByPath = fileService.getItemsByPath(path);
        assertThatList(itemsByPath).hasSize(1);

        mockMvc.perform(post("/directories/delete")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("name", dirName + "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        //Check that no directory is found
        List<FileItemDto> items = fileService.getItemsByPath(path);
        assertThatList(items).hasSize(0);
    }

    @Test
    @DisplayName("Test directory delete in root - expect directory is deleted")
    public void deleteOneDirectoryInRoot() throws Exception {
        final String path = "";
        final String dirName = "dir";
        final String anotherDirName = "anotherDir";

        // Create directories in path
        directoryService.createDirectory(path, dirName);
        directoryService.createDirectory(path, anotherDirName);

        //Assert that directory is created with required name
        List<FileItemDto> itemsByPath = fileService.getItemsByPath(path);
        assertThatList(itemsByPath).hasSize(2);

        mockMvc.perform(post("/directories/delete")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("name", dirName + "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        //Check that no directory is found
        List<FileItemDto> items = fileService.getItemsByPath(path);
        assertThatList(items).hasSize(1);
        assertEquals(anotherDirName + "/", items.get(0).getName());
    }
}

