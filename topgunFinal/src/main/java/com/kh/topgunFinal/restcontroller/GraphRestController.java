package com.kh.topgunFinal.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.topgunFinal.dao.GraphDao;
import com.kh.topgunFinal.vo.FlightPaymentVO;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:3000" })
@RestController
@RequestMapping("/graph")
public class GraphRestController {
    
    @Autowired
    private GraphDao graphDataDao;

    // 특정 사용자 ID로 결제 및 항공사 정보를 조회
    @GetMapping("/airlinePayment")
    public List<FlightPaymentVO> flightPayments(@RequestParam("userId") String userId) {
        return graphDataDao.flightPayment(userId);
    }
    

    // 모든 사용자에 대한 결제 및 항공사 정보를 조회
    @GetMapping("/allFlightPayment")
    public List<FlightPaymentVO> allFlightPayments() {
        return graphDataDao.allflightPayment();
    }
}
