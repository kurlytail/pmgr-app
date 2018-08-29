package com.bst.pmgr.selenium;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class SeleniumTestExecutionListener implements TestExecutionListener {

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub
		TestExecutionListener.super.beforeTestClass(testContext);
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
	    ApplicationContext context = testContext.getApplicationContext();
	    if (context instanceof ConfigurableApplicationContext) {

	        SeleniumTest annotation = AnnotationUtils.findAnnotation(
	                testContext.getTestClass(), SeleniumTest.class);
	        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
	        Object bean = beanFactory.createBean(annotation.driver());
	        beanFactory.registerSingleton("webDriver", bean);
	    }
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub
		TestExecutionListener.super.beforeTestMethod(testContext);
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub
		TestExecutionListener.super.afterTestMethod(testContext);
	}

}
