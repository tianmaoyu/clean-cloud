package org.clean.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clean.system.controller.TEnumController;
import org.clean.system.enums.SexEnum;
import org.clean.system.enums.UserStatus;
import org.clean.system.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Transactional
@SpringBootTest
public class TEnumControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private TEnumController tEnumController;

    @Autowired
    private ObjectMapper objectMapper ;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(tEnumController).build();
    }

    @Test
    void testGetUserType() throws Exception {
        mockMvc.perform(get("/TEnum/getUserType")
                .param("userType", "USER"))
                .andExpect(status().isOk());


        assertThat(tEnumController.getUserType(UserType.USER)).isEqualTo(UserType.USER);
    }

    @Test
    void testGetUserTypeList() throws Exception {
        mockMvc.perform(get("/TEnum/getUserTypeList")
                .param("userType", "ADMIN"))
                .andExpect(status().isOk());


        List<UserType> result = tEnumController.getUserTypeList(UserType.ADMIN);
        assertThat(result).containsExactly(UserType.USER, UserType.ADMIN);
    }

    @Test
    void testGetSexEnum() throws Exception {
        mockMvc.perform(get("/TEnum/getSexEnum")
                .param("sexEnum", "MALE"))
                .andExpect(status().isOk());

        assertThat(tEnumController.getSexEnum(SexEnum.MALE)).isEqualTo(SexEnum.MALE);
    }

    @Test
    void testGetUserStatus() throws Exception {
        mockMvc.perform(get("/TEnum/getUserStatus")
                .param("userStatus", "ENABLED"))
                .andExpect(status().isOk());


        assertThat(tEnumController.getUserStatus(UserStatus.ENABLED)).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void testGetId() throws Exception {
        mockMvc.perform(get("/TEnum/getId")
                .param("id", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("123"));

        assertThat(tEnumController.getId(123)).isEqualTo(123);
    }

    @Test
    void testGetUserStatusList() throws Exception {
        mockMvc.perform(get("/TEnum/getUserStatusList")
                .param("userStatus", "ENABLED"))
                .andExpect(status().isOk());


        List<UserStatus> result = tEnumController.getUserStatusList(UserStatus.ENABLED);
        assertThat(result).containsExactly(
            UserStatus.ENABLED,
            UserStatus.ENABLED,
            UserStatus.DISABLED,
            UserStatus.LOCKED
        );
    }

    @Test
    void testAddUserStatusList() throws Exception {
        String jsonContent = objectMapper.writeValueAsString(List.of("enabled", "disabled"));
        
        mockMvc.perform(post("/TEnum/addUserStatusList")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk());

    }

    @Test
    void testAddUserStatusListWithInvalidData() throws Exception {
        String invalidJsonContent = "{\"invalid\":\"data\"}";
        
        mockMvc.perform(post("/TEnum/addUserStatusList")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonContent))
                .andExpect(status().isBadRequest());
    }
}