package cn.lsz.word.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.dictionary.DictionaryFactory;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.util.WordConfTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: lsz
 * @create: 2019-12-03 10:00
 *
 */
@Service
public class AreaNameFilterService implements ApplicationRunner {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Map<String, Object> areaMap = new HashMap<>();

    private Map<String, String> areaMappingMap = new HashMap<>();

    /**
     * Word分词器配置文件,可用逗号配置多个文件
     */
    private final String AREA_WORD_FILE_PATH = "word/area-word.txt";
    /**
     * 分词映射文件路径（src/main/resources/word/area-mapping.txt）
     */
    private final String AREA_MAPPING_FILE_PATH = "src"+File.separator+"main"+File.separator+"resources"+File.separator+ "word"+File.separator+"area-mapping.txt";
    /**
     * 数据库内容路径（src/main/resources/word/area-db.txt）
     */
    private final String AREA_DB_FILE_PATH = "src"+File.separator+"main"+File.separator+"resources"+File.separator+ "word"+File.separator+"area-db.txt";

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        //初始化映射缓存（个别地名映射会有偏差）
        initAreaMappingMap();
        //初始化数据库（这里用配置文件代替）
        initAreaDbMap();
        //Word分词器配置文件,可用逗号配置多个文件,该分词器目前仅作为检索区域使用，所以将其他相关配置项全部设置为空(提高效率)
        WordConfTools.set("dic.path","classpath:"+AREA_WORD_FILE_PATH);
        WordConfTools.set("bigram.path","null");
        WordConfTools.set("part.of.speech.des.path","null");
        WordConfTools.set("part.of.speech.dic.path","null");
        WordConfTools.set("punctuation.path","null");
        WordConfTools.set("quantifier.path","null");
        WordConfTools.set("stopwords.path","null");
        WordConfTools.set("surname.path","null");
        WordConfTools.set("word.antonym.path","null");
        WordConfTools.set("word.refine.path","null");
        WordConfTools.set("word.synonym.path","null");
        DictionaryFactory.reload();
        LOGGER.info("配置结束");
    }

    private void initAreaDbMap() throws IOException {
        File file = new File(AREA_DB_FILE_PATH);
        List<String> list = FileUtils.readLines(file,UTF_8);
        list.stream().forEach(s -> {
            String[] temp = s.split("=");
            areaMap.put(temp[0], JSONObject.parseObject(temp[1]));
        });
    }

    private void initAreaMappingMap() throws IOException {
        File file = new File(AREA_MAPPING_FILE_PATH);
        List<String> list = FileUtils.readLines(file,UTF_8);
        list.stream().forEach(s -> {
            String[] temp = s.split("=");
            areaMappingMap.put(temp[0],temp[1]);
        });
    }


    public Set<Object> getAreaFullInfo(String text) {

        Set<Object> areaSet = new HashSet<>();
        List<Word> words = WordSegmenter.seg(text);

        LOGGER.info("分词结果:"+words);
        words.stream().forEach( w -> {
            String word = w.getText();
            //目前还没有单独一个字符的地区名所以省略length = 1的
            if(word.length() > 1){
                Object result = areaMap.get(areaMappingMap.get(word));
                if(result != null){
                    areaSet.add(result);
                }
            }
        });

        return areaSet;
    }
}
