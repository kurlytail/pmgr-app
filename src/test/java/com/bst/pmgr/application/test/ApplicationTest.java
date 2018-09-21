package com.bst.pmgr.application.test;

import static com.bst.utility.testlib.SnapshotListener.expect;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bst.utility.testlib.SeleniumTest;
import com.bst.utility.testlib.SeleniumTestExecutionListener;
import com.bst.utility.testlib.SnapshotListener;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SeleniumTest(driver = ChromeDriver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(listeners = { SeleniumTestExecutionListener.class,
		SnapshotListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
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

	@Test
	public void sendAndReceiveEmail() throws Exception {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom("automator@brainspeedtech.com");
		message.setTo("test@mail.com");
		message.setSubject("Confirm Registration");
		message.setText("Some text");

		emailService.send(message);

		MimeMessage[] messages = smtpServerRule.getReceivedMessages();

		assert (messages.length == 1);

		for (MimeMessage msg : messages) {
			expect(msg.getContent()).toMatchSnapshot();
			expect(msg.getAllRecipients()).toMatchSnapshot();
			expect(msg.getFrom()).toMatchSnapshot();
		}
	}

	@Test
	public void registerAndSendEmail() throws Exception {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebDriverWait wait = new WebDriverWait(driver, 5);

		// Signup
		driver.get(url("/auth/signup"));
		driver.findElement(By.id("user-registration-email")).sendKeys("test@mail.com");
		driver.switchTo().frame(0);
		driver.findElement(
				By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='reCAPTCHA'])[1]/preceding::div[4]"))
				.click();

		driver.switchTo().parentFrame();
		wait.until(ExpectedConditions.elementToBeClickable(By.id("signup-button")));
		driver.findElement(By.id("signup-button")).click();

		// Check email
		smtpServerRule.waitForIncomingEmail(1000, 1);
		MimeMessage[] messages = smtpServerRule.getReceivedMessages();
		assert (messages.length == 1);

		MimeMessage msg = messages[0];

		expect(msg.getAllRecipients()).toMatchSnapshot();
		expect(msg.getFrom()).toMatchSnapshot();

		String registrationMessage = msg.getContent().toString();

		Pattern p = Pattern.compile("href=\"(http://.*)\""); // the pattern to search for
		Matcher m = p.matcher(registrationMessage);

		assert (m.find());

		String registrationLink = StringEscapeUtils.unescapeHtml4(m.group(1));
		driver.get(registrationLink);

		driver.findElement(By.id("auth-continue-password")).sendKeys("password");
		driver.findElement(By.id("auth-continue-confirm-password")).sendKeys("password");
		driver.findElement(By.id("auth-continue-name")).sendKeys("John Doe");
		driver.findElement(By.id("auth-complete-button")).click();

		// driver.findElement(By.id("auth-signin-email")).sendKeys("test@mail.com");
		driver.findElement(By.id("auth-signin-password")).sendKeys("password");
		driver.findElement(By.id("auth-signin-button")).click();

		driver.findElement(By.id("root"));

	}

}
