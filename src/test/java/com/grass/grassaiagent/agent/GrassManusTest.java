package com.grass.grassaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GrassManusTest {

    @Resource
    private GrassManus grassManus;

    @Test
    void testRun() {
        String userPrompt = """
                我的另一半在居住在上海黄浦区，请帮我推荐 5 公里内的合适的约会地点，
                并结合一些网络图片，制定一份详细的约会计划，
                应以 markdown 格式输出
                """;
        String result = grassManus.run(userPrompt);
        Assertions.assertNotNull(result);
        System.out.println(result);
    }

}