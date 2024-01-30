package ru.borshchevskiy.filestorage.integration.files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.integration.IntegrationTestBase;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.service.FileService;
import ru.borshchevskiy.filestorage.service.UserService;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class MainPageIntegrationTest extends IntegrationTestBase {

    private MockMvc mockMvc;
    @Autowired
    private MockHttpSession session;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MinioRepository minioRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    private static final String USERNAME = "test@test.com";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_CONFIRMATION = "password";

    @BeforeEach
    public void prepareContext() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Creating test user
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail(USERNAME);
        requestDto.setFirstname(FIRSTNAME);
        requestDto.setLastname(LASTNAME);
        requestDto.setPassword(PASSWORD);
        requestDto.setPasswordConfirmation(PASSWORD_CONFIRMATION);
        userService.create(requestDto);

        // Perform login to trigger creation of session scoped bean userSessionData
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
    public void getMainPage() throws Exception {
        final String path = "";

        // Create and save mock files
        MockMultipartFile multipartFile1 = new MockMultipartFile("file1", "file1.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE, "file1".getBytes());
        MockMultipartFile multipartFile2 = new MockMultipartFile("file2", "file2.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE, "file2".getBytes());
        MockMultipartFile multipartFile3 = new MockMultipartFile("file3", "file3.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE, "file3".getBytes());

        fileService.uploadFile(multipartFile1.getInputStream(), path, multipartFile1.getOriginalFilename());
        fileService.uploadFile(multipartFile2.getInputStream(), path, multipartFile2.getOriginalFilename());
        fileService.uploadFile(multipartFile3.getInputStream(), path, multipartFile3.getOriginalFilename());

        mockMvc.perform(get("/")
                        .session(session)
                        .param("path", path))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("path"))
                .andExpect(model().attributeExists("breadcrumbs"))
                .andExpect(model().attribute("filesList", hasSize(3)))
                .andExpect(model().attributeDoesNotExist("parentPath"));
    }

    @Test
    public void updateFileList() throws Exception {
        final String path = "";

        // Create and save mock files
        MockMultipartFile multipartFile1 = new MockMultipartFile("file1", "file1.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE, "file1".getBytes());
        MockMultipartFile multipartFile2 = new MockMultipartFile("file2", "file2.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE, "file2".getBytes());
        MockMultipartFile multipartFile3 = new MockMultipartFile("file3", "file3.txt",
                MediaType.MULTIPART_FORM_DATA_VALUE, "file3".getBytes());

        fileService.uploadFile(multipartFile1.getInputStream(), path, multipartFile1.getOriginalFilename());
        fileService.uploadFile(multipartFile2.getInputStream(), path, multipartFile2.getOriginalFilename());
        fileService.uploadFile(multipartFile3.getInputStream(), path, multipartFile3.getOriginalFilename());

        mockMvc.perform(get("/updateFilesList")
                        .session(session)
                        .param("path", path))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("fragments/main/fragment-filesList :: filesList"))
                .andExpect(model().attributeExists("path"))
                .andExpect(model().attribute("filesList", hasSize(3)))
                .andExpect(model().attributeDoesNotExist("parentPath"));
    }
}
