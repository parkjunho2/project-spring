package com.kh.topgunFinal.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.topgunFinal.vo.FlightPaymentVO;

@Repository
public class GraphDataDao {

    @Autowired
    private SqlSession sqlSession;

    public List<FlightPaymentVO> flightPayment(String userId) {
        return sqlSession.selectList("graph.flightPayment", userId);
    }
    
    // 모든 사용자에 대한 결제 및 항공사 정보를 조회
    public List<FlightPaymentVO> allflightPayment() {
        return sqlSession.selectList("graph.allFlightPayment");
    }
}