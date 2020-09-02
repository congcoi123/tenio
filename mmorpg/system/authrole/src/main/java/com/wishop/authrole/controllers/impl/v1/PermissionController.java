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
package com.wishop.authrole.controllers.impl.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.wishop.authrole.controllers.PermissionInterface;
import com.wishop.authrole.entities.Permission;
import com.wishop.authrole.services.PermissionService;
import com.wishop.common.entities.response.BaseReponse;
import com.wishop.common.entities.response.ListResultsResponse;
import com.wishop.common.entities.response.PageResultsResponse;

@RequestMapping("v1/" + "${api.path.root}")
@RestController
public class PermissionController implements PermissionInterface {

	@Autowired
	private PermissionService permissionService;

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllPermissions() {
		return new ListResultsResponse().setListResults("permissions", permissionService.getAllPermissions()).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllPermissions(int page, int limit) {
		List<Permission> permissions = permissionService.getAllPermissions(page, limit);
		return new PageResultsResponse(page, limit, permissions.size(), permissionService.count())
				.setListResults("permissions", permissions).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getPermission(Long id) {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(permissionService.getPermission(id));
		return new ListResultsResponse().setListResults("permissions", permissions).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> deletePermission(Long id) {
		permissionService.deletePermission(id);
		return new BaseReponse().get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> savePermission(Permission permission) {
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(permissionService.savePermission(permission));
		return new ListResultsResponse().setListResults("permissions", permissions).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> updatePermission(Permission permission, Long id) {
		return null;
	}

}
