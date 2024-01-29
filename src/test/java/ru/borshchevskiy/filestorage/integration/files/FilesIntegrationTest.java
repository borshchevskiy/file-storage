package ru.borshchevskiy.filestorage.integration.files;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
import ru.borshchevskiy.filestorage.service.FileService;
import ru.borshchevskiy.filestorage.service.UserService;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class FilesIntegrationTest extends IntegrationTestBase {

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
    @DisplayName(value = "Test file download - expect file is saved")
    public void download() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        final String path = "";
        final String fileName = "file.txt";
        final String fileData = "fileData";

        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), path, fileName);

        MvcResult mvcResult = mockMvc.perform(get("/files/download")
                        .session(session)
                        .param("path", path)
                        .param("file", fileName))
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileData.getBytes()));
    }

    @Test
    @DisplayName("Test file upload with wrong Content-Type header - expect error 400")
    public void uploadNotMultipart() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        final String requestPath = "";
        final String boundary = "q1w2e3r4t5y6u7i8o9";
        final String fileName = "file.txt";
        final String contentType = MediaType.TEXT_PLAIN_VALUE;

        final String start = "--" + boundary +
                "\r\n Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";
        final String end = "\r\n--" + boundary + "--";
        final String fileData = "fileData";

        final byte[] body = (start + fileData + end).getBytes();

        mockMvc.perform(post("/files/upload")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf().asHeader())
                        .content(body)
                        .session(session)
                        .queryParam("path", requestPath))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("errors/error400"));
    }

    @Test
    @DisplayName("Test file upload without file - expect nothing is saved")
    public void uploadNothing() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        final String requestPath = "";
        final String responsePath = "";
        final String boundary = "q1w2e3r4t5y6u7i8o9";

        final byte[] body = new byte[0];

        mockMvc.perform(post("/files/upload")
                        .contentType("multipart/form-data; boundary=" + boundary)
                        .with(csrf().asHeader())
                        .content(body)
                        .session(session)
                        .queryParam("path", requestPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", responsePath))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        List<FileItemDto> files = fileService.getItemsByPath(requestPath);
        assertThatList(files).isEmpty();
    }

    @Test
    @DisplayName("Test file upload - expect file is uploaded")
    public void upload() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        final String requestPath = "";
        final String responsePath = "";
        final String boundary = "q1w2e3r4t5y6u7i8o9";
        final String fileName = "file.txt";
        final String contentType = MediaType.TEXT_PLAIN_VALUE;

        final String start = "--" + boundary +
                "\r\n Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-type: " + contentType + "\r\n\r\n";
        final String end = "\r\n--" + boundary + "--";
        final String fileData = "fileData";

        final byte[] body = (start + fileData + end).getBytes();

        mockMvc.perform(post("/files/upload")
                        .contentType("multipart/form-data; boundary=" + boundary)
                        .with(csrf().asHeader())
                        .content(body)
                        .session(session)
                        .queryParam("path", requestPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", responsePath))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        List<FileItemDto> files = fileService.getItemsByPath(requestPath);

        assertThat(files.size()).isEqualTo(1);
        assertThat(files.get(0).getName()).isEqualTo(fileName);
        assertThat(files.get(0).getSize()).isEqualTo(fileData.getBytes().length);
    }


    @Test
    @DisplayName("Test file rename - expect file has new name")
    public void renameFile() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        final String path = "";
        final String fileName = "file.txt";
        final String fileData = "fileData";
        final String newFileName = "newFileName";
        final String extension = ".txt";

        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), path, fileName);

        mockMvc.perform(post("/files/rename")
                        .session(session)
                        .with(csrf())
                        .param("path", path)
                        .param("oldName", fileName)
                        .param("newName", newFileName))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        final List<FileItemDto> files = fileService.getItemsByPath(path);

        assertThat(files.size()).isEqualTo(1);
        assertThat(files.get(0).getName()).isEqualTo(newFileName + extension);
        assertThat(files.get(0).getSize()).isEqualTo(fileData.getBytes().length);
    }

    @Test
    @DisplayName("Test file delete - expect file is deleted and parent directory is present, despite it is empty")
    public void deleteFile() throws Exception {
        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Save the test file to storage with parent directory described in "path"
        final String path = "dir/";
        final String fileName = "file.txt";
        final String fileData = "fileData";
        fileService.uploadFile(new ByteArrayInputStream(fileData.getBytes()), path, fileName);

        mockMvc.perform(post("/files/delete")
                        .session(session)
                        .with(csrf())
                        .param("path", path )
                        .param("name", fileName))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute("path", path))
                .andExpect(redirectedUrlPattern("/updateFilesList*"));

        // Check that file is absent, but its parent directory is present
        List<FileItemDto> files = fileService.getItemsByPath(path);
        List<FileItemDto> parentDirectory = fileService.getItemsByPath("");

        assertThatList(files).isEmpty();
        assertThatList(parentDirectory).hasSize(1);
        assertTrue(parentDirectory.get(0).isDirectory());
        assertEquals(path, parentDirectory.get(0).getName());
    }
}

