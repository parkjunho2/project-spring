package com.kh.topgunFinal.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.topgunFinal.dao.GraphDao;
import com.kh.topgunFinal.vo.FlightPaymentVO;

import java.util.List;

@CrossOrigin(origins = {"https://topguntravel.shop", "https://www.topguntravel.shop"})
@RestController
@RequestMapping("/graph")
public class GraphRestController {
    
    @Autowired
    private GraphDao graphDataDao;

    //params 항공사 정보를 조회
    @GetMapping("/airlinePayment")
    public List<FlightPaymentVO> flightPayments(@RequestParam("userId") String userId) {
        return graphDataDao.flightPayment(userId);
    }
    

    // 모든 항공사 결제정보 조회
    @GetMapping("/allFlightPayment")
    public List<FlightPaymentVO> allFlightPayments() {
        return graphDataDao.allflightPayment();
    }
}
