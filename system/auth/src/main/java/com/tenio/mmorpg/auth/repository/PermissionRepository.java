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
package com.tenio.mmorpg.auth.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tenio.mmorpg.auth.entities.Permission;

public interface PermissionRepository extends CrudRepository<Permission, Long> {

	@Query(value = "SELECT * FROM permission p WHERE p.deleted = 0 AND p.name = :name", nativeQuery = true)
	List<Permission> getByName(@Param("name") String name);

	@Query(value = "SELECT * FROM permission p WHERE p.deleted = 0 AND p.id = :id", nativeQuery = true)
	List<Permission> getById(@Param("id") Long id);

	@Query(value = "SELECT * FROM permission p WHERE p.deleted = 0", nativeQuery = true)
	List<Permission> getAll();

	// LIMIT have to be set before OFFSET (syntax error preventing)
	@Query(value = "SELECT * FROM permission p WHERE p.deleted = 0 ORDER BY p.id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<Permission> getAll(@Param("offset") int offset, @Param("limit") int limit);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query(value = "UPDATE permission p SET p.deleted = 0, p.updated_date = :updatedDate WHERE p.id = :id", nativeQuery = true)
	void removeById(@Param("id") Long id, @Param("updatedDate") Date updatedDate);

	@Query(value = "SELECT COUNT(*) FROM permission p WHERE p.deleted = 0", nativeQuery = true)
	long getCount();

}
