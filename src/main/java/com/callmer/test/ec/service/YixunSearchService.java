package com.callmer.test.ec.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wltea4pinyin.analyzer.lucene.IKAnalyzer4PinYin;

import com.callmer.test.ec.home.YixunHome;
import com.callmer.test.ec.model.YixunProduct;
import com.callmer.test.ec.service.analyzer.PrefixAnalyzer;
/**
 * 搜索的服务，依靠于lucene
 * @author <a href="mailto:rebricate@gmail.com">刘刚</a>
 * @version 2013-9-28
 */
@Service
public class YixunSearchService {

	
	private static String FEILD_NAME = "name";
    
    //使用standard分词器分词的name
    private static String FEILD_NAME_STANDARD = "name_standard";

    //使用IK分词器分词的name
    private static String FEILD_NAME_IK = "name_IK";

    //使用prefix分词器分词的name
    private static String FEILD_NAME_PREFIX = "name_prefix";

    //使用prefix分词器分词的全拼name
    private static String FEILD_NAME_QUANPIN = "name_quanpin";

    //使用prefix分词器分词的首字母name
    private static String FEILD_NAME_SHOUZIMU = "name_shouzimu";

    private static String FEILD_ID = "id";

    private static String FEILD_CATE = "cate";
    
    private static String FEILD_PRICE = "price";

    private static String FEILD_DESC = "desc";

    // 保存路径
    private static String RESULT_INDEX_DIR = "/home/liugang/mobvoi/result/index/";

    // 保存路径
    private static String SUGGEST_INDEX_DIR = "/home/liugang/mobvoi/suggest/index/";

    //实例化IKAnalyzer分词器
    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);

    private static IndexSearcher resultIndexSearcher;

    private static IndexSearcher suggestIndexSearcher;
    
    //reslut指定域所用的分析器
    private static QueryParser RESULT_NAME_QP = new QueryParser(Version.LUCENE_42, FEILD_NAME, analyzer);
    private static QueryParser RESULT_CATE_QP = new QueryParser(Version.LUCENE_42, FEILD_CATE, analyzer);
    private static QueryParser RESULT_NAME_PREFIX_QP = new QueryParser(Version.LUCENE_42, FEILD_NAME_PREFIX, new PrefixAnalyzer());
    private static QueryParser RESULT_DESC_QP = new QueryParser(Version.LUCENE_42, FEILD_DESC, new IKAnalyzer4PinYin(true));
    
    //suggest指定域所用的分析器
    private static QueryParser SUGGEST_NAME_QP = new QueryParser(Version.LUCENE_42, FEILD_NAME,
            new PrefixAnalyzer());
    private static QueryParser SUGGEST_NAME_STAND_QP = new QueryParser(Version.LUCENE_42, FEILD_NAME,
            new StandardAnalyzer(Version.LUCENE_42));
    private static QueryParser SUGGEST_NAME_IK_QP = new QueryParser(Version.LUCENE_42, FEILD_NAME_IK,
                    new IKAnalyzer4PinYin(true));
    private static QueryParser SUGGEST_PINYIN_QP = new QueryParser(Version.LUCENE_42, FEILD_NAME_QUANPIN,
            new PrefixAnalyzer());
    private static QueryParser SUGGEST_SHOUZIMU_QP = new QueryParser(Version.LUCENE_42,
            FEILD_NAME_SHOUZIMU, new PrefixAnalyzer());

    
    
    @Autowired
    private YixunHome home;
    
    public void createIndex(){
    	List<YixunProduct> productList = home.getAll();
    	System.out.println("size" + productList.size());
    	createResultIndex(productList);
    	createSuggestIndex(productList);
    }
    
    
    /**
     * 加载索引文件
     */
    @PostConstruct
    private synchronized void loadIndex() {
        System.out.println("AppSearchService LoadIndex");
        try {
            Directory directory = null;
            try {
                directory = FSDirectory.open(new File(RESULT_INDEX_DIR));
                RAMDirectory ramDir = new RAMDirectory(directory, IOContext.READ);
                DirectoryReader indexReader = DirectoryReader.open(ramDir);
                resultIndexSearcher = new IndexSearcher(indexReader);
            } catch (IOException e) {
                System.out.println("resultIndexSearcher load erro");
                e.printStackTrace();
            }
            try {
                directory = FSDirectory.open(new File(SUGGEST_INDEX_DIR));
                RAMDirectory ramDir = new RAMDirectory(directory, IOContext.READ);
                DirectoryReader indexReader = DirectoryReader.open(ramDir);
                suggestIndexSearcher = new IndexSearcher(indexReader);
            } catch (IOException e) {
            	 System.out.println("suggestIndexSearcher load erro");
                e.printStackTrace();
            }
        } catch (Exception e) {
        	System.out.println("AppSearchService init error!");
        }
        System.out.println("AppSearchService LoadIndex over");
    }
    
    
    
    
    
	private void createResultIndex(List<YixunProduct> productList){
		long timeB = System.currentTimeMillis();
        try {
            Directory directory = FSDirectory.open(new File(RESULT_INDEX_DIR));
            File indexFile = new File(RESULT_INDEX_DIR);
            if (!indexFile.exists()) {
                indexFile.mkdirs();
            } else {
                deleteFile(indexFile);
                indexFile.mkdirs();
            }

            //指定域所用的分析器
            Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
            analyzerMap.put(FEILD_NAME_PREFIX, new PrefixAnalyzer());
            analyzerMap.put(FEILD_DESC, new IKAnalyzer4PinYin(true));

            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerMap);
            
            
            // 索引读取的配置
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_42, wrapper);
            conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter indexWriter = new IndexWriter(directory, conf);

            Iterator<YixunProduct> resulstIt = productList.iterator();

            while (resulstIt.hasNext()) {
            	YixunProduct product = resulstIt.next();
                Document document = new Document();

                //添加域
                document.add(new IntField(FEILD_ID, product.getId(), Field.Store.YES));
                document.add(new TextField(FEILD_NAME, product.getName(), Field.Store.YES));
                document.add(new TextField(FEILD_NAME_PREFIX, product.getName(), Field.Store.NO));
                document.add(new TextField(FEILD_CATE, product.getCategory(), Field.Store.YES));
                document.add(new FloatField(FEILD_PRICE, product.getPrice(), Field.Store.YES));
                document.add(new TextField(FEILD_DESC, product.getDescription(), Field.Store.YES));
                indexWriter.addDocument(document);

            }
            indexWriter.commit();
            indexWriter.close();
            System.out.println("Android App result index create time is "
                    + String.valueOf(System.currentTimeMillis() - timeB));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //logger.error("Android App result index create error!", e);
        }
	}
	
	
	/**
     * 生成搜索建议的索引，包含汉字，全拼和首字母
     * 
     * @param resultList
     */
    private void createSuggestIndex(List<YixunProduct> productList) {

        long timeB = System.currentTimeMillis();
        try {
            Directory directory = FSDirectory.open(new File(SUGGEST_INDEX_DIR));
            File indexFile = new File(SUGGEST_INDEX_DIR);
            if (!indexFile.exists()) {
                indexFile.mkdirs();
            } else {
                deleteFile(indexFile);
                indexFile.mkdirs();
            }

            //指定域所用的分析器
            Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
            analyzerMap.put(FEILD_NAME_QUANPIN, new PrefixAnalyzer());
            analyzerMap.put(FEILD_NAME_SHOUZIMU, new PrefixAnalyzer());
            analyzerMap.put(FEILD_NAME_STANDARD, new StandardAnalyzer(Version.LUCENE_42));
            analyzerMap.put(FEILD_NAME, new PrefixAnalyzer());

            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new IKAnalyzer4PinYin(
                    true), analyzerMap);

            // 索引读取的配置

            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_42, wrapper);
            conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter indexWriter = new IndexWriter(directory, conf);

            Iterator<YixunProduct> resulstIt = productList.iterator();

            while (resulstIt.hasNext()) {
            	YixunProduct app = resulstIt.next();
                Document document = new Document();

                //添加域
                document.add(new IntField(FEILD_ID, app.getId(), Field.Store.YES));
                document.add(new TextField(FEILD_NAME, app.getName(), Field.Store.YES));
                document.add(new TextField(FEILD_NAME_STANDARD, app.getName(), Field.Store.NO));
                document.add(new TextField(FEILD_NAME_IK, app.getName(), Field.Store.NO));
                document.add(new TextField(FEILD_NAME_QUANPIN, PinyinHelper.convertToPinyinString(
                        app.getName(), "", PinyinFormat.WITHOUT_TONE), Field.Store.NO));
                document.add(new TextField(FEILD_NAME_SHOUZIMU, PinyinHelper.getShortPinyin(app
                        .getName()), Field.Store.NO));

                indexWriter.addDocument(document);

            }
            indexWriter.commit();
            indexWriter.close();
            System.out.println("Android App index create time is "
                    + String.valueOf(System.currentTimeMillis() - timeB));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        	System.out.println("Android App index create error!");
        }
    }

	
	
	/**
     * 获取搜索结果
     * @param begin
     * @param count
     * @param keyword
     * @return
     */
    public JSONArray getResult(int begin, int count, String keyword,
    		float minPrice, float maxPrice, String cate) {
        //searchLogger.info("Result begin:" + begin + " count:" + count + " keyword:" + keyword);

    	JSONArray productArray = new JSONArray();

        if (resultIndexSearcher == null) {
            return productArray;
        }

        try {

            TopDocs topDoc = null;

            //设置权重
            Query descQuery = RESULT_DESC_QP.parse(keyword);
            Query nameQuery = RESULT_NAME_QP.parse(keyword);
            Query namePrefixQuery = RESULT_NAME_PREFIX_QP.parse(keyword);
            
            //设置搜索条件
            BooleanQuery bq = new BooleanQuery();
            BooleanQuery innerbq = new BooleanQuery();
            innerbq.add(namePrefixQuery, BooleanClause.Occur.SHOULD);
            innerbq.add(nameQuery, BooleanClause.Occur.SHOULD);
            innerbq.add(descQuery, BooleanClause.Occur.SHOULD);
            bq.add(innerbq, BooleanClause.Occur.MUST);

            if(0.0 < minPrice && minPrice < maxPrice){
                Query priceQuery = NumericRangeQuery.newFloatRange(FEILD_PRICE, minPrice, maxPrice, true, true);
                bq.add(priceQuery, BooleanClause.Occur.MUST);
            }
            if(cate != null && cate.length() > 0){
            	System.out.println("fukc!" + cate);
                Query catePrefixQuery = RESULT_CATE_QP.parse(cate);
                bq.add(catePrefixQuery, BooleanClause.Occur.MUST);
            }
            
            //设置排序规则
            Sort sort = new Sort(
                    new SortField(FEILD_CATE,SortField.Type.SCORE), 
                    new SortField(FEILD_NAME_PREFIX,SortField.Type.SCORE),
                    new SortField(FEILD_NAME,SortField.Type.SCORE), 
                    new SortField(FEILD_DESC,SortField.Type.SCORE)); 

            topDoc = resultIndexSearcher.search(bq, count + begin, sort);

            ScoreDoc[] scoreDocs = topDoc.scoreDocs;

            System.out.println("scoreDocs count" + scoreDocs.length);
            for (int i = begin; (i < begin + count) && (i < scoreDocs.length); i++) {
                Document doc = resultIndexSearcher.doc(scoreDocs[i].doc);
                JSONObject jobject = new JSONObject();
                try {
					jobject.put(FEILD_ID, doc.get(FEILD_ID));
					jobject.put(FEILD_NAME, doc.get(FEILD_NAME));
	                jobject.put(FEILD_CATE, doc.get(FEILD_CATE));
	                jobject.put(FEILD_PRICE, doc.get(FEILD_PRICE));
	                jobject.put(FEILD_DESC, doc.get(FEILD_DESC));
				} catch (JSONException e) {
					e.printStackTrace();
				}
                productArray.put(jobject);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return productArray;
    }

    
    /**
     * 提供搜索建议
     * @param begin
     * @param count
     * @param keyword
     * @return
     */
    public JSONArray getSuggest(int begin, int count, String keyword) {

    	JSONArray nameArray = new JSONArray();

        if (resultIndexSearcher == null) {
            return nameArray;
        }

        try {
            
            TopDocs topDoc = null;

            //设置权重
            Query nameQuery = SUGGEST_NAME_QP.parse(keyword);
            nameQuery.setBoost(4);
            Query nameStandardQuery = SUGGEST_NAME_STAND_QP.parse(keyword);
            nameStandardQuery.setBoost(4);
            Query nameIKQuery = SUGGEST_NAME_IK_QP.parse(keyword);
            nameQuery.setBoost(1);
            Query quanpinQuery = SUGGEST_PINYIN_QP.parse(keyword);
            nameQuery.setBoost(3);
            Query shouzimuQuery = SUGGEST_SHOUZIMU_QP.parse(keyword);
            nameQuery.setBoost(2);

            //搜索规则
            BooleanQuery bq = new BooleanQuery();
            BooleanQuery innerbq = new BooleanQuery();
            innerbq.add(nameQuery, BooleanClause.Occur.SHOULD);
            innerbq.add(nameStandardQuery, BooleanClause.Occur.SHOULD);
            innerbq.add(nameIKQuery, BooleanClause.Occur.SHOULD);
            innerbq.add(quanpinQuery, BooleanClause.Occur.SHOULD);
            innerbq.add(shouzimuQuery, BooleanClause.Occur.SHOULD);
            bq.add(innerbq, BooleanClause.Occur.MUST);

            topDoc = suggestIndexSearcher.search(bq, count + begin);
            ScoreDoc[] scoreDocs = topDoc.scoreDocs;
            for (int i = begin; (i < begin + count) && (i < scoreDocs.length); i++) {
                Document doc = suggestIndexSearcher.doc(scoreDocs[i].doc);
                nameArray.put(doc.get(FEILD_NAME));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return nameArray;
    }
    
    
	/**
     * 删除目录（文件夹）以及目录下的文件
     * 
     * @param dir 被删除目录的文件路径
     */
    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    this.deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            System.out.println("所删除的文件不存在！" + '\n');
        }
    }

	
	
}
