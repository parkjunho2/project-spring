package com.kh.topgunFinal.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.topgunFinal.dao.FlightDao;
import com.kh.topgunFinal.dto.FlightDto;
import com.kh.topgunFinal.vo.FlightComplexSearchRequestVO;
import com.kh.topgunFinal.vo.FlightComplexSearchResponseVO;
import com.kh.topgunFinal.vo.FlightVO;
import com.kh.topgunFinal.vo.SeatsCountVO;

import org.springframework.web.bind.annotation.RequestParam;



@CrossOrigin(origins = {"http://localhost:3000"})
@RestController
@RequestMapping("/flight")
public class FlightRestController {

	@Autowired
	private FlightDao flightDao;
	
	
	@GetMapping("/")
	public List<FlightVO> list() {
		return flightDao.selectList();
	}

	@GetMapping("/seatsCountList/{flightId}")
    public List<SeatsCountVO> seatsCountList(@PathVariable int flightId) {
        return flightDao.seatsCount(flightId);
    }

	//검색 
	@GetMapping("/column/{column}/keyword/{keyword}")
	public List<FlightDto> search(
	        @PathVariable String column, @PathVariable String keyword) {
	    List<FlightDto> list = flightDao.search(column, keyword);
	    return list;
	}
	//복합 검색
	@PostMapping("/complexSearch")
	public FlightComplexSearchResponseVO complexSearch(
							@RequestBody FlightComplexSearchRequestVO requestVO) {
		
		int count = flightDao.complexSearchCount(requestVO);
		//마지막 = 페이징을 안쓰는 경우 or 검색개수가 종료번호보다 작거나 같은 경우
		boolean last = requestVO.getEndRow() == null || count <= requestVO.getEndRow();
		 
		FlightComplexSearchResponseVO response = new FlightComplexSearchResponseVO();
		response.setFlightList(flightDao.complexSearch(requestVO));
		response.setCount(count);
		response.setLast(last);
		return response;
	}
	
	@GetMapping("/{flightId}")
	public FlightDto detail(@PathVariable int flightId) {
		return flightDao.selectOne(flightId);
	}
	
	@DeleteMapping("/{flightId}")
	public void delete(@PathVariable int flightId) {
		flightDao.delete(flightId);
	}
	
	@PostMapping("/")
	public void insert(@RequestBody FlightDto flightDto) {
		flightDao.insert(flightDto);
	}
	
	@PutMapping("/")
	public void update(@RequestBody FlightDto flightDto) {
		flightDao.update(flightDto);
	}
	
}
