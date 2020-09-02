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
package com.wishop.authrole.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.wishop.authrole.entities.Credential;

public interface CredentialRepository extends CrudRepository<Credential, String> {

	@Query(value = "SELECT * FROM credential c WHERE c.deleted = 0 AND c.user_name = :user_name", nativeQuery = true)
	List<Credential> getByUserName(@Param("user_name") String userName);

	@Query(value = "SELECT * FROM credential c WHERE c.deleted = 0 AND r.id = :id", nativeQuery = true)
	List<Credential> getById(@Param("id") Long id);

	@Query(value = "SELECT * FROM credential c WHERE c.deleted = 0", nativeQuery = true)
	List<Credential> getAll();

	// LIMIT have to be set before OFFSET (syntax error preventing)
	@Query(value = "SELECT * FROM credential c WHERE c.deleted = 0 ORDER BY c.id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
	List<Credential> getAll(@Param("offset") int offset, @Param("limit") int limit);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query(value = "UPDATE credential c SET c.deleted = 0, c.updated_date = :updatedDate WHERE r.id = :id", nativeQuery = true)
	void removeById(@Param("id") Long id, @Param("updatedDate") Date updatedDate);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query(value = "UPDATE credential c SET c.deleted = 0, c.updated_date = :updatedDate WHERE r.user_name = :user_name", nativeQuery = true)
	void removeByUserName(@Param("user_name") String userName, @Param("updatedDate") Date updatedDate);

	@Query(value = "SELECT COUNT(*) FROM credential c WHERE c.deleted = 0", nativeQuery = true)
	long getCount();

}
