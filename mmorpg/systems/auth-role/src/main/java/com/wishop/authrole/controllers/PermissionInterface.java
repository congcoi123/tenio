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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wishop.authrole.entities.Permission;

@RefreshScope
@Configuration
@ComponentScan
@Validated
@PreAuthorize("denyAll")
public interface PermissionInterface {

	@GetMapping({ "${api.path.permissions.getAll}" })
	@PreAuthorize("hasRole('PERM_READ_PERMISSION')")
	ResponseEntity<Object> getAllPermissions();

	@GetMapping({ "${api.path.permissions.getByPage}" })
	@PreAuthorize("hasRole('PERM_READ_PERMISSION')")
	ResponseEntity<Object> getAllPermissions(@PathVariable("page") @NotNull @Min(1) int page,
			@PathVariable("limit") @NotNull @Min(1) @Max(250) int limit);

	@GetMapping({ "${api.path.permissions.getById}" })
	@PreAuthorize("hasRole('PERM_READ_PERMISSION')")
	ResponseEntity<Object> getPermission(@PathVariable("id") Long id);

	@DeleteMapping({ "${api.path.permissions.deleteById}" })
	@PreAuthorize("hasRole('PERM_DELETE_PERMISSION')")
	ResponseEntity<Object> deletePermission(@PathVariable("id") Long id);

	@PostMapping({ "{$api.path.permissions.createById}" })
	@PreAuthorize("hasRole('PERM_WRITE_PERMISSION')")
	ResponseEntity<Object> savePermission(@Valid @RequestBody Permission permission);

	@PutMapping({ "${api.path.permissions.updateById}" })
	@PreAuthorize("hasRole('PERM_WRITE_PERMISSION')")
	@Deprecated
	ResponseEntity<Object> updatePermission(@RequestBody Permission permission, @PathVariable("id") Long id);

}
