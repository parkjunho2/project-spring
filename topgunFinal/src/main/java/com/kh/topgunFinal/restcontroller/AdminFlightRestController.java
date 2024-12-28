package com.kh.topgunFinal.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.repository.Lock;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.topgunFinal.dao.AdminFlightDao;
import com.kh.topgunFinal.dao.FlightDao;
import com.kh.topgunFinal.dao.SeatsDao;
import com.kh.topgunFinal.dto.FlightDto;

@CrossOrigin(origins = {"https://localhost:3000"})
@RestController
@RequestMapping("/admin")
public class AdminFlightRestController {

	@Autowired
	private AdminFlightDao adminFlightDao;
	
	@Autowired
	private SeatsDao seatsDao;
	
	@GetMapping("/")
	public List<FlightDto> list() {
		return adminFlightDao.selectList();
	}
	
	
	@GetMapping("/{flightId}")
	public FlightDto detail(@PathVariable int flightId) {
		return adminFlightDao.selectOne(flightId);
	}
	
	//검색 
	@GetMapping("/column/{column}/keyword/{keyword}")
	public List<FlightDto> search(
	        @PathVariable String column, @PathVariable String keyword) {
	    List<FlightDto> list = adminFlightDao.search(column, keyword);
	    return list;
	}
	
	@PutMapping("/update")
    @Transactional
    public void update(@RequestBody FlightDto flightDto) {
        adminFlightDao.update(flightDto);

        // 승인 완료 후 좌석 생성
        if(flightDto.getFlightStatus().equals("승인")) {
            seatsDao.insertList(flightDto.getFlightId());
        }
    }
}
