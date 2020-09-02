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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wishop.authrole.entities.Permission;
import com.wishop.authrole.repository.PermissionRepository;
import com.wishop.common.exceptions.EntityDuplicatedException;
import com.wishop.common.exceptions.EntityNotFoundException;

@Service
public class PermissionService {

	@Autowired
	PermissionRepository permissionRepository;

	public List<Permission> getAllPermissions() {
		List<Permission> permissions = new ArrayList<Permission>();
		permissionRepository.getAll().forEach(permission -> permissions.add(permission));
		return permissions;
	}

	public List<Permission> getAllPermissions(int page, int limit) {
		List<Permission> permissions = new ArrayList<Permission>();
		if (page < 1 || limit < 1 || limit > 250)
			throw new NumberFormatException();
		// Page start from 1
		permissionRepository.getAll((page - 1) * limit, limit).forEach(permission -> permissions.add(permission));
		return permissions;
	}

	public long count() {
		return permissionRepository.getCount();
	}

	public Permission getPermission(Long id) {
		if (__isExistPermisison(id))
			return __getPermission(id);
		else
			throw new EntityNotFoundException("Permission", id);
	}

	public void deletePermission(Long id) {
		if (__isExistPermisison(id)) {
			permissionRepository.removeById(id, new Date());
		} else
			throw new EntityNotFoundException("Permission", id);
	}

	public Permission savePermission(Permission permission) {
		if (!isExistPermisison(permission.getName()))
			return permissionRepository.save(permission);
		else
			throw new EntityDuplicatedException("Permission", permission.getName());
	}

	private Permission __getPermission(Long id) {
		List<Permission> permissions = permissionRepository.getById(id);
		if (permissions.isEmpty())
			throw new EntityNotFoundException("Permission", id);
		Permission permission = permissions.get(0);
		if (permission == null)
			throw new EntityNotFoundException("Permission", id);

		return permission;
	}

	private boolean __isExistPermisison(Long id) {
		List<Permission> permissions = permissionRepository.getById(id);
		if (permissions.isEmpty())
			return false;
		Permission permission = permissions.get(0);
		if (permission == null)
			return false;

		return true;
	}

	public Permission getPermission(String name) {
		List<Permission> permissions = permissionRepository.getByName(name);
		if (permissions.isEmpty())
			throw new EntityNotFoundException("Permission", name);
		Permission permission = permissions.get(0);
		if (permission == null)
			throw new EntityNotFoundException("Permission", name);

		return permission;
	}

	public boolean isExistPermisison(String name) {
		List<Permission> permissions = permissionRepository.getByName(name);
		if (permissions.isEmpty())
			return false;
		Permission permission = permissions.get(0);
		if (permission == null)
			return false;

		return true;
	}

}
