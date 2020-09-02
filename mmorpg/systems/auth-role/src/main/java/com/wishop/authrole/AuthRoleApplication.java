/*
The MIT License

Copyright (c) 2019-2020 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.wishop.authrole;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.wishop.authrole.entities.Credential;
import com.wishop.authrole.entities.Permission;
import com.wishop.authrole.entities.Role;
import com.wishop.authrole.repository.CredentialRepository;
import com.wishop.authrole.repository.PermissionRepository;
import com.wishop.authrole.repository.RoleRepository;

@SpringBootApplication
@Configuration
@EnableWebMvc
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableEurekaClient
@EnableCircuitBreaker
public class AuthRoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthRoleApplication.class, args);
	}

	@Profile("development")
	@Bean
	CommandLineRunner initDatabase(PermissionRepository permissionRepository, RoleRepository roleRepository,
			CredentialRepository credentialRepository) {
		return args -> {
			Permission per = new Permission("PERM_READ_NEWS");
			per.setDeleted(true);
			permissionRepository.save(per);
			
			Role role = new Role("Admin");
			role.addPermission(new Permission("PERM_READ_CREDENTIAL"));
			role.addPermission(new Permission("PERM_WRITE_CREDENTIAL"));
			role.addPermission(new Permission("PERM_DELETE_CREDENTIAL"));
			role.addPermission(new Permission("PERM_READ_PERMISSION"));
			role.addPermission(new Permission("PERM_WRITE_PERMISSION"));
			role.addPermission(new Permission("PERM_DELETE_PERMISSION"));
			role.addPermission(new Permission("PERM_READ_ROLE"));
			role.addPermission(new Permission("PERM_WRITE_ROLE"));
			role.addPermission(new Permission("PERM_DELETE_ROLE"));

			Role role2 = new Role("Moder");
			role2.addPermission(new Permission("PERM_DELETE_NEWS9"));
			role2.addPermission(new Permission("PERM_DELETE_NEWS10"));
			role2.addPermission(new Permission("PERM_DELETE_NEWS11"));
			roleRepository.save(role2);

			Credential cre = new Credential("kong", "12345"); // 11
			credentialRepository.save(cre);
			
			Credential myCre = credentialRepository.getByUserName("kong").get(0);
			myCre.addRole(role);
			credentialRepository.save(myCre);

		};
	}

}
