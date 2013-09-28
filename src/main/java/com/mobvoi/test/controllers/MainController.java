package com.mobvoi.test.controllers;

import java.util.List;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.mobvoi.test.home.YixunHome;
import com.mobvoi.test.model.YixunProduct;
import com.mobvoi.test.service.YixunSearchService;
import com.mobvoi.test.service.YixunSpiderService;
/**
 * controller
 * @author <a href="mailto:rebricate@gmail.com">刘刚</a>
 * @version 2013-9-28
 */
@Path("")
public class MainController {

	@Autowired
	YixunSpiderService spiderService;
	
	@Autowired
	YixunSearchService searchService;
	
	@Autowired
	YixunHome yixunHome;
	
	
    @Get("")
    public String main() {
    	System.out.println("welcome");
        return "@Welcome";
    }
    
    //开始爬数据
    @Get("spider")
    public String spider() {
    	System.out.println("begin");
    	spiderService.sipder(650000, 1000, 50);
        return "@spider";
    }
    
    //生成索引
    @Get("index")
    public String index() {
    	System.out.println("begin create index");
    	searchService.createIndex();
        return "@begin create index";
    }
    
    //获取搜索结果
    @Get("search")
    public String search(Invocation inv, @Param("keyword") String keyword,@Param("cate") String cate,
    		@Param("begin") int begin, @Param("count") int count, 
    		@Param("minPrice") float minPrice,@Param("maxPrice") float maxPrice) {
    	System.out.println("begin search");
    	System.out.println("keyword" + keyword);
    	JSONArray productArray = searchService.getResult(begin, count, keyword, minPrice, maxPrice, cate);
        return "@" + productArray.toString();
    }
    
    //获取搜索建议
    @Get("suggest")
    public String suggest(Invocation inv, @Param("keyword") String keyword,
    		@Param("begin") int begin, @Param("count") int count) {
    	System.out.println("begin search");
    	System.out.println("keyword" + keyword);
    	JSONArray productArray = searchService.getSuggest(begin, count, keyword);
        return "@" + productArray.toString();
    }
    
    //获取源数据
    @Get("list")
    public String list(Invocation inv, @Param("beginId") int beginId, @Param("count") int count) {
    	System.out.println("list");
    	List<YixunProduct> list = yixunHome.getListById(beginId, count);
    	JSONArray productArray = new JSONArray();
    	for (YixunProduct yixunProduct : list) {
			JSONObject jObject = new JSONObject();
			try {
				jObject.put("id", yixunProduct.getId());
				jObject.put("name", yixunProduct.getName());
				jObject.put("category", yixunProduct.getCategory());
				jObject.put("price", yixunProduct.getPrice());
				jObject.put("desc", yixunProduct.getDescription());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			productArray.put(jObject);
		}
        return "@" + productArray.toString();
    }
    
    
}
