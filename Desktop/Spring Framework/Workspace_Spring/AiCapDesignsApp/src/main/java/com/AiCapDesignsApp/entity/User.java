package com.AiCapDesignsApp.entity;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String username;
	private String email;
	private String adress;
	private String phone;
	private String type;
	private String password;

	@OneToMany(mappedBy = "user")
	private List<Cap> caps;

	@OneToMany(mappedBy = "user")
	private List<Order> orders;

	public User() {
	}

	public User(Integer id, String name, String username, String email, String adress, String phone, String type,
			String password, List<Cap> caps, List<Order> orders) {
		super();
		this.id = id;
		this.name = name;
		this.username = username;
		this.email = email;
		this.adress = adress;
		this.phone = phone;
		this.type = type;
		this.password = password;
		this.caps = caps;
		this.orders = orders;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Cap> getProductos() {
		return caps;
	}

	public void setProductos(List<Cap> caps) {
		this.caps = caps;
	}

	public List<Order> getOrdenes() {
		return orders;
	}

	public void setOrdenes(List<Order> orders) {
		this.orders = orders;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", username=" + username + ", email=" + email + ", adress="
				+ adress + ", phone=" + phone + ", type=" + type + ", password=" + password + ", caps=" + caps
				+ ", ordenes=" + orders + "]";
	}

}