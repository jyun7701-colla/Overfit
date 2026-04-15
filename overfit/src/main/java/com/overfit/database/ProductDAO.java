package com.overfit.database;

import com.overfit.model.ProductVO;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDAO {

    // 클린 검색 (ad_flag = 'N')
    public List<ProductVO> getCleanProducts(String skin_type, String category) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            Map<String, String> map = new HashMap<>();
            map.put("skin_type", skin_type);
            map.put("category",  category);
            return s.selectList("com.overfit.mapper.ProductMapper.getCleanProducts", map);
        }
    }

    // 맞춤 추천
    public List<ProductVO> getRecommendedProducts(String skin_type) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectList("com.overfit.mapper.ProductMapper.getRecommendedProducts", skin_type);
        }
    }

    // 광고 리뷰 수
    public int getAdReviewCount() {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectOne("com.overfit.mapper.ProductMapper.getAdReviewCount");
        }
    }

    // 추천 저장
    public int insertRecommendation(String user_id, int prod_idx, String reco_reason) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            Map<String, Object> map = new HashMap<>();
            map.put("user_id",     user_id);
            map.put("prod_idx",    prod_idx);
            map.put("reco_reason", reco_reason);
            return s.insert("com.overfit.mapper.ProductMapper.insertRecommendation", map);
        }
    }

    // 셀럽 목록
    public List<Map> getCelebBySkipType(String skin_type) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectList("com.overfit.mapper.ProductMapper.getCelebBySkipType", skin_type);
        }
    }
}