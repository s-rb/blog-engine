package main.controller.defaultController;

import main.controller.DefaultController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DefaultControllerTest {

    private static final String INDEX_URL = "/";
    private static final String SOME_WRONG_URL = "/dcsd/sxcsa12";
    private static final String INDEX_HTML = "index.html";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DefaultController controller;
    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    public void testGetIndex() throws Exception {
        this.mockMvc.perform(get(INDEX_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(forwardedUrl(INDEX_HTML));
    }

    @Test
    public void testRedirectToIndex() throws Exception {
        MockMvc mockMvc =
                webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
        mockMvc.perform(get(SOME_WRONG_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("forward:/"))
                .andExpect(forwardedUrl("/"));
    }

    @After
    public void tearDown() throws IOException {
    }
}