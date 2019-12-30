package cn.lsz.word;

import cn.lsz.word.service.AreaNameFilterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest
class WordApplicationTests {

    @Autowired
    AreaNameFilterService service;

    @Test
    void contextLoads() throws InterruptedException {
        String txt = "我是上海人，去过帝都玩";
        Set<Object> areaFullInfo = service.getAreaFullInfo(txt);
        System.out.println("检索结果："+areaFullInfo);

    }

}
