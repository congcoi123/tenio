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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = {AuthApplication.class, SecurityTest.SecurityTestConfig.class})
@WebAppConfiguration
//@IntegrationTest("server.port:0")
public class SecurityTest {

	private static final String X_AUTH_USERNAME = "X-Auth-Username";
	private static final String X_AUTH_PASSWORD = "X-Auth-Password";
//	private static final String X_AUTH_TOKEN = "X-Auth-Token";

	@Value("${local.server.port}")
	int port;

	@Value("${keystore.file}")
	String keystoreFile;

	@Value("${keystore.pass}")
	String keystorePass;

	@Configuration
	public static class SecurityTestConfig {

	}

	@Before
	public void setup() {

	}

	@Test
	public void healthEndpoint_isAvailableToEveryone() {
		when().get("/health").then().statusCode(HttpStatus.OK.value()).body("status", equalTo("UP"));
	}

	@Test
	public void metricsEndpoint_withoutBackendAdminCredentials_returnsUnauthorized() {
		when().get("/metrics").then().statusCode(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void metricsEndpoint_withInvalidBackendAdminCredentials_returnsUnauthorized() {
		String username = "test_user_2";
		String password = "InvalidPassword";
		given().header(X_AUTH_USERNAME, username).header(X_AUTH_PASSWORD, password).when().get("/metrics").then()
				.statusCode(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void metricsEndpoint_withCorrectBackendAdminCredentials_returnsOk() {
		String username = "backend_admin";
		String password = "remember_to_change_me_by_external_property_on_deploy";
		given().header(X_AUTH_USERNAME, username).header(X_AUTH_PASSWORD, password).when().get("/metrics").then()
				.statusCode(HttpStatus.OK.value());
	}
//
//    @Test
//    public void authenticate_withoutPassword_returnsUnauthorized() {
//        given().header(X_AUTH_USERNAME, "SomeUser").
//                when().post(Api.AUTH).
//                then().statusCode(HttpStatus.UNAUTHORIZED.value());
//
//        BDDMockito.verifyNoMoreInteractions(mockedExternalServiceAuthenticator);
//    }
//
//    @Test
//    public void authenticate_withoutUsername_returnsUnauthorized() {
//        given().header(X_AUTH_PASSWORD, "SomePassword").
//                when().post(Api.AUTH).
//                then().statusCode(HttpStatus.UNAUTHORIZED.value());
//
//        BDDMockito.verifyNoMoreInteractions(mockedExternalServiceAuthenticator);
//    }
//
//    @Test
//    public void authenticate_withoutUsernameAndPassword_returnsUnauthorized() {
//        when().post(Api.AUTH).
//                then().statusCode(HttpStatus.UNAUTHORIZED.value());
//
//        BDDMockito.verifyNoMoreInteractions(mockedExternalServiceAuthenticator);
//    }
//
//    @Test
//    public void authenticate_withValidUsernameAndPassword_returnsToken() {
////        authenticateByUsernameAndPasswordAndGetToken();
//    }
//
//    @Test
//    public void authenticate_withInvalidUsernameOrPassword_returnsUnauthorized() {
//        String username = "test_user_2";
//        String password = "InvalidPassword";
//
//        BDDMockito.when(mockedExternalServiceAuthenticator.authenticate(anyString(), anyString())).
//                thenThrow(new BadCredentialsException("Invalid Credentials"));
//
//        given().header(X_AUTH_USERNAME, username).header(X_AUTH_PASSWORD, password).
//                when().post(Api.AUTH).
//                then().statusCode(HttpStatus.UNAUTHORIZED.value());
//    }
//
//    @Test
//    public void gettingStuff_withoutToken_returnsUnauthorized() {
////        when().get(ApiController.STUFF_URL).
////                then().statusCode(HttpStatus.UNAUTHORIZED.value());
//    }
//
//    @Test
//    public void gettingStuff_withInvalidToken_returnsUnathorized() {
////        given().header(X_AUTH_TOKEN, "InvalidToken").
////                when().get(ApiController.STUFF_URL).
////                then().statusCode(HttpStatus.UNAUTHORIZED.value());
//    }
//
//    @Test
//    public void gettingStuff_withValidToken_returnsData() {
////        String generatedToken = authenticateByUsernameAndPasswordAndGetToken();
//
////        given().header(X_AUTH_TOKEN, generatedToken).
////                when().get(ApiController.STUFF_URL).
////                then().statusCode(HttpStatus.OK.value());
//    }

//    private String authenticateByUsernameAndPasswordAndGetToken() {
//        String username = "test_user_2";
//        String password = "ValidPassword";
//
//        UsernamePasswordAuthenticator authenticationWithToken = new UsernamePasswordAuthenticator(username, null);
//        BDDMockito.when(mockedExternalServiceAuthenticator.authenticate(eq(username), eq(password))).
//                thenReturn(authenticationWithToken);
//
//        ValidatableResponse validatableResponse = given().header(X_AUTH_USERNAME, username).
//                header(X_AUTH_PASSWORD, password).
//                when().post(Api.AUTH).
//                then().statusCode(HttpStatus.OK.value());
//        String generatedToken = authenticationWithToken.getToken();
//        validatableResponse.body("token", equalTo(generatedToken));
//
//        return generatedToken;
//    }

}
