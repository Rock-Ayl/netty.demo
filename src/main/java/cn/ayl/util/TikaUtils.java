package cn.ayl.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by Rock-Ayl on 2020-11-5
 * apache tika Utils 文件内容识别、抽取工具
 */
public class TikaUtils {

    //存储着需要特殊处理的文件真实类型
    public static Map<String, String> TiKaFileRealTypeMaps = new HashMap<>();

    //初始化需要做特殊处理的文件真实类型
    static {
        //docx的类型码,因为docx处理出现重复文本,故而特殊处理
        TiKaFileRealTypeMaps.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
    }

    /**
     * 抽取文件文本内容
     *
     * @param file 文件对象
     * @return
     * @throws IOException
     * @throws TikaException
     */
    public static String getContext(File file) throws IOException, TikaException {
        //创建一个tika
        Tika tika = new Tika();
        //文件真实类型
        String fileRealExt = TiKaFileRealTypeMaps.get(tika.detect(file));
        //判空
        if (fileRealExt != null) {
            //根据文件真实类型做处理
            switch (fileRealExt) {
                //docx由于重复、乱码问题,需要这么做
                case "docx":
                    //结果初始化
                    StringBuffer str = new StringBuffer();
                    //获取docx
                    List<XWPFParagraph> paraGraph = new XWPFDocument(new FileInputStream(file)).getParagraphs();
                    //循环
                    for (XWPFParagraph para : paraGraph) {
                        //循环
                        for (XWPFRun r : para.getRuns()) {
                            //当前文本位置
                            int i = 0;
                            //当前段
                            String thisStr = r.getText(i++);
                            //判空
                            if (StringUtils.isNotEmpty(thisStr)) {
                                //组装
                                str.append(thisStr);
                            }
                        }
                    }
                    //返回docx全内容
                    return str.toString();
            }
        }
        //默认抽取并返回
        return new Tika().parseToString(file);
    }

}  