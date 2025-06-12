package com.AiCapDesignsApp.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="details")
public class OrderDetails {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private double amount;
	private double price;
	private double Total;
	
	@OneToOne
	private Order order;
	
	@ManyToOne
	private Cap cap;
	
	public OrderDetails() {
	}
	
	

	public OrderDetails(Integer id, String name, double amount, double precio, double total) {
		super();
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.price = precio;
		Total = total;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getTotal() {
		return Total;
	}

	public void setTotal(double total) {
		Total = total;
	}
	

	public Order getOrder() {
		return order;
	}



	public void setOrder(Order order) {
		this.order = order;
	}



	public Cap getCap() {
		return cap;
	}



	public void setCap(Cap cap) {
		this.cap = cap;
	}



	@Override
	public String toString() {
		return "orderDetails [id=" + id + ", name=" + name + ", amount=" + amount + ", precio=" + price + ", Total="
				+ Total + "]";
	}
	

}
