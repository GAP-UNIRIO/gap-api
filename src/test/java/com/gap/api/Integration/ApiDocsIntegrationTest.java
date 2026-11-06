package com.gap.api.Integration;

import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiDocsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IStorageService storageService;

    @Test
    @DisplayName("@spec:AC-141 publica o contrato OpenAPI sem login")
    void apiDocs_withoutLogin_returnsOpenApiContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("GAP API"))
                .andExpect(jsonPath("$.paths['/orders']").exists());
    }

    @Test
    @DisplayName("@spec:AC-142 abre a interface do Swagger em /doc sem login")
    void doc_withoutLogin_redirectsToSwaggerUi() throws Exception {
        mockMvc.perform(get("/doc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("swagger-ui/index.html")));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("@spec:AC-143 endpoints de negócio continuam exigindo login")
    void businessEndpoint_withoutLogin_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
