package com.bst.pmgr.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class VersionAdvice {

	@Value("${pmgr.app.version}")
	private String applicationVersion;

	@ModelAttribute("applicationVersion")
	public String getApplicationVersion() {
		return this.applicationVersion;
	}

}
