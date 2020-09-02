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
package com.wishop.authrole.controllers;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wishop.authrole.entities.Credential;
import com.wishop.authrole.entities.request.AssignRoleRequest;

@RefreshScope
@Configuration
@ComponentScan
@Validated
@PreAuthorize("denyAll")
public interface CredentialInterface {

	@GetMapping({ "${api.path.credentials.getAll}" })
	@PreAuthorize("hasRole('PERM_READ_CREDENTIAL')")
	ResponseEntity<Object> getAllCredentials();

	@GetMapping({ "${api.path.credentials.getByPage}" })
	@PreAuthorize("hasRole('PERM_READ_CREDENTIAL')")
	ResponseEntity<Object> getAllCredentials(@PathVariable("page") @NotNull @Min(1) int page,
			@PathVariable("limit") @NotNull @Min(1) @Max(250) int limit);

	@GetMapping({ "${api.path.credentials.getRolesByUsername}" })
	@PreAuthorize("hasRole('PERM_READ_CREDENTIAL')")
	ResponseEntity<Object> getAllRoles(@PathVariable("username") String userName);

	@GetMapping({ "${api.path.credentials.getPermissionsByUsername}" })
	@PreAuthorize("hasRole('PERM_READ_CREDENTIAL')")
	ResponseEntity<Object> getAllPermissions(@PathVariable("username") String userName);

	@GetMapping({ "${api.path.credentials.getByUsername}" })
	@PreAuthorize("hasRole('PERM_READ_CREDENTIAL')")
	ResponseEntity<Object> getCredential(@PathVariable("username") String userName);

	@DeleteMapping({ "${api.path.credentials.deleteByUsername}" })
	@PreAuthorize("hasRole('PERM_DELETE_CREDENTIAL')")
	ResponseEntity<Object> deleteCredential(@PathVariable("username") String userName);

	@PostMapping({ "${api.path.credentials.createUsername}" })
	@PreAuthorize("hasRole('PERM_WRITE_CREDENTIAL')")
	ResponseEntity<Object> saveCredential(@RequestBody Credential credential);

	@PostMapping({ "${api.path.credentials.assign}" })
	@PreAuthorize("hasRole('PERM_WRITE_CREDENTIAL')")
	ResponseEntity<Object> assignRole(@Valid @RequestBody AssignRoleRequest assignRequest);

}
