package com.mobvoi.test.home;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobvoi.test.dao.YixunProductDAO;
import com.mobvoi.test.model.YixunProduct;

@Service
public class YixunHome {
	
	@Autowired
	private YixunProductDAO yixunDAO;
	
	private YixunHome(){}
	
	public void inserProduc(YixunProduct product){
		try {
			yixunDAO.insertOrUpdate(product);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public List<YixunProduct> getListById(int id, int count){
		try {
			return yixunDAO.getListById(id, count);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public List<YixunProduct> getAll(){
		try {
			return yixunDAO.getAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}