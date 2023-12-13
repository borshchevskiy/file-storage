package ru.borshchevskiy.filestorage.integration.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.borshchevskiy.filestorage.integration.IntegrationTestBase;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class RegistrationIntegrationTest extends IntegrationTestBase {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void prepare() {
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
    @DisplayName("Get registration page - status 200 and view 'registration' " +
            "with 'userRequestDto' model param is received")
    public void getRegistration() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("userRequestDto"));
    }


    @Test
    @DisplayName("Perform registration - status 300 'Redirection' " +
            "and 'login' view is received")
    public void register() throws Exception {

        final String email = "test@test.com";
        final String firstname = "firstname";
        final String lastname = "lastname";
        final String password = "password";
        final String passwordConfirmation = "password";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Perform registration with different password and confirmation - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithPasswordAndConfirmationMismatch() throws Exception {

        final String email = "test@test.com";
        final String firstname = "firstname";
        final String lastname = "lastname";
        final String password = "password";
        final String passwordConfirmation = "wordpass";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }

    @Test
    @DisplayName("Perform registration with empty password and confirmation - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithNullPassword() throws Exception {

        final String email = "test@test.com";
        final String firstname = "firstname";
        final String lastname = "lastname";
        final String password = null;
        final String passwordConfirmation = null;

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }

    @Test
    @DisplayName("Perform registration with too short password and confirmation - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithShortPassword() throws Exception {

        final String email = "test@test.com";
        final String firstname = "firstname";
        final String lastname = "lastname";
        final String password = "abc";
        final String passwordConfirmation = "abc";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }

    @Test
    @DisplayName("Perform registration with empty username (email) - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithNullEmail() throws Exception {

        final String email = null;
        final String firstname = "firstname";
        final String lastname = "lastname";
        final String password = "password";
        final String passwordConfirmation = "password";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }

    @Test
    @DisplayName("Perform registration with invalid username (email) - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithInvalidEmail() throws Exception {

        final String email = "email";
        final String firstname = "firstname";
        final String lastname = "lastname";
        final String password = "password";
        final String passwordConfirmation = "password";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }

    @Test
    @DisplayName("Perform registration with empty firstname - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithNullFirstname() throws Exception {

        final String email = "test@test.com";
        final String firstname = null;
        final String lastname = "lastname";
        final String password = "password";
        final String passwordConfirmation = "password";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }

    @Test
    @DisplayName("Perform registration with firstname longer than 255 chars - status 300 'Redirection' " +
            "to 'registration' view and errors param in model")
    public void registerWithLongFirstname() throws Exception {

        final String email = "test@test.com";
        final char[] array = new char[256];
        Arrays.fill(array, 'a');
        final String firstname = new String(array);
        final String lastname = "lastname";
        final String password = "password";
        final String passwordConfirmation = "password";

        mockMvc.perform(post("/registration")
                        .param("email", email)
                        .param("firstname", firstname)
                        .param("lastname", lastname)
                        .param("password", password)
                        .param("passwordConfirmation", passwordConfirmation)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(flash().attributeExists("errors", "userRequestDto"));
    }
}
