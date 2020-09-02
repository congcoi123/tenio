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
package com.wishop.authrole.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wishop.authrole.entities.Credential;
import com.wishop.authrole.entities.Permission;
import com.wishop.authrole.entities.Role;
import com.wishop.authrole.entities.request.AssignRoleRequest;
import com.wishop.authrole.repository.CredentialRepository;
import com.wishop.common.exceptions.EntityNotFoundException;

@Service
public class CredentialService {

	@Autowired
	private RoleService roleService;

	@Autowired
	private CredentialRepository credentialRepository;

	public List<Credential> getAllCredentials() {
		List<Credential> credentials = new ArrayList<Credential>();
		credentialRepository.getAll().forEach(credential -> credentials.add(credential));
		return credentials;
	}

	public List<Credential> getAllCredentials(int page, int limit) {
		List<Credential> credentials = new ArrayList<Credential>();
		if (page < 1 || limit < 1 || limit > 250)
			throw new NumberFormatException();
		// Page start from 1
		credentialRepository.getAll((page - 1) * limit, limit).forEach(credential -> credentials.add(credential));
		return credentials;
	}

	public long count() {
		return credentialRepository.getCount();
	}

	public List<Role> getAllRoles(String userName) {
		if (!isExistCredential(userName))
			throw new EntityNotFoundException("User", userName);

		Credential credential = getCredential(userName);
		return new ArrayList<Role>(credential.getRoles());
	}

	public List<String> getAllPermissions(String userName) {
		if (!isExistCredential(userName))
			throw new EntityNotFoundException("User", userName);

		List<String> permissions = new ArrayList<String>();
		Credential credential = getCredential(userName);
		Set<Role> roles = credential.getRoles();
		for (Role role : roles) {
			for (Permission perm : role.getPermissions()) {
				if (!permissions.contains(perm.getName()))
					permissions.add(perm.getName());
			}
		}
		return permissions;
	}

	public void deleteCredential(String userName) {
		if (!isExistCredential(userName))
			throw new EntityNotFoundException("User", userName);
		else
			credentialRepository.removeByUserName(userName, new Date());
	}

	public Credential saveCredential(Credential credential) {
		return credentialRepository.save(credential);
	}

	public Credential assignRole(AssignRoleRequest assignRequest) {
		// get parameters
		Long roleId = assignRequest.getRoleId();
		String userName = assignRequest.getUserName();

		// check role's id
		if (!roleService.isExistRole(roleId))
			throw new EntityNotFoundException("Role", roleId);

		// check user's name
		if (!isExistCredential(userName))
			throw new EntityNotFoundException("User", userName);

		// assign
		Credential credential = getCredential(userName);
		Role role = roleService.getRole(roleId);
		credential.addRole(role);
		return credentialRepository.save(credential);
	}

	public Credential getCredential(String userName) {
		List<Credential> credentials = credentialRepository.getByUserName(userName);
		if (credentials.isEmpty())
			throw new EntityNotFoundException("User", userName);
		Credential credential = credentials.get(0);
		if (credential == null)
			throw new EntityNotFoundException("User", userName);

		return credential;
	}

	public boolean isExistCredential(String userName) {
		List<Credential> credentials = credentialRepository.getByUserName(userName);
		if (credentials.isEmpty())
			return false;
		Credential credential = credentials.get(0);
		if (credential == null)
			return false;

		return true;
	}

}
