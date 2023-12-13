package ru.borshchevskiy.filestorage.integration.profile;

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
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;
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
public class UserIntegrationTest extends IntegrationTestBase {

    private MockMvc mockMvc;
    @Autowired
    private MockHttpSession session;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;
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
    }

    @Test
    @DisplayName("Get profile page - response status is 200 and 'profile' page is received")
    public void getProfile() throws Exception {
        // Perform login
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/profile")
                        .session(session))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("Update profile - response status is 300 and redirection to 'profile' page is received " +
            " and session scoped bean 'userSessionData' is updated")
    public void updateProfile() throws Exception {
        final String newFirstname = "newFirstname";
        final String newLastname = "newLastname";

        UserResponseDto expectedUserParam = userService.findByEmail(USERNAME);
        expectedUserParam.setFirstname(newFirstname);
        expectedUserParam.setLastname(newLastname);

        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Perform profile update
        mockMvc.perform(post("/profile/update")
                        .session(session)
                        .with(csrf())
                        .param("firstname", newFirstname)
                        .param("lastname", newLastname)
                        .param("email", USERNAME))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("user", expectedUserParam))
                .andExpect(flash().attribute("isProfileUpdated", true));

        // Verify that session scoped bean is updated during profile update
        assertThat(((UserSessionData) session.getAttribute("scopedTarget.userSessionData"))
                .getFirstname()).isEqualTo(newFirstname);
        assertThat(((UserSessionData) session.getAttribute("scopedTarget.userSessionData"))
                .getLastname()).isEqualTo(newLastname);
    }

    @Test
    @DisplayName("Update profile failure (firstname is null) - response status is 300 and redirection to 'profile' page is received " +
            "and session scoped bean 'userSessionData' is not updated")
    public void updateProfileWithError() throws Exception {
        final String newFirstname = null;
        final String newLastname = "newLastname";

        final UserRequestDto expectedUserParam = new UserRequestDto();
        expectedUserParam.setFirstname(newFirstname);
        expectedUserParam.setLastname(newLastname);
        expectedUserParam.setEmail(USERNAME);

        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Perform profile update and expect error param
        mockMvc.perform(post("/profile/update")
                        .session(session)
                        .param("firstname", newFirstname)
                        .param("lastname", newLastname)
                        .param("email", USERNAME)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("user", expectedUserParam))
                .andExpect(flash().attributeExists("errors"));

        // Verify that session scoped bean is not updated during profile update
        assertThat(((UserSessionData) session.getAttribute("scopedTarget.userSessionData"))
                .getFirstname()).isEqualTo(FIRSTNAME);
        assertThat(((UserSessionData) session.getAttribute("scopedTarget.userSessionData"))
                .getLastname()).isEqualTo(LASTNAME);
    }

    @Test
    @DisplayName("Update password - response status is 300 and redirection to 'profile' page is received")
    public void updatePassword() throws Exception {

        final String newPassword = "newPassword";

        UserResponseDto expectedUserParam = userService.findByEmail(USERNAME);
        expectedUserParam.setFirstname(FIRSTNAME);
        expectedUserParam.setLastname(LASTNAME);

        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Perform password update
        mockMvc.perform(post("/profile/update/password")
                        .session(session)
                        .param("password", newPassword)
                        .param("passwordConfirmation", newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("user", expectedUserParam))
                .andExpect(flash().attribute("isPasswordUpdated", true));
    }

    @Test
    @DisplayName("Update password failure (password too short - response status is 300 and redirection to 'profile' " +
            "page is received and 'errors' param is present in the model")
    public void updatePasswordFailure() throws Exception {

        final String newPassword = "new";

        final UserRequestDto expectedUserParam = new UserRequestDto();
        expectedUserParam.setPassword(newPassword);
        expectedUserParam.setPasswordConfirmation(newPassword);

        // Perform login to trigger creation of session scoped bean userSessionData
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .session(session)
                        .param("email", USERNAME)
                        .param("password", PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Perform password update
        mockMvc.perform(post("/profile/update/password")
                        .session(session)
                        .param("password", newPassword)
                        .param("passwordConfirmation", newPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("user", expectedUserParam))
                .andExpect(flash().attributeExists("errors"));
    }
}
