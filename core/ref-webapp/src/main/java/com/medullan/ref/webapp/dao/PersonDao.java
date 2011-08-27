package com.medullan.ref.webapp.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medullan.ref.webapp.domain.Person;

public interface PersonDao extends JpaRepository<Person, Integer> {

}
