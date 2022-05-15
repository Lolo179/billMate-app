package com.AiCapDesignsApp.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="orders")
public class Order {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String number;
	private Date creationDate;
	private Date receptionDate;
	
	private Double total;
	
	@ManyToOne
	private User user;
	
	@OneToOne(mappedBy="order")
	private OrderDetails detalils;

	public Order() {
	}

	public Order(Integer id, String number, Date creationDate, Date receptionDate, Double total) {
		super();
		this.id = id;
		this.number = number;
		this.creationDate = creationDate;
		this.receptionDate = receptionDate;
		this.total = total;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getReceptionDate() {
		return receptionDate;
	}

	public void setReceptionDate(Date receptionDate) {
		this.receptionDate = receptionDate;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}
	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	

	public OrderDetails getDetalils() {
		return detalils;
	}

	public void setDetalils(OrderDetails detalils) {
		this.detalils = detalils;
	}

	@Override
	public String toString() {
		return "order [id=" + id + ", number=" + number + ", creationDate=" + creationDate + ", receptionDate="
				+ receptionDate + ", total=" + total + "]";
	}
	
	

}
