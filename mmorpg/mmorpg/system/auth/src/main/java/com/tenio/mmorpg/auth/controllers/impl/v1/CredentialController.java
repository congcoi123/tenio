/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.mmorpg.auth.controllers.impl.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.tenio.mmorpg.auth.controllers.CredentialInterface;
import com.tenio.mmorpg.auth.entities.Credential;
import com.tenio.mmorpg.auth.entities.request.AssignRoleRequest;
import com.tenio.mmorpg.auth.services.CredentialService;
import com.tenio.mmorpg.common.entities.response.BaseReponse;
import com.tenio.mmorpg.common.entities.response.ListResultsResponse;
import com.tenio.mmorpg.common.entities.response.PageResultsResponse;

@RequestMapping("v1/" + "${api.path.root}")
@RestController
public class CredentialController implements CredentialInterface {

	@Autowired
	private CredentialService credentialService;

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllCredentials() {
		return new ListResultsResponse().setListResults("users", credentialService.getAllCredentials()).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllCredentials(int page, int limit) {
		List<Credential> credentials = credentialService.getAllCredentials(page, limit);
		return new PageResultsResponse(page, limit, credentials.size(), credentialService.count())
				.setListResults("users", credentials).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllRoles(String userName) {
		return new ListResultsResponse().setListResults("roles", credentialService.getAllRoles(userName)).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getAllPermissions(String userName) {
		return new ListResultsResponse().setListResults("permissions", credentialService.getAllPermissions(userName))
				.get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> getCredential(String userName) {
		List<Credential> credentials = new ArrayList<Credential>();
		credentials.add(credentialService.getCredential(userName));
		return new ListResultsResponse().setListResults("users", credentials).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> deleteCredential(String userName) {
		credentialService.deleteCredential(userName);
		return new BaseReponse().get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> saveCredential(Credential credential) {
		List<Credential> credentials = new ArrayList<Credential>();
		credentials.add(credentialService.saveCredential(credential));
		return new ListResultsResponse().setListResults("users", credentials).get();
	}

	@HystrixCommand
	@Override
	public ResponseEntity<Object> assignRole(AssignRoleRequest assignRequest) {
		List<Credential> credentials = new ArrayList<Credential>();
		credentials.add(credentialService.assignRole(assignRequest));
		return new ListResultsResponse().setListResults("users", credentials).get();
	}

}
