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
package com.wishop.authrole.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.wishop.authrole.entities.Credential;
import com.wishop.authrole.entities.Permission;
import com.wishop.authrole.entities.Role;
import com.wishop.authrole.services.CredentialService;
import com.wishop.common.exceptions.EntityNotFoundException;

@Service // It has to be annotated with @Service.
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private CredentialService credentialService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		if (!credentialService.isExistCredential(username))
			throw new EntityNotFoundException("User", username);

		Credential credential = credentialService.getCredential(username);
		for (Role role : credential.getRoles()) {
			for (Permission permission : role.getPermissions()) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + permission.getName()));
			}
		}

		return new User(credential.getUserName(), encoder.encode(credential.getPassword()), authorities);
	}

}