package com.bst.pmgr.configuration.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bst.pmgr.application.Application;
import com.bst.utility.testlib.SeleniumTest;
import com.bst.utility.testlib.SeleniumTestExecutionListener;
import com.bst.utility.testlib.SnapshotListener;

@ExtendWith(SpringExtension.class)
@SeleniumTest(driver = ChromeDriver.class)
@TestExecutionListeners(listeners = { SeleniumTestExecutionListener.class,
		SnapshotListener.class }, mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
public class VersionTest {
	@Autowired
	private WebDriver driver;

	@LocalServerPort
	private int port;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	public void setup() {
		MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	public void testShouldReturnCorrectVersion() throws Exception {
		this.driver.get(this.url("/auth/signup"));
		SnapshotListener.expect(this.driver.findElement(By.id("applicationVersion")).getText()).toMatchSnapshot();
	}

	public String url(final String path) {
		return "http://localhost:" + this.port + path;
	}

}
