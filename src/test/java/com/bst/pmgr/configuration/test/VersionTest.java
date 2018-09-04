package com.bst.pmgr.configuration.test;

import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bst.pmgr.application.Application;
import com.bst.utility.test.lib.SeleniumTest;

@RunWith(SpringJUnit4ClassRunner.class)
@SeleniumTest(driver = ChromeDriver.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
public class VersionTest {
	@Autowired
	private WebDriver driver;

	@LocalServerPort
	private int port;

	@Autowired
	private WebApplicationContext context;

	@Before
	public void setup() {
		MockMvcBuilders.webAppContextSetup(context).build();
	}

	public String url(String path) {
		return "http://localhost:" + port + path;
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
	@DisplayName("Should provide the correct version in the test")
	public void shouldReturnCorrectVersion() throws Exception {
		driver.get(url("/auth/signup"));
		expect(driver.findElement(By.id("applicationVersion")).getText()).toMatchSnapshot();
	}

}
