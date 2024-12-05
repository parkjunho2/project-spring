package com.kh.topgunFinal.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.kh.topgunFinal.dto.SeatsDto;
import com.kh.topgunFinal.service.CreateSeatService;
import com.kh.topgunFinal.vo.FlightPassangerInfoVO;
import com.kh.topgunFinal.vo.SeatsFlightInfoVO;

@Repository
public class SeatsDao {

	@Autowired
	private SqlSession sqlSession;

	@Autowired
	private CreateSeatService createSeatService;

	@Value("${custom.seat.row}")
	private int seatRow;

	@Value("${custom.seat.col}")
	private int seatCol;
	
	// 좌석 조회
	public List<SeatsDto> selectList(int flightId) {
	    return sqlSession.selectList("seats.selectSeatsListByFlightId", flightId);
	}
	
	// 조건 리스트 조회(FlightId로 좌석을 조회해야한다)
	public List<SeatsDto> selectListByFlightId(int flightId){
		return sqlSession.selectList("seats.listByFlightId", flightId);
	}

	// 좌석 생성
	public void insertList(int flightId) {
	    List<SeatsDto> list = createSeatService.createList(seatRow, seatCol, flightId);
	    for (SeatsDto seat : list) {
	        sqlSession.insert("seats.insert", seat);
	    }
	}
	//항공기에 대한 좌석의 정보
	public List<SeatsFlightInfoVO> selectSeatsFlightInfo(int flightId) {
	    return sqlSession.selectList("payment.seatsFlightInfoByFlightId", flightId);
	}
	
	//좌석 결제시 상태 사용 으로 변경
	public boolean seatsStatus(SeatsDto seatsDto) {
		return sqlSession.update("seats.usedSeats", seatsDto)>0;
	}
	
	//항공기 탑승자 명단
	 public List<FlightPassangerInfoVO> passangerInfo(int flightId) {
	        return sqlSession.selectList("flightPassangerInfo", flightId);
	    }
}

