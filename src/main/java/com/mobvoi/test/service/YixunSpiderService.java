package com.mobvoi.test.service;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobvoi.test.home.YixunHome;
import com.mobvoi.test.model.YixunProduct;
/**
 * 爬易迅数据的蜘蛛
 * @author <a href="mailto:rebricate@gmail.com">刘刚</a>
 * @version 2013-9-28
 */
@Service
public class YixunSpiderService {
	
	private final static int CATE_LEVEL_COUNT = 3;

	private static YixunSpiderService instance = new YixunSpiderService();
	
	@Autowired
	private YixunHome yixunHome;
	
	public static YixunSpiderService getInstance() {
		return instance;
	}

	private YixunSpiderService(){}
	
	public void sipder(int beginId, int count, int times) {
		YixunProduct product ;
		System.out.println("Yixun product pider begin!");
		for (int i = 0; i < times; i++) {
			for (int j = 0; j < count; j++) {
				product = getAProduct(beginId++);
				if(product != null){
					yixunHome.inserProduc(product);
				}
			}
			System.out.println("Spider yixun poducts.Count:" + count + "|endId" + beginId);
		}
		System.out.println("Yixun product pider over!");
	}

	private YixunProduct getAProduct(int id) {
		String url = "http://item.yixun.com/item-" + id + ".html";
		String userAgent = "Mozilla/5.0 (X11; U; Linux i586; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)";

		try {
			Document doc = Jsoup.connect(url)
					.userAgent(userAgent) // ! set the user agent
					.timeout(10 * 1000) // set timeout
					.get();

			// 先解析此链接是否可用，不可用是返回“非常抱歉”
			String titile = doc.select(".hd").text();
			if (!titile.contains("非常抱歉")) {
				Elements categories = doc.select(".mod_crumb a");
				Elements price = doc.select(".xbase_row2 dd");
				Elements keywords = doc.select(".mod_crumb span");
				Elements description = doc.select(".xbase_row1 h1");

				for (Element element : categories) {
					System.out.println(element.text());
				}
				System.out.println(price.text());
				System.out.println(keywords.text());
				System.out.println(description.text());
				
				
				YixunProduct product = new YixunProduct();
				product.setId(id);
				product.setCategory(generateCate(categories));
				product.setName(keywords.text());
				product.setDescription(description.text());
				// 返回的是"￥12.12",有些会返回暂无报价，就设置为0
				try {
					product.setPrice(Float
							.parseFloat(price.text().substring(1)));
				} catch (Exception e) {
					product.setPrice(0);
				}

				return product;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String generateCate(Elements categories) {
		StringBuilder cateSB = new StringBuilder();
		// 现在的情况，只有二手商品是没有分类的，那就默认为0
		if (categories.size() == CATE_LEVEL_COUNT) {
			String cate_1 = categories.get(1).text();
			String cate_2 = categories.get(2).text();
			cateSB.append(cate_1).append(" ").append(cate_2);
		}
		return cateSB.toString();
	}

}
