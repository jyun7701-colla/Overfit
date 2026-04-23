package com.overfit.database;

import com.overfit.model.UserVO;
import org.apache.ibatis.session.SqlSession;

public class UserDAO {

    // 회원가입
    public int insertUser(UserVO user) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.insert("com.overfit.mapper.UserMapper.insertUser", user);
        }
    }

    // 로그인
    public UserVO loginUser(UserVO user) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectOne("com.overfit.mapper.UserMapper.loginUser", user);
        }
    }

    // 아이디 중복 확인
    public boolean isIdDuplicate(String user_id) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            int count = s.selectOne("com.overfit.mapper.UserMapper.isIdDuplicate", user_id);
            return count > 0;
        }
    }

    // 이메일로 아이디 찾기
    public String findIdByEmail(String email) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.selectOne("com.overfit.mapper.UserMapper.findIdByEmail", email);
        }
    }

    // 기본 정보 수정
    public int updateUser(UserVO user) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.update("com.overfit.mapper.UserMapper.updateUser", user);
        }
    }

    // 비밀번호 변경
    public int updatePassword(UserVO user) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.update("com.overfit.mapper.UserMapper.updatePassword", user);
        }
    }

    // 피부 타입 수정
    public int updateSkinType(UserVO user) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.update("com.overfit.mapper.UserMapper.updateSkinType", user);
        }
    }

    // 회원 탈퇴
    public int deleteUser(UserVO user) {
        try (SqlSession s = MyBatisUtil.getSession()) {
            return s.delete("com.overfit.mapper.UserMapper.deleteUser", user);
        }
    }
}