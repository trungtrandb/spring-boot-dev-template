package site.code4fun.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import site.code4fun.constant.AppEndpoints;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class SecuredControllerSpringBootIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
          .webAppContextSetup(context)
          .apply(springSecurity())
          .build();
    }

    @Test
    void anyWhenUnauthenticatedThenUnauthorized() throws Exception {
        this.mvc.perform(get(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.PRODUCTS_ENDPOINT))
                .andExpect(status().isFound());
    }

    @WithMockUser
    @Test
    void endpointWhenNotUserAuthorityThenForbidden() throws Exception {
        this.mvc.perform(get(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.PRODUCTS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(authorities="ROLE_ADMIN")
    @Test
    void endpointWhenUserAuthorityThenAuthorized() throws Exception {
        this.mvc.perform(get(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.PRODUCTS_ENDPOINT))
                .andExpect(status().isOk());
    }
}