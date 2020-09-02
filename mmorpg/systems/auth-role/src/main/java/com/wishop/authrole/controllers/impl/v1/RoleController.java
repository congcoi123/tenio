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
import com.wishop.authrole.controllers.RoleInterface;
import com.wishop.authrole.entities.Role;
import com.wishop.authrole.entities.request.AssignPermRequest;
import com.wishop.authrole.services.RoleService;
import com.wishop.common.entities.response.BaseReponse;
import com.wishop.common.entities.response.ListResultsResponse;
import com.wishop.common.entities.response.PageResultsResponse;

@RequestMapping("v1/" + "${api.path.root}")
@RestController
public class RoleController implements RoleInterface {

	@Autowired
	private RoleService roleService;

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllRoles() {
		return new ListResultsResponse().setListResults("roles", roleService.getAllRoles()).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllPermissions(int page, int limit) {
		List<Role> roles = roleService.getAllRoles(page, limit);
		return new PageResultsResponse(page, limit, roles.size(), roleService.count()).setListResults("roles", roles)
				.get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getRole(Long id) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(roleService.getRole(id));
		return new ListResultsResponse().setListResults("roles", roles).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> deleteRole(Long id) {
		roleService.deleteRole(id);
		return new BaseReponse().get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> saveRole(Role role) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(roleService.saveRole(role));
		return new ListResultsResponse().setListResults("roles", roles).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> assignPermissions(AssignPermRequest assignRequest) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(roleService.assignPermissions(assignRequest));
		return new ListResultsResponse().setListResults("roles", roles).get();
	}

}
