package ru.borshchevskiy.filestorage.integration.search;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.integration.IntegrationTestBase;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.service.DirectoryService;
import ru.borshchevskiy.filestorage.service.FileService;
import ru.borshchevskiy.filestorage.service.UserService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class SearchIntegrationTest extends IntegrationTestBase {

    private MockMvc mockMvc;
    @Autowired
    private MockHttpSession session;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DirectoryService directoryService;
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
    public void prepareContext() {
        // Create a user
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail(USERNAME);
        requestDto.setFirstname(FIRSTNAME);
        requestDto.setLastname(LASTNAME);
        requestDto.setPassword(PASSWORD);
        requestDto.setPasswordConfirmation(PASSWORD_CONFIRMATION);

        userService.create(requestDto);

        // Configure MockMvc with web context and enable security
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
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
    public void search() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        String query = "1";

        String rootPath = "";
        String pathToFind = "dir1/";
        String innerPathToFind = "innerDir1/";
        String anotherPathToFind = "dir11/";
        String pathNotToBeFound = "dir2/";

        String fileNameToFind = "file1.txt";
        String anotherFileNameToFind = "file11.txt";
        String filenameNotToBeFound = "file.txt";
        String fileData = "fileData";

        /*
        Create directories:
        user-{id}-files/dir1/
        user-{id}-files/dir1/innerDir1
        user-{id}-files/dir11/
        user-{id}-files/dir2/
         */
        directoryService.createDirectory(rootPath, pathToFind);
        directoryService.createDirectory(pathToFind, innerPathToFind);
        directoryService.createDirectory(rootPath, anotherPathToFind);
        directoryService.createDirectory(rootPath, pathNotToBeFound);

        // This directories should be found with search request '1'
        List<String> directoriesToBeFound = List.of(
                rootPath + pathToFind,
                pathToFind + innerPathToFind,
                rootPath + anotherPathToFind);

        /*
        Create files to be found with search request '1'
        user-{id}-files/file1.txt
        user-{id}-files/file11.txt
        user-{id}-files/dir2/file1.txt
        user-{id}-files/dir1/file1.txt
         */
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), rootPath, fileNameToFind);
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), rootPath, anotherFileNameToFind);
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), pathNotToBeFound, fileNameToFind);
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), pathToFind, fileNameToFind);

        /*
        Create files not to be found with search request '1'
        user-{id}-files/file.txt
        user-{id}-files/dir11/file.txt
         */
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), rootPath, filenameNotToBeFound);
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), anotherPathToFind, filenameNotToBeFound);

        // This files should be found with search request '1'
        List<String> filesToBeFound = List.of(
                rootPath + fileNameToFind,
                rootPath + anotherFileNameToFind,
                pathNotToBeFound + fileNameToFind,
                pathToFind + fileNameToFind);

        List<String> expectedSearchResults = new ArrayList<>();
        expectedSearchResults.addAll(directoriesToBeFound);
        expectedSearchResults.addAll(filesToBeFound);

        MvcResult mvcResult = mockMvc.perform(get("/search")
                        .session(session)
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andReturn();

        List<String> actualSearchResults = ((List<FileItemDto>) mvcResult.getModelAndView().getModel().get("results"))
                .stream()
                .map(FileItemDto::getFullName)
                .toList();

        assertThatList(actualSearchResults).containsAll(expectedSearchResults);
    }
}
