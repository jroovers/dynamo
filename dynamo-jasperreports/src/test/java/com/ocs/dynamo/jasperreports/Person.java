package com.ocs.dynamo.jasperreports;

import com.ocs.dynamo.domain.AbstractEntity;

public class Person extends AbstractEntity<Integer> {

	private static final long serialVersionUID = 427919371150660076L;

	private Integer id;

	private Integer socialId;

	private String name;

	private Customer customer;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSocialId() {
		return socialId;
	}

	public void setSocialId(Integer socialId) {
		this.socialId = socialId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Person(Integer socialId, String name) {
		super();
		this.socialId = socialId;
		this.name = name;
	}

	public Person(Integer socialId, String name, Customer customer) {
		super();
		this.socialId = socialId;
		this.name = name;
		this.customer = customer;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}