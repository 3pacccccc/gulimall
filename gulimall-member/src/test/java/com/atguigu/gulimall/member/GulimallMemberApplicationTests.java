package com.atguigu.gulimall.member;

import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallMemberApplicationTests {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Test
    void contextLoads() {
    }

    @Test
    void mybatisTest() {
        MemberLevelEntity defaultLevel = memberLevelDao.getDefaultLevel();
        System.out.println(defaultLevel);

    }
}
