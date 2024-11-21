package com.kh.topgunFinal.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.topgunFinal.dto.PaymentDetailDto;
import com.kh.topgunFinal.dto.PaymentDto;
import com.kh.topgunFinal.vo.PaymentTotalVO;
import com.kh.topgunFinal.vo.SeatsFlightInfoVO;
import com.kh.topgunFinal.vo.TimerVO;

@Repository
public class PaymentDao {
    @Autowired
    private SqlSession sqlSession;
//카카오 결제 주문번호
    public int payServiceSequence(){
        return sqlSession.selectOne("payment.payServiceSequence");
    }
    
    public int paymentSequence(){
        return sqlSession.selectOne("payment.paymentSequence");
    }
    public int paymentDetailSequence(){
        return sqlSession.selectOne("payment.paymentDetailSequence");
    }
    public void paymentInsert(PaymentDto paymentDto){
        sqlSession.insert("payment.paymentInsert", paymentDto);
    }
    public void paymentDetailInsert(PaymentDetailDto paymentDetailDto){
        sqlSession.insert("payment.paymentDetailInsert", paymentDetailDto);
    }
	public List<PaymentDto> selectList(String userId) {
		return sqlSession.selectList("payment.list", userId);
	}
	public PaymentDto selectOne(int paymentNo) {
		return sqlSession.selectOne("payment.find", paymentNo);
	}
	public List<PaymentDetailDto> selectDetailList(int paymentNo){
		return sqlSession.selectList("payment.findDetail", paymentNo);
	}
	public List<PaymentTotalVO> selectTotalList(String userId){
		return sqlSession.selectList("payment.findTotal", userId);
	}
	//전체취소
	public boolean cancelAll(int paymentNo) {
		return sqlSession.update("payment.cancelAll", paymentNo)>0;
	}
	public boolean cancelAllItem(int paymentNo) {
		return sqlSession.update("payment.cancelAllItem", paymentNo)>0;
	}
	//항목취소
	public boolean cancelItem(int paymentDetailNo) {
		return sqlSession.update("payment.cancelItem", paymentDetailNo)>0;
	}
	public boolean decreaseItemRemain(int paymentNo, int money) {
		Map<String, Integer> params = new HashMap<>();
		params.put("paymentNo", paymentNo);
		params.put("money", money);
		return sqlSession.update("payment.decreaseItemRemain", params)>0;
	}
	public PaymentDetailDto selectDetailOne(int paymentDetailNo) {
		return sqlSession.selectOne("payment.selectDetailOne", paymentDetailNo);
	}
	// 정보 추가입력
	public boolean updatePaymentDetail(PaymentDetailDto paymentDetailDto) {
	    return sqlSession.update("payment.paymentDetailUpdate", paymentDetailDto) > 0;
	}
	// 좌석과 항공편 정보를 조회하는 메서드
    public List<SeatsFlightInfoVO> seatsFlightInfo(int flightId) {
        return sqlSession.selectList("payment.seatsFlightInfo", flightId);
    }
	 
    public List<PaymentDetailDto> selectPaymentDetailList(int paymentNo) {
        return sqlSession.selectList("payment.selectPaymentDetailList", paymentNo);
    }
 // 항공편 좌석 모든 리스트 수정완료 
    public List<SeatsFlightInfoVO> seatsFlightInfoList() {
        return sqlSession.selectList("payment.seatsFlightInfoList");
    }
    
   //DB 데이터 중복확인
   public int exists(int flightId, int seatsNo) {
    Map<String, Integer> params = new HashMap<>();
    params.put("flightId", flightId);
    params.put("seatsNo", seatsNo);
    return sqlSession.selectOne("payment.exists", params);
}

	public List<TimerVO> timerList(int paymentNo) {
		return sqlSession.selectList("payment.timerList", paymentNo);  // selectList 사용
	}
}
