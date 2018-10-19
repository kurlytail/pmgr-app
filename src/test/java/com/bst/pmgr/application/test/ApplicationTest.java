package com.bst.pmgr.application.test;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bst.utility.testlib.SeleniumTest;
import com.bst.utility.testlib.SeleniumTestExecutionListener;
import com.bst.utility.testlib.SnapshotListener;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

@ExtendWith(SpringExtension.class)
@SeleniumTest(driver = ChromeDriver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(listeners = { SeleniumTestExecutionListener.class,
		SnapshotListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
public class ApplicationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private WebDriver driver;

	@Autowired
	private JavaMailSender emailService;

	@LocalServerPort
	private int port;

	public GreenMail smtpServer;

	@AfterEach
	public void cleanup() {
		this.smtpServer.stop();
	}

	@BeforeEach
	public void setup() {
		this.smtpServer = new GreenMail(ServerSetupTest.ALL);
		this.smtpServer.start();
		MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	public void testRegisterAndSendEmail() throws Exception {
		this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		final WebDriverWait wait = new WebDriverWait(this.driver, 10);

		// Signup
		this.driver.get(this.url("/auth/signup"));
		this.driver.findElement(By.id("user-registration-email")).sendKeys("test@mail.com");
		this.driver.switchTo().frame(0);
		this.driver
				.findElement(By.xpath(
						"(.//*[normalize-space(text()) and normalize-space(.)='reCAPTCHA'])[1]/preceding::div[4]"))
				.click();
		wait.withTimeout(5, TimeUnit.SECONDS);
		this.driver.switchTo().parentFrame();
		wait.until(ExpectedConditions.elementToBeClickable(By.id("signup-button"))).click();

		// Check email
		this.smtpServer.waitForIncomingEmail(1000, 1);
		final MimeMessage[] messages = this.smtpServer.getReceivedMessages();
		assert (messages.length == 1);

		final MimeMessage msg = messages[0];

		SnapshotListener.expect(msg.getAllRecipients()).toMatchSnapshot();
		SnapshotListener.expect(msg.getFrom()).toMatchSnapshot();

		final String registrationMessage = msg.getContent().toString();

		final Pattern p = Pattern.compile("href=\"(http://.*)\""); // the pattern to search for
		final Matcher m = p.matcher(registrationMessage);

		assert (m.find());

		final String registrationLink = StringEscapeUtils.unescapeHtml4(m.group(1));
		this.driver.get(registrationLink);

		this.driver.findElement(By.id("auth-continue-password")).sendKeys("password");
		this.driver.findElement(By.id("auth-continue-confirm-password")).sendKeys("password");
		this.driver.findElement(By.id("auth-continue-name")).sendKeys("John Doe");
		this.driver.findElement(By.id("auth-complete-button")).click();

		// driver.findElement(By.id("auth-signin-email")).sendKeys("test@mail.com");
		this.driver.findElement(By.id("auth-signin-password")).sendKeys("password");
		this.driver.findElement(By.id("auth-signin-button")).click();

		// TODO
		// driver.findElement(By.id("user-dashboard"));

	}

	@Test
	public void testSendAndReceiveEmail() throws Exception {
		final SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom("automator@brainspeedtech.com");
		message.setTo("test@mail.com");
		message.setSubject("Confirm Registration");
		message.setText("Some text");

		this.emailService.send(message);

		final MimeMessage[] messages = this.smtpServer.getReceivedMessages();

		assert (messages.length == 1);

		for (final MimeMessage msg : messages) {
			SnapshotListener.expect(msg.getContent()).toMatchSnapshot();
			SnapshotListener.expect(msg.getAllRecipients()).toMatchSnapshot();
			SnapshotListener.expect(msg.getFrom()).toMatchSnapshot();
		}
	}

	public String url(final String path) {
		return "http://localhost:" + this.port + path;
	}

}
