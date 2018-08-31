package com.bst.pmgr.application;

import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ApplicationTest {

	@Autowired
	private JavaMailSender emailService;

	@LocalServerPort
	private int port;

	@Autowired
	private WebApplicationContext context;

	@Rule
	public GreenMailRule smtpServerRule = new GreenMailRule(ServerSetupTest.ALL);

	private WebClient webClient;

	@Before
	public void setup() {
		this.webClient = MockMvcWebClientBuilder.webAppContextSetup(context, springSecurity()).contextPath("").build();
		webClient.getOptions().setCssEnabled(false);
		//webClient.getOptions().setJavaScriptEnabled(false);
		this.smtpServerRule.start();
	}

	public String url(String path) {
		return "http://localhost:" + port + path;
	}

	@After
	public void cleanup() {
		this.smtpServerRule.stop();
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
	@DisplayName("Can send using email service and receive using greenmail")
	public void sendAndReceiveEmail() throws IOException, MessagingException {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom("automator@brainspeedtech.com");
		message.setTo("test@mail.com");
		message.setSubject("Confirm Registration");
		message.setText("Some text");

		emailService.send(message);

		MimeMessage[] messages = smtpServerRule.getReceivedMessages();

		assert (messages.length == 1);

		for (MimeMessage msg : messages) {
			expect(msg.getContent(), msg.getAllHeaders(), msg.getAllRecipients(), msg.getFrom()).toMatchSnapshot();
		}
	}

	@Test
	@DisplayName("Should send a registration email on registration")
	public void registerAndSendEmail() throws Exception {
		HtmlPage signupPage = webClient.getPage(url("/auth/signup"));
		assert (signupPage).isHtmlPage();

		DomElement element = signupPage.getElementById("user-registration-email");
		signupPage.setFocusedElement(element);
		element.setTextContent("test@mail.com");

		element = signupPage.tabToNextElement();
		element.click();

		element = signupPage.tabToNextElement();
		element.click();

		webClient.waitForBackgroundJavaScript(15000);

		smtpServerRule.waitForIncomingEmail(5000, 1);
		MimeMessage[] messages = smtpServerRule.getReceivedMessages();
		assert (messages.length == 1);
		MimeMessage msg = messages[0];
		expect(msg.getContent(), msg.getAllHeaders(), msg.getAllRecipients(), msg.getFrom(), signupPage.asText())
				.toMatchSnapshot();
	}

}
