package com.bst.pmgr.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextListener;

class ApplicationTest {
	
	private Application application = null;
	
	@BeforeEach
	public void setupApplication() {
		this.application = new Application();
	}

	@Test
	void testRequestContextListener() {
		RequestContextListener contextListener = application.requestContextListener();
		assertNotNull(contextListener);
	}

}
