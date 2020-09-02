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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wishop.authrole.entities.Permission;
import com.wishop.authrole.entities.Role;
import com.wishop.authrole.entities.request.AssignPermRequest;
import com.wishop.authrole.repository.RoleRepository;
import com.wishop.common.exceptions.EntityNotFoundException;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionService permissionService;

	public List<Role> getAllRoles() {
		List<Role> roles = new ArrayList<Role>();
		roleRepository.getAll().forEach(role -> roles.add(role));
		return roles;
	}

	public List<Role> getAllRoles(int page, int limit) {
		List<Role> roles = new ArrayList<Role>();
		if (page < 1 || limit < 1 || limit > 250)
			throw new NumberFormatException();
		// Page start from 1
		roleRepository.getAll((page - 1) * limit, limit).forEach(role -> roles.add(role));
		return roles;
	}

	public long count() {
		return roleRepository.getCount();
	}

	public void deleteRole(Long id) {
		if (isExistRole(id))
			roleRepository.removeById(id, new Date());
		else
			throw new EntityNotFoundException("Role", id);
	}

	public Role saveRole(Role role) {
		return roleRepository.save(role);
	}

	public Role assignPermissions(AssignPermRequest assignRequest) {
		// get parameters
		Long roleId = assignRequest.getRoleId();
		List<String> permissionNames = assignRequest.getPermissions();

		// check role's id
		if (!isExistRole(roleId))
			throw new EntityNotFoundException("Role", roleId);

		// check permissions' name
		for (String permission : permissionNames) {
			if (!permissionService.isExistPermisison(permission))
				throw new EntityNotFoundException("Permission", permission);
		}

		// get all permissions
		Set<Permission> perms = new HashSet<Permission>();
		for (String permissionName : permissionNames) {
			perms.add(permissionService.getPermission(permissionName));
		}

		// assign
		Role role = getRole(roleId);
		role.setPermissions(perms);
		return roleRepository.save(role);
	}

	public Role getRole(Long id) {
		List<Role> roles = roleRepository.getById(id);
		if (roles.isEmpty())
			throw new EntityNotFoundException("Role", id);
		Role role = roles.get(0);
		if (role == null)
			throw new EntityNotFoundException("Role", id);

		return role;
	}

	public boolean isExistRole(Long id) {
		List<Role> roles = roleRepository.getById(id);
		if (roles.isEmpty())
			return false;
		Role role = roles.get(0);
		if (role == null)
			return false;

		return true;
	}

}
