package com.kh.topgunFinal.restcontroller;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.topgunFinal.dao.FlightDao;
import com.kh.topgunFinal.dao.PaymentDao;
import com.kh.topgunFinal.dao.SeatsDao;
import com.kh.topgunFinal.dto.PaymentDetailDto;
import com.kh.topgunFinal.dto.PaymentDto;
import com.kh.topgunFinal.dto.SeatsDto;
import com.kh.topgunFinal.error.TargetNotFoundException;
import com.kh.topgunFinal.service.PayService;
import com.kh.topgunFinal.service.TokenService;
import com.kh.topgunFinal.vo.FlightPassangerInfoVO;
import com.kh.topgunFinal.vo.PaymentInfoVO;
import com.kh.topgunFinal.vo.PaymentTotalVO;
import com.kh.topgunFinal.vo.SeatsApproveRequestVO;
import com.kh.topgunFinal.vo.SeatsFlightInfoVO;
import com.kh.topgunFinal.vo.SeatsPurchaseRequestVO;
import com.kh.topgunFinal.vo.SeatsQtyVO;
import com.kh.topgunFinal.vo.TimerVO;
import com.kh.topgunFinal.vo.UserClaimVO;
import com.kh.topgunFinal.vo.pay.PayApproveRequestVO;
import com.kh.topgunFinal.vo.pay.PayApproveResponseVO;
import com.kh.topgunFinal.vo.pay.PayCancelRequestVO;
import com.kh.topgunFinal.vo.pay.PayCancelResponseVO;
import com.kh.topgunFinal.vo.pay.PayOrderRequestVO;
import com.kh.topgunFinal.vo.pay.PayOrderResponseVO;
import com.kh.topgunFinal.vo.pay.PayReadyRequestVO;
import com.kh.topgunFinal.vo.pay.PayReadyResponseVO;

@CrossOrigin(origins = {"https://localhost:3000"})
@RestController
@RequestMapping("/seats")
public class SeatsRestController {

	@Autowired
	private FlightDao flightDao;

	@Autowired
	private SeatsDao seatsDao;

	@Autowired
	private PayService payService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PaymentDao paymentDao;

	@Autowired
	private SqlSession sqlSession;


	// 좌석 조회
	@GetMapping("/{flightId}")
	public List<SeatsDto> list(@PathVariable int flightId) {
		return seatsDao.selectList(flightId);
	}

	// 항공편 정보 조회
	@GetMapping("/info/{flightId}")
	public List<SeatsFlightInfoVO> flightInfoVO(@PathVariable int flightId) {
		return paymentDao.seatsFlightInfo(flightId);
	}

	// 항공기 탑승자 명단 조회
	@GetMapping("/passanger/{flightId}")
	public List<FlightPassangerInfoVO> getPassengerInfo(@PathVariable int flightId) {
		return seatsDao.passangerInfo(flightId);
	}
	//구매하기버튼 클릭 중복검사
	@PostMapping("/exist")
    public void checkDuplicate(@RequestBody List<SeatsQtyVO> seatsList) {
        // 좌석 리스트 순회하며 중복 체크
        for (SeatsQtyVO seatRequest : seatsList) {
            int count = paymentDao.exists(seatRequest.getFlightId(), seatRequest.getSeatsNo());
            if (count > 0) {
                throw new TargetNotFoundException("이미 결제된 좌석입니다: " + seatRequest.getSeatsNo());
            }
        }
    }

	// 좌석 구매
	@PostMapping("/purchase")
	public PayReadyResponseVO purchase(@RequestHeader("Authorization") String token, // 회원토큰
			@RequestBody SeatsPurchaseRequestVO request) throws URISyntaxException {
		UserClaimVO claimVO = // 회원 아이디 불러옴
				tokenService.check(tokenService.removeBearer(token));
		List<SeatsFlightInfoVO> flightInfoList = paymentDao
				.seatsFlightInfo(request.getSeatsList().get(0).getFlightId());
		List<SeatsDto> seatsList = seatsDao.selectList(request.getSeatsList().get(0).getFlightId());
		int flightPrice = flightInfoList.get(0).getFlightPrice();
		String arrival = flightDao.selectArrival(request.getSeatsList().get(0).getFlightId());
		StringBuffer buffer = new StringBuffer();
		int total = 0;
		for (SeatsQtyVO vo : request.getSeatsList()) {
			SeatsDto seatDto = seatsList.stream()
					.filter(seat -> seat.getSeatsNo() == vo.getSeatsNo())
					.findFirst()
					.orElseThrow(() -> new TargetNotFoundException("결제 대상 없음"));
			total += (seatDto.getSeatsPrice() + flightPrice) * vo.getQty();
			if (buffer.isEmpty()) {
				buffer.append(flightInfoList.get(0).getAirlineName() + " ");
				buffer.append(arrival + "행 ");
				buffer.append(seatDto.getSeatsRank());
				buffer.append(seatDto.getSeatsNumber() + " ");
			}
		}if (request.getSeatsList().size() >= 2) { // 2좌석 이상 구매시
			buffer.append(" 외 " + (request.getSeatsList().size() - 1) + "건 ");
		}
		// ready 준비 (입력)
		PayReadyRequestVO requestVO = new PayReadyRequestVO();
		requestVO.setPartnerOrderId(paymentDao.payServiceSequence());// 주문번호 Random
		requestVO.setPartnerUserId(claimVO.getUserId());// header token
		requestVO.setItemName(buffer.toString());
		requestVO.setTotalAmount(total);
		requestVO.setApprovalUrl(request.getApprovalUrl());
		requestVO.setCancelUrl(request.getCancelUrl());
		requestVO.setFailUrl(request.getFailUrl());
		PayReadyResponseVO responseVO = payService.ready(requestVO); // ready 처리 (입력된 값을) , payservice로 가서 ready #4에 requestVO 입력
		return responseVO;// ready 출력 PayService response로부터 tid,url,partner_order_id, partner_user_id 받아옴
	}

	// response에 받은 tid ,partner_order_id, partner_user_id , pg_token 전달
	@Transactional
	@PostMapping("/approve")
	public PayApproveResponseVO approve(@RequestHeader("Authorization") String token, // 아이디 토큰
			@RequestBody SeatsApproveRequestVO request // tid,pg_token,partnerOrderId
	) throws URISyntaxException {

		UserClaimVO claimVO = // 아이디 토큰 불러옴
				tokenService.check(tokenService.removeBearer(token));

		// 중복 좌석 체크
		for (SeatsQtyVO seatRequest : request.getSeatsList()) {
			int count = paymentDao.exists(seatRequest.getFlightId(), seatRequest.getSeatsNo());
			if (count > 0) {
				throw new TargetNotFoundException("이미 결제된 좌석입니다: " + seatRequest.getSeatsNo());
			}
		}

		// approve 준비 (입력)
		PayApproveRequestVO requestVO = new PayApproveRequestVO();
		requestVO.setPartnerOrderId(request.getPartnerOrderId());
		requestVO.setPartnerUserId(claimVO.getUserId());
		requestVO.setTid(request.getTid());
		requestVO.setPgToken(request.getPgToken());

		// approve 처리 client에 전송
		PayApproveResponseVO responseVO = payService.approve(requestVO);

		// DB저장
		// [1]대표 정보 등록
		int flightPrice = flightDao.selectPrice(request.getSeatsList().get(0).getFlightId());
		int paymentSeq = paymentDao.paymentSequence();

		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setPaymentNo(paymentSeq);// 결제번호
		paymentDto.setPaymentTid(responseVO.getTid());//// 거래번호
		paymentDto.setFlightId(request.getSeatsList().get(0).getFlightId());
		paymentDto.setPaymentName(responseVO.getItemName());// 상품명
		paymentDto.setPaymentTotal(responseVO.getAmount().getTotal());// 총결제금액
		paymentDto.setPaymentRemain(paymentDto.getPaymentTotal());// 취소가능금액
		paymentDto.setUserId(claimVO.getUserId());// 결제한 아이디
		paymentDao.paymentInsert(paymentDto);// 대표정보 등록

		// [2]상세 정보 등록
		List<SeatsDto> seatsList = seatsDao.selectList(request.getSeatsList().get(0).getFlightId());
		for (SeatsQtyVO qtyVO : request.getSeatsList()) {// tid,pg_token,partner_orderId
			SeatsDto seatsDto = seatsList.stream()
					.filter(seat -> seat.getSeatsNo() == qtyVO.getSeatsNo())
					.findFirst()
					.orElseThrow(() -> new TargetNotFoundException("존재하지 않는 좌석입니다"));
			int paymentDetailSeq = paymentDao.paymentDetailSequence();// 번호추출
			PaymentDetailDto paymentDetailDto = new PaymentDetailDto();
			paymentDetailDto.setPaymentDetailNo(paymentDetailSeq);// 번호 설정
			paymentDetailDto.setFlightId(seatsDto.getFlightId());// 항공기번호
			paymentDetailDto.setPaymentDetailName(seatsDto.getSeatsRank() + seatsDto.getSeatsNumber());// 좌석번호
			paymentDetailDto.setPaymentDetailPrice(seatsDto.getSeatsPrice() + flightPrice);// 좌석판매가
			paymentDetailDto.setPaymentDetailSeatsNo(seatsDto.getSeatsNo());// 좌석별고유번호
			paymentDetailDto.setPaymentDetailQty(qtyVO.getQty());// 구매수량
			paymentDetailDto.setPaymentDetailOrigin(paymentSeq);// 어느소속에 상세번호인지
			paymentDetailDto.setPaymentDetailPassport(qtyVO.getPaymentDetailPassport());
			paymentDetailDto.setPaymentDetailPassanger(qtyVO.getPaymentDetailPassanger());
			paymentDetailDto.setPaymentDetailEnglish(qtyVO.getPaymentDetailEnglish());
			paymentDetailDto.setPaymentDetailSex(qtyVO.getPaymentDetailSex());
			paymentDetailDto.setPaymentDetailBirth(qtyVO.getPaymentDetailBirth());
			paymentDetailDto.setPaymentDetailCountry(qtyVO.getPaymentDetailCountry());
			paymentDetailDto.setPaymentDetailVisa(qtyVO.getPaymentDetailVisa());
			paymentDetailDto.setPaymentDetailExpire(qtyVO.getPaymentDetailExpire());
			seatsDao.seatsStatus(seatsDto);// 결제시 사용으로 변경
			paymentDao.paymentDetailInsert(paymentDetailDto);
		}
		// approve 출력
		return responseVO;
	}

	// 구매 내역 조회
	@GetMapping("/paymentlist")
	public List<PaymentDto> paymentList(@RequestHeader("Authorization") String token) {
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
		List<PaymentDto> list = paymentDao.selectList(claimVO.getUserId());
		return list;
	}

	// 구매 내역 상세 조회
	@GetMapping("/paymentlist/{paymentNo}")
	public List<PaymentDetailDto> paymentDetailList(@RequestHeader("Authorization") String token,
			@PathVariable int paymentNo) {
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
		PaymentDto paymentDto = paymentDao.selectOne(paymentNo);
		if (paymentDto == null)
			throw new TargetNotFoundException("존재하지 않는 결제번호");
		if (!paymentDto.getUserId().equals(claimVO.getUserId()))// 내 결제 정보가 아니면
			throw new TargetNotFoundException("잘못된 대상의 결제번호");
		List<PaymentDetailDto> list = paymentDao.selectDetailList(paymentNo);
		return list;
	}

	// 결제된 모든목록 조회
	@GetMapping("/paymentTotalList")
	public List<PaymentTotalVO> paymentTotalList(@RequestHeader("Authorization") String token) {
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
		return paymentDao.selectTotalList(claimVO.getUserId());
	}

	@GetMapping("/order/{tid}")
	public PayOrderResponseVO order(@PathVariable String tid) throws URISyntaxException {
		PayOrderRequestVO request = new PayOrderRequestVO();
		request.setTid(tid);
		return payService.order(request);
	}

	@GetMapping("/detail/{paymentNo}")
	public PaymentInfoVO detail(@RequestHeader("Authorization") String token, @PathVariable int paymentNo)
			throws URISyntaxException {
		// 결제내역
		PaymentDto paymentDto = paymentDao.selectOne(paymentNo);
		if (paymentDto == null)
			throw new TargetNotFoundException("존재하지 않는 결제내역");
		// 회원 소유 검증
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
		if (!paymentDto.getUserId().equals(claimVO.getUserId()))
			throw new TargetNotFoundException("결제내역의 소유자가 아닙니다.");
		// 결제 상세 내역
		List<PaymentDetailDto> list = paymentDao.selectDetailList(paymentNo);
		// 조회내역
		PayOrderRequestVO requestVO = new PayOrderRequestVO();
		requestVO.setTid(paymentDto.getPaymentTid());
		PayOrderResponseVO responseVO = payService.order(requestVO);

		List<TimerVO> timerList = paymentDao.timerList(paymentNo);
		// 반환 형태 생성
		PaymentInfoVO infoVO = new PaymentInfoVO();
		infoVO.setTimerVO(timerList);
		infoVO.setPaymentDto(paymentDto);
		infoVO.setPaymentDetailList(list);
		infoVO.setResponseVO(responseVO);
		return infoVO;
	}

	// 1. 전체취소(paymentNo)
	@Transactional
	@DeleteMapping("/cancelAll/{paymentNo}")
	public PayCancelResponseVO cancelAll(@PathVariable int paymentNo, @RequestHeader("Authorization") String token)
			throws URISyntaxException {
		PaymentDto paymentDto = paymentDao.selectOne(paymentNo);
		if (paymentDto == null)
			throw new TargetNotFoundException("존재하지 않는 결제정보");
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
		if (!paymentDto.getUserId().equals(claimVO.getUserId()))
			throw new TargetNotFoundException("소유자 불일치");
		if (paymentDto.getPaymentRemain() == 0)
			throw new TargetNotFoundException("이미 취소된 결제");

		// 남은금액 취소 요청
		PayCancelRequestVO request = new PayCancelRequestVO();
		request.setTid(paymentDto.getPaymentTid());
		request.setCancelAmount(paymentDto.getPaymentRemain());
		PayCancelResponseVO response = payService.cancel(request);
		// 잔여금액 0으로 변경
		paymentDao.cancelAll(paymentNo);
		// 관련항목의 상태를 취소로 변경
		paymentDao.cancelAllItem(paymentNo);

		return response;
	}

	// 2. 항목취소(paymentDetailNo)
	@DeleteMapping("/cancelItem/{paymentDetailNo}")
	public PayCancelResponseVO cancelItem(@RequestHeader("Authorization") String token,
			@PathVariable int paymentDetailNo) throws URISyntaxException {
		PaymentDetailDto paymentDetailDto = paymentDao.selectDetailOne(paymentDetailNo);
		if (paymentDetailDto == null)
			throw new TargetNotFoundException("존재하지 않는 결제정보");
		PaymentDto paymentDto = paymentDao.selectOne(paymentDetailDto.getPaymentDetailOrigin());
		if (paymentDto == null)
			throw new TargetNotFoundException("존재하지 않는 결제정보");
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
		if (!paymentDto.getUserId().equals(claimVO.getUserId()))
			throw new TargetNotFoundException("소유자 불일치");

		// 취소요청
		int money = paymentDetailDto.getPaymentDetailPrice() * paymentDetailDto.getPaymentDetailQty();
		PayCancelRequestVO request = new PayCancelRequestVO();
		request.setTid(paymentDto.getPaymentTid());
		request.setCancelAmount(money);
		PayCancelResponseVO response = payService.cancel(request);
		// 상태항목을 취소로 변경
		response.setItemName(paymentDetailDto.getPaymentDetailName());
		paymentDao.cancelItem(paymentDetailNo);
		// 항목금액 차감
		paymentDao.decreaseItemRemain(paymentDto.getPaymentNo(), money);
		return response;
	}

	// 정보 업데이트
	@PutMapping("/detailUpdate")
	public void update(@RequestBody PaymentDetailDto paymentDetailDto) {
		paymentDao.updatePaymentDetail(paymentDetailDto);
	}

	// 좌석과 항공편 정보를 조회하는 메서드
	@GetMapping("/flightInfoList")
	public List<SeatsFlightInfoVO> seatsFlightInfoList() {
		return sqlSession.selectList("payment.seatsFlightInfoList");
	}
	//이거에 맞는 결제페이지 만들어야 함
	@GetMapping("/payment/{id}/success/{status}")
    public String showSuccessPage(@PathVariable Long id, @PathVariable String status) {
        // 해당 로직 처리 후 뷰 반환
        return "payment/success";  // success.html 또는 다른 뷰 이름
    }
	
}
