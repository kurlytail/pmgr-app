package com.bst.pmgr.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.bst.user.authentication.components.UserController;
import com.bst.user.registration.components.RegistrationController;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("com.bst.configuration")
@EnableTransactionManagement
public class ApplicationTest {
	
	@Autowired
	private RegistrationController registrationController;
	
	@Autowired
	private UserController userController;

    @Test
    public void contexLoads() throws Exception {
        assertThat(registrationController).isNotNull();
        assertThat(userController).isNotNull();
    }

}
