package com.kh.topgunFinal.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kh.topgunFinal.dto.FlightDto;
import com.kh.topgunFinal.vo.FlightComplexSearchRequestVO;
import com.kh.topgunFinal.vo.FlightVO;
import com.kh.topgunFinal.vo.SeatsCountVO;

@Repository
public class FlightDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    
    @Autowired
    private SqlSession sqlSession;
    
    // 등록
    public void insert(FlightDto dto) {
        sqlSession.insert("flight.add", dto);
    }
    
    // 수정
    public boolean update(FlightDto dto) {
        int result = sqlSession.update("flight.fix", dto);
        return result > 0;
    }
    
    // 삭제
    public boolean delete(int flightId) {
        return sqlSession.delete("flight.del", flightId) > 0;
    }
    
    // 조회
    public List<FlightVO> selectList() {
        return sqlSession.selectList("flight.list"); 
    }
    
    // 검색
    public List<FlightDto> search(String column, String keyword) {
        Map<String, Object> params = new HashMap<>();
        params.put("column", column);
        params.put("keyword", keyword);
        return sqlSession.selectList("flight.search", params);
    }
    //복합검색
    public List<FlightVO> complexSearch(FlightComplexSearchRequestVO  requestVO){
    	return sqlSession.selectList("flight.complexFlightSearch" , requestVO);
    }
     
	//복합 검색 카운트 메소드
	public int complexSearchCount(FlightComplexSearchRequestVO requestVO) {
		return sqlSession.selectOne("flight.complexSearchCount", requestVO);
	}
	
    // 상세
    public FlightDto selectOne(int flightId) {
        return sqlSession.selectOne("flight.find", flightId);
    }
    
     
    // 시퀀스 생성 및 등록 메소드
    public int sequence() {
        String sql = "select flight_seq.nextval from dual";
        return jdbcTemplate.queryForObject(sql, int.class);
    }
    
    public void insertWithSequence(FlightDto flightDto) {
        String sql = "insert into flight("
                        + "flight_id, flight_number, departure_time, arrival_time, "
                        + "flight_time, departure_airport, arrival_airport, user_id, flight_price"
                    + ") values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] data = {
            flightDto.getFlightId(), 
            flightDto.getFlightNumber(),
            flightDto.getDepartureTime(),
            flightDto.getArrivalTime(),
            flightDto.getFlightTime(),
            flightDto.getDepartureAirport(),
            flightDto.getArrivalAirport(),
            flightDto.getUserId(),
            flightDto.getFlightPrice()
        };
        jdbcTemplate.update(sql, data);
    }
    //항공편 가격
	public int selectPrice(int flightId) {
		return sqlSession.selectOne("flight.getFlightPrice", flightId);
	}
	//항공편 출발위치
	public String selectArrival(int flightId) {
		return sqlSession.selectOne("flight.getArrivalAirport", flightId);
	}

    //항공편 좌석 수
    public List<SeatsCountVO> seatsCount(int flightId){
        return sqlSession.selectList("seats.seatsCount", flightId);
    }

}


