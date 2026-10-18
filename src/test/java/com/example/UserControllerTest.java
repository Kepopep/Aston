package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ApiExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturnCreatedAndUserDto() throws Exception {
        User user = mockUser(1L, "Ivan", "ivan@example.com", 25);

        when(userService.save("Ivan", "ivan@example.com", 25))
                .thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Ivan",
                                    "email": "ivan@example.com",
                                    "age": 25
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService).save("Ivan", "ivan@example.com", 25);
    }

    @Test
    void findById_shouldReturnUserDto() throws Exception {
        User user = mockUser(1L, "Ivan", "ivan@example.com", 25);

        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService).findById(1L);
    }

    @Test
    void findAll_shouldReturnListOfUserDtos() throws Exception {
        User first = mockUser(
                1L, "Ivan", "ivan@example.com", 25);

        User second = mockUser(
                2L, "Anna", "anna@example.com", 30);

        when(userService.findAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Anna"));

        verify(userService).findAll();
    }

    @Test
    void findAll_whenNoUsers_shouldReturnEmptyList() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void update_shouldReturnUpdatedUserDto() throws Exception {
        User updated = mockUser(
                1L, "Ivan", "ivan@example.com", 26);

        when(userService.update(1L, null, null, 26))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "age": 26
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.age").value(26));

        verify(userService).update(1L, null, null, 26);
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).delete(1L);
    }

    @Test
    void findById_whenUserDoesNotExist_shouldReturnNotFound()
            throws Exception {
        when(userService.findById(42L))
                .thenThrow(new UserNotFoundException(42L));

        mockMvc.perform(get("/api/users/{id}", 42L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void create_whenEmailIsInvalid_shouldReturnBadRequest()
            throws Exception {
        when(userService.save("Ivan", "invalid-email", 25))
                .thenThrow(new IllegalArgumentException(
                        "Некорректный email"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Ivan",
                                    "email": "invalid-email",
                                    "age": 25
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Некорректный email"));
    }

    private User mockUser(
            Long id,
            String name,
            String email,
            Integer age
    ) {
        User user = mock(User.class);

        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(email);
        when(user.getAge()).thenReturn(age);

        return user;
    }
}
