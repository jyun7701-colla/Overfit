package com.overfit.database;

import com.overfit.model.FeedbackVO;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class FeedbackDAO {

    // 피드백 등록
    public int insertFeedback(FeedbackVO fb) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.insert("com.overfit.mapper.FeedbackMapper.insertFeedback", fb);
        }
    }

    // 제품별 피드백 조회
    public List<FeedbackVO> getFeedbackByProduct(int prod_idx) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectList("com.overfit.mapper.FeedbackMapper.getFeedbackByProduct", prod_idx);
        }
    }

    // 사용자별 피드백 조회
    public List<FeedbackVO> getFeedbackByUser(String user_id) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectList("com.overfit.mapper.FeedbackMapper.getFeedbackByUser", user_id);
        }
    }
}