package com.overfit.database;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisUtil {

    private static SqlSessionFactory factory;

    static {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            factory = new SqlSessionFactoryBuilder().build(reader);
            System.out.println("[MyBatis] 연결 성공!");
        } catch (IOException e) {
            System.err.println("[MyBatis] 연결 실패: " + e.getMessage());
        }
    }

    public static SqlSession getSession() {
        return factory.openSession(true); // 자동 커밋
    }
}