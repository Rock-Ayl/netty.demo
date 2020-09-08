package cn.ayl.common.db.elaticsearch;

import cn.ayl.common.json.JsonObject;
import cn.ayl.common.json.JsonObjects;
import cn.ayl.config.Const;
import cn.ayl.util.JsonUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created By Rock-Ayl on 2020-09-07
 * Es索引 核心
 */
public class IndexTable {

    //协议名称
    private static final String SchemeName = "http";
    //IP
    private static final String Ip = "127.0.0.1";
    //端口
    private static final int Port = 9200;

    /**
     * 创建一个ES连接
     *
     * @return
     */
    public static RestHighLevelClient client() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(Ip, Port, SchemeName)));
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
        RestHighLevelClient EsClient = client();
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
        SearchRequest searchRequest = new SearchRequest("file");
        //把查询函数构造对象注入查询请求中
        searchRequest.source(sourceBuilder);
        //初始化result
        JsonObject result = JsonObject.VOID();
        //初始化items
        JsonObjects items = JsonObjects.VOID();
        try {
            //创建响应对象
            SearchResponse searchResponse = EsClient.search(searchRequest, RequestOptions.DEFAULT);
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
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(selPhoneList(1, 10).toString());
    }
}
