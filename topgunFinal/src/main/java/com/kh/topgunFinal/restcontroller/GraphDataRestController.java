package com.kh.topgunFinal.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.topgunFinal.dao.GraphDataDao;
import com.kh.topgunFinal.vo.FlightPaymentVO;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:3000" })
@RestController
@RequestMapping("/api")
public class GraphDataRestController {
    
    @Autowired
    private GraphDataDao graphDataDao;

    // 특정 사용자 ID로 결제 및 항공사 정보를 조회
    @GetMapping("/flight-payments")
    public List<FlightPaymentVO> flightPayments(@RequestParam("userId") String userId) {
        return graphDataDao.flightPayment(userId);
    }
    

    // 모든 사용자에 대한 결제 및 항공사 정보를 조회
    @GetMapping("/all-flight-payments")
    public List<FlightPaymentVO> allFlightPayments() {
        return graphDataDao.allflightPayment();
    }
}
