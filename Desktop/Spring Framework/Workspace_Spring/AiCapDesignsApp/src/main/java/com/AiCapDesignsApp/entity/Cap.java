package com.AiCapDesignsApp.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cap")
public class Cap {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String img;
	private double price;
	private int amount;
	
	
	@ManyToOne
	private User user;
	
	
	public Cap() {

	}


	public Cap(Integer id, String name, String img, int amount, double price, User user) {
		super();
		this.id = id;
		this.name = name;
		this.img = img;
		this.price = price;
		this.amount = amount;
		this.user = user;
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


	public String getImg() {
		return img;
	}


	public void setImg(String img) {
		this.img = img;
	}


	public int getAmount() {
		return amount;
	}


	public void setAmount(int amount) {
		this.amount = amount;
	}

	public double getPrecio() {
		return price;
	}
	
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	

	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	@Override
	public String toString() {
		return "Gorra [id=" + id + ", name=" + name + ", img=" + img + ", amount=" + amount + "]";
	}





	
	
	
	

}
