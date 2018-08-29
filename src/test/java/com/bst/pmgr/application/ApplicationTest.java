package com.bst.pmgr.application;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.bst.pmgr.mail.SmtpServerRule;
import com.bst.user.registration.components.EmailService;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ApplicationTest {
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private WebApplicationContext context;
	
    @Autowired
    private EmailService emailService;
    
    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);
	
	private WebClient webClient;

	@Before
	public void setup() {
	    this.webClient = MockMvcWebClientBuilder
	        .webAppContextSetup(context, springSecurity())
	        .contextPath("").build();
	}
	
	public String url(String path) {
		return "http://localhost:" + port + path;
	}
	
	@After
	public void cleanup() {
		this.webClient.close();
	}
	
	@BeforeClass
	public static void startSnapshot() {
		start();
	}
	
	@AfterClass
	public static void stopSnapshot() {
		validateSnapshots();
	}
    
    @Test
    @DisplayName("Check index page returns without auth")
    public void checkIndexWithNoAuth() throws Exception {
    	HtmlPage indexPage = webClient.getPage(url("/index"));
    	assert(indexPage).isHtmlPage();
    }
    
    @Test
    @DisplayName("Check registration page can be accessed without auth")
    public void checkRegistrationPage() throws Exception {
    	HtmlPage signupPage = webClient.getPage(url("/auth-signup"));
    	assert(signupPage).isHtmlPage();
    }

}
