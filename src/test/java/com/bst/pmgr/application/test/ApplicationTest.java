package com.bst.pmgr.application.test;

import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bst.utility.test.lib.SeleniumTest;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SeleniumTest(driver = ChromeDriver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ApplicationTest {

	@Autowired
	private WebDriver driver;

	@Autowired
	private JavaMailSender emailService;

	@LocalServerPort
	private int port;

	@Autowired
	private WebApplicationContext context;

	@Rule
	public GreenMailRule smtpServerRule = new GreenMailRule(ServerSetupTest.ALL);

	@Before
	public void setup() {
		MockMvcBuilders.webAppContextSetup(context).build();
		this.smtpServerRule.start();
	}

	public String url(String path) {
		return "http://localhost:" + port + path;
	}

	@After
	public void cleanup() {
		this.smtpServerRule.stop();
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
	public void registerAndSendEmail() throws Exception {
		
		// Signup
		driver.get(url("/auth/signup"));
		driver.findElement(By.id("user-registration-email")).sendKeys("test@mail.com");
		driver.switchTo().frame(0);
		driver.findElement(
				By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='reCAPTCHA'])[1]/preceding::div[4]"))
				.click();
		
		Thread.sleep(5000);
		
		driver.switchTo().parentFrame();
		driver.findElement(By.id("signup-button")).click();

		// Check email
		smtpServerRule.waitForIncomingEmail(5000, 1);
		MimeMessage[] messages = smtpServerRule.getReceivedMessages();
		assert (messages.length == 1);
		
		MimeMessage msg = messages[0];

		String registrationMessage = msg.getContent().toString();

	    Pattern p = Pattern.compile("(http://.*)");   // the pattern to search for
	    Matcher m = p.matcher(registrationMessage);

	    assert(m.find());
	    
	    String registrationLink = m.group(1);
	    driver.get(registrationLink);
	    
		expect(msg.getAllHeaders(), msg.getAllRecipients(), msg.getFrom())
			.toMatchSnapshot();

	}

}
