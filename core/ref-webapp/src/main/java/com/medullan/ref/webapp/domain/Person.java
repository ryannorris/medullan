/**
 * 
 */
package com.medullan.ref.webapp.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author rnorris
 * 
 */
@Entity
public class Person {

	@Id
	@GeneratedValue
	Integer id;

	@NotNull
	String firstName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
