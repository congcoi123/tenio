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
package com.wishop.common.configurations;

import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;

// To use this class outside. You have to 
// 1. Define it as a bean, either by adding @Component or use @Bean to instantiate an object from it
// 2. Use the @Autowire to ask spring to auto create it for you, and inject all the values.

// So, If you tried to create an instance manually (i.e. new JwtConfig()). This won't inject all the values. 
// Because you didn't ask Spring to do so (it's done by you manually!).
// Also, if, at any time, you tried to instantiate an object that's not defined as a bean
// Don't expect Spring will autowire the fields inside that class object.

public class JwtConfig {

	// Spring doesn't inject/autowire to "static" fields.
	// Link: https://stackoverflow.com/a/6897406
	// Can be set as configuration parameters in application.properties
	@Getter
	@Value("${security.jwt.uri:/auth/**}")
	private String Uri;

	@Getter
	@Value("${security.jwt.header:Authorization}")
	private String header;

	@Getter
	@Value("${security.jwt.prefix:Bearer }")
	private String prefix;

	@Getter
	@Value("${security.jwt.expiration:#{24*60*60}}")
	private int expiration;

	@Getter
	@Value("${security.jwt.secret:JwtSecretKey}")
	private String secret;

}
