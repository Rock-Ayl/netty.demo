package cn.ayl.common.db.elaticsearch;

import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import cn.ayl.config.Const;
import cn.ayl.util.JsonUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created By Rock-Ayl on 2020-09-07
 * Es索引核心
 */
public class IndexTable {

    protected static Logger logger = LoggerFactory.getLogger(IndexTable.class);

    //协议名称
    private static final String SchemeName = "http";
    //IP
    private static final String Ip = "127.0.0.1";
    //端口
    private static final int Port = 9200;
    //索引名称
    private static final String IndexName = "file";

    /**
     * 创建一个ES连接
     *
     * @return
     */
    public static RestHighLevelClient client() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(Ip, Port, SchemeName)));
    }

    /**
     * 新建一个索引数据例子
     *
     * @param indexJson 数据对象
     * @return
     */
    public static boolean addIndexData(JsonObject indexJson) {
        //创建连接
        RestHighLevelClient esClient = client();
        //创建索引请求
        IndexRequest indexRequest = new IndexRequest(IndexName);
        //组装并设置索引类型
        indexRequest.source(indexJson, XContentType.JSON);
        try {
            //新建索引
            esClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            //日志
            logger.error("新增失败,出现异常:[{}]", e);
            //返回失败
            return false;
        }
        //返回成功
        return true;
    }

    /**
     * 一个学习、测试用的查询例子
     *
     * @param pageIndex 分页-页码
     * @param pageSize  分页-每页数据量
     * @return
     */
    public static JsonObject selPhoneList(Integer pageIndex, Integer pageSize) {
        //创建连接
        RestHighLevelClient esClient = client();
        //创建父查询对象
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        //创建子查询对象
        QueryBuilder childQuery = QueryBuilders.termQuery("fileName", "标");
        //子查询对象放入父查询对象中
        query.must(childQuery);
        //创建查询函数构造对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //把父查询对象放入函数构造对象中
        sourceBuilder.query(query);
        //如果分页
        if (pageIndex != null && pageSize != null) {
            //计算位置
            pageIndex = pageIndex - 1 < 0 ? 0 : pageIndex - 1;
            //设置一次最大获取数据量=20
            pageSize = Math.min(pageSize, 20);
            //如果起始位置为0
            if (pageIndex == 0) {
                //从哪个位置读
                sourceBuilder.from(pageIndex);
                //读几条数据
                sourceBuilder.size(pageSize);
            } else {
                //从哪个位置读
                sourceBuilder.from(pageIndex * pageSize);
                //读几条数据
                sourceBuilder.size(pageSize);
            }
        }
        //设置超时时间
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //取消默认最大查询数量上限(默认10000)
        sourceBuilder.trackTotalHits(true);
        //构造请求发起对象,这里直接配置索引名即可
        SearchRequest searchRequest = new SearchRequest(IndexName);
        //把查询函数构造对象注入查询请求中
        searchRequest.source(sourceBuilder);
        //初始化result
        JsonObject result = JsonObject.Success();
        //初始化items
        JsonObjects items = JsonObjects.VOID();
        try {
            //创建响应对象
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            //获取响应中的列表数据
            SearchHits searchHits = searchResponse.getHits();
            //获取响应中的列表数据总数
            String total = searchHits.getTotalHits().toString();
            //循环参数
            for (SearchHit hit : searchHits.getHits()) {
                //获取该参数
                String value = hit.getSourceAsString();
                //解析成Json
                JsonObject valueJson = JsonUtils.parse(value);
                //组装进items
                items.add(valueJson);
            }
            //items组装至result
            result.put(Const.Items, items);
            //组装总数
            result.put(Const.TotalCount, total);
        } catch (IOException e) {
            //日志
            logger.error("查询失败,出现异常:[{}]", e);
            //返回失败
            return JsonObject.Fail("查询失败.");
        }
        //返回数据
        return result;
    }

    /**
     * 测试主方法
     *
     * @param args
     */
    public static void main(String[] args) {

        //查询
        System.out.println(selPhoneList(1, 10).toString());

        //初始化索引Json
        JsonObject indexJson = JsonObject.VOID();
        //组装一些参数
        indexJson.append("fileId", 20);
        indexJson.append("fileName", "白白.docx");
        indexJson.append("fileSize", 12000);
        indexJson.append("fileExt", "docx");
        indexJson.append("master", 10);
        indexJson.append("time", 1600353921128L);
        //新建数据
        System.out.println(addIndexData(indexJson));

    }
}
