package ru.borshchevskiy.filestorage.integration.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.integration.IntegrationTestBase;
import ru.borshchevskiy.filestorage.service.UserService;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class LoginIntegrationTest extends IntegrationTestBase {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockHttpSession session;
    @Autowired
    private UserService userService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final String USERNAME = "test@test.com";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_CONFIRMATION = "password";

    @BeforeEach
    public void prepare() {
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
    }

    @Test
    @DisplayName("Get login page")
    public void getLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Perform login - login successful and get redirect to main page")
    public void performLogin() throws Exception {

        // Ensure that session scoped bean is null
        assertThat(session.getAttribute("scopedTarget.userSessionData")).isNull();

        // Perform login
        mockMvc.perform(post("/login")
                        .session(session)
                        .with(csrf())
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verify that session scoped bean is present and populated with expected data
        assertThat(((UserSessionData) session.getAttribute("scopedTarget.userSessionData"))
                .getFirstname()).isEqualTo(FIRSTNAME);
        assertThat(((UserSessionData) session.getAttribute("scopedTarget.userSessionData"))
                .getLastname()).isEqualTo(LASTNAME);
    }

    @Test
    @DisplayName("Fail login - redirect to login page")
    public void failLogin() throws Exception {
        final String invalidUsername = "invalidUsername";

        // Ensure that session scoped bean is null
        assertThat(session.getAttribute("scopedTarget.userSessionData")).isNull();

        // Fail login due to invalid username
        mockMvc.perform(post("/login")
                        .session(session)
                        .with(csrf())
                        .param("email", invalidUsername)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login*"));

        // Ensure that session scoped bean was not created
        assertThat(session.getAttribute("scopedTarget.userSessionData")).isNull();
    }
}
