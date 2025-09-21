package org.clean.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clean.system.controller.UserController;
import org.clean.system.entity.User;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserStatus;
import org.clean.system.enums.UserType;
import org.clean.system.param.UserAddParam;
import org.clean.system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testGetById() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("test_user");
        when(userService.getById(1L)).thenReturn(mockUser);

        mockMvc.perform(get("/user/getById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1))
                .andExpect(jsonPath("name").value("test_user"));

        assertThat(userController.getById(1L)).isEqualTo(mockUser);
    }

    @Test
    void testAdd() throws Exception {
        UserAddParam param = new UserAddParam();
        param.setName("new_user");
        param.setAge(25);
        param.setEmail("new_user@example.com");
        param.setUserType(UserType.USER);
        param.setSex(SexEnum.FEMALE);


        String jsonContent = objectMapper.writeValueAsString(param);

        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("new_user"))
                .andExpect(jsonPath("age").value(25))
                .andExpect(jsonPath("email").value("new_user@example.com"));
    }

    @Test
    void testAddWithInvalidData() throws Exception {
        UserAddParam invalidParam = new UserAddParam();
        // 不设置必填字段 name

        String jsonContent = objectMapper.writeValueAsString(invalidParam);

        mockMvc.perform(post("/user/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddList() throws Exception {
        when(userService.addList(1)).thenReturn(true);

        mockMvc.perform(post("/user/addList")
                .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertThat(userController.addList(5)).isTrue();
    }

    @Test
    void testAddListWithInvalidCount() throws Exception {
        mockMvc.perform(post("/user/addList")
                .param("count", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testEdit() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("updated_user");
        when(userService.edit(any(User.class))).thenReturn(1);

        String jsonContent = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/user/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        assertThat(userController.edit(user)).isEqualTo(1);
    }

    @Test
    void testEdit2() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setName("another_user");
        when(userService.edit(any(User.class))).thenReturn(1);

        mockMvc.perform(post("/user/edit2")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "2")
                .param("name", "another_user"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

//        assertThat(userController.edit2(user)).isEqualTo(1);
    }

    @Test
    void testEditLambda() throws Exception {
        when(userService.editLambda(3L, "lambda_user")).thenReturn(true);

        mockMvc.perform(post("/user/editLambda")
                .param("name", "lambda_user")
                .param("userId", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        assertThat(userController.editLambda("lambda_user", 3L)).isTrue();
    }

    @Test
    void testEditLambdaWithInvalidParams() throws Exception {
        mockMvc.perform(post("/user/editLambda")
                .param("name", "")
                .param("userId", "invalid"))
                .andExpect(status().isBadRequest());
    }
}