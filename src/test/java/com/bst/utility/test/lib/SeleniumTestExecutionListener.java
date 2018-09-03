package com.bst.utility.test.lib;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

public class SeleniumTestExecutionListener extends DependencyInjectionTestExecutionListener {

	private WebDriver driver = null;

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		ApplicationContext context = testContext.getApplicationContext();
		if (context instanceof ConfigurableApplicationContext && driver == null) {
			SeleniumTest annotation = AnnotationUtils.findAnnotation(testContext.getTestClass(), SeleniumTest.class);
			ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
			driver = (WebDriver) beanFactory.createBean(annotation.driver());
			beanFactory.registerSingleton("webDriver", driver);
		}
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		if (driver != null) {
			driver.close();
		}
	}
}
