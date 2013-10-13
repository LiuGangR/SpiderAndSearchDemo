package com.callmer.test.ec.dao;

import java.util.List;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

import com.callmer.test.ec.model.YixunProduct;

@DAO
public interface YixunProductDAO {

	public String DB_NAME = " yixun_product ";
	
	public String FIELDS = " id, price, category, name, description ";
	
	@SQL("INSERT into " + DB_NAME + "("+  FIELDS + ") values (:1.id,:1.price,:1.category,:1.name,:1.description) " +
			"ON DUPLICATE KEY UPDATE " +
			"price=:1.price,category=:1.category,name=:1.name,description=:1.description")
	public int insertOrUpdate(
			@SQLParam("product") YixunProduct product
			);
	
	@SQL("SELECT " + FIELDS + " FROM " + DB_NAME + " WHERE id>:1 order by id asc limit :2")
	public List<YixunProduct> getListById(
			@SQLParam("id") int id,
			@SQLParam("count") int count
			);
	
	@SQL("SELECT " + FIELDS + " FROM " + DB_NAME)
	public List<YixunProduct> getAll(
			);
}
