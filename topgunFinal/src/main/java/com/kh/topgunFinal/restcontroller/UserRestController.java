package com.kh.topgunFinal.restcontroller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.topgunFinal.configuration.CustomCertProperties;
import com.kh.topgunFinal.dao.CertDao;
import com.kh.topgunFinal.dao.UserDao;
import com.kh.topgunFinal.dao.UserTokenDao;
import com.kh.topgunFinal.dto.AirlineDto;
import com.kh.topgunFinal.dto.CertDto;
import com.kh.topgunFinal.dto.MemberDto;
import com.kh.topgunFinal.dto.UserDto;
import com.kh.topgunFinal.dto.UserTokenDto;
import com.kh.topgunFinal.error.TargetNotFoundException;
import com.kh.topgunFinal.service.AttachmentService;
import com.kh.topgunFinal.service.TokenService;
import com.kh.topgunFinal.vo.ChangePasswordRequestVO;
import com.kh.topgunFinal.vo.DeleteUserRequestVo;
import com.kh.topgunFinal.vo.InfoResponseVO;
import com.kh.topgunFinal.vo.JoinRequestVO;
import com.kh.topgunFinal.vo.UserClaimVO;
import com.kh.topgunFinal.vo.UserComplexRequestVO;
import com.kh.topgunFinal.vo.UserComplexResponseVO;
import com.kh.topgunFinal.vo.UserLoginRequestVO;
import com.kh.topgunFinal.vo.UserLoginResponseVO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"})
@RestController
@RequestMapping("/users")
public class UserRestController {

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserTokenDao userTokenDao;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private AttachmentService attachmentService;

	@Autowired
	private CertDao certDao;

	@Autowired
	private CustomCertProperties customCertProperties;

	@PostMapping("/login")
	public UserLoginResponseVO login(@RequestBody UserLoginRequestVO requestVo) {
		
		// 회원조회
		UserDto userDto = userDao.selectOne(requestVo.getUsersId());
		if (userDto == null) {// 아이디 없음
			throw new TargetNotFoundException("아이디 없음");
		}

		boolean isValid = encoder.matches(requestVo.getUsersPw(), userDto.getUsersPassword());// 암호화O

		if (isValid) {
			UserLoginResponseVO response = new UserLoginResponseVO();
			response.setUsersId(userDto.getUsersId());
			response.setUsersType(userDto.getUsersType());
			response.setUsersName(userDto.getUsersName());
			UserClaimVO claimVO = new UserClaimVO();
			claimVO.setUserId(userDto.getUsersId());
			claimVO.setUserType(userDto.getUsersType());
			response.setAccessToken(tokenService.createAccessToken(claimVO));// 액세스토큰
			response.setRefreshToken(tokenService.createRefreshToken(claimVO));// 리프레시토큰
			return response;
		} else {// 로그인 실패
			throw new TargetNotFoundException("비밀번호 불일치");
		}
	}

	@PostMapping("/refresh")
	public UserLoginResponseVO refresh(@RequestHeader("Authorization") String refreshToken) {
		// [1] refreshToken이 없거나 Bearer로 시작하지 않으면 안됨
		if (refreshToken == null)
			throw new TargetNotFoundException("토큰 없음");
		if (tokenService.isBearerToken(refreshToken) == false)
			throw new TargetNotFoundException("Bearer 토큰 아님");

		// [2] 토큰에서 정보를 추출
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(refreshToken));
		if (claimVO.getUserId() == null)
			throw new TargetNotFoundException("아이디 없음");
		if (claimVO.getUserType() == null)
			throw new TargetNotFoundException("등급 없음");

		// [3] 토큰 발급 내역을 조회
		UserTokenDto userTokenDto = new UserTokenDto();
		userTokenDto.setTokenTarget(claimVO.getUserId());
		userTokenDto.setTokenValue(tokenService.removeBearer(refreshToken));
		UserTokenDto resultDto = userTokenDao.selectOne(userTokenDto);
		if (resultDto == null)// 발급내역이 없음
			throw new TargetNotFoundException("발급 내역이 없음");

		// [4] 기존의 리프시 토큰 삭제
		userTokenDao.delete(resultDto);

		// [5] 로그인 정보 재발급
		UserLoginResponseVO response = new UserLoginResponseVO();
		response.setUsersId(claimVO.getUserId());
		response.setUsersType(claimVO.getUserType());
		response.setAccessToken(tokenService.createAccessToken(claimVO));// 재발급
		response.setRefreshToken(tokenService.createRefreshToken(claimVO));// 재발급
		return response;
	}

	// 회원 본인의 정보를 반환하는 기능
	// - 아이디를 변경할 수 없도록 Authorization 헤더에서 정보를 읽어 조회한 뒤 반환
	@GetMapping("/find")
	public UserDto find(@RequestHeader("Authorization") String accessToken) {
		if (tokenService.isBearerToken(accessToken) == false)
			throw new TargetNotFoundException("유효하지 않은 토큰");
		UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(accessToken));

		UserDto userDto = userDao.selectOne(claimVO.getUserId());
		if (userDto == null)
			throw new TargetNotFoundException("존재하지 않는 회원");

		userDto.setUsersPassword(null);// 비밀번호 제거

		return userDto;
	}

	@PostMapping("/checkId")
	public ResponseEntity<Boolean> checkDuplicateUserId(@RequestParam String userId) {
		UserDto user = userDao.selectOne(userId);
		boolean isDuplicate = (user != null); // 사용자가 존재하면 중복
		return ResponseEntity.ok().body(isDuplicate);
	}

	// 회원 등록 기능
	@PostMapping("/join")
	public void join(@RequestBody JoinRequestVO vo) {

		// 기본 회원 정보 저장 객체 생성
		UserDto userDto = new UserDto();

		// 상세 회원 정보 저장 객체 생성
		MemberDto memberDto = new MemberDto();

		// 항공사 회원 정보 저장 객체 생성
		AirlineDto airlineDto = new AirlineDto();

		if (vo.getUsersType().equals("MEMBER")) {
			// MEMBER 유형에 대한 처리
			userDto.setUsersId(vo.getUsersId());
			userDto.setUsersPassword(vo.getUsersPassword());
			userDto.setUsersName(vo.getUsersName());
			userDto.setUsersType(vo.getUsersType());
			userDto.setUsersEmail(vo.getUsersEmail());
			userDto.setUsersContact(vo.getUsersContact());

			// ZonedDateTime으로 변환
			ZonedDateTime zonedDateTime = ZonedDateTime.parse(vo.getMemberBirth());

			// LocalDateTime으로 변환 (UTC 시간대에서 시스템 기본 시간대로 변환)
			LocalDateTime localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

			memberDto.setMemberBirth(localDateTime);
			memberDto.setMemberEngName(vo.getMemberEngName());
			memberDto.setMemberId(vo.getUsersId());
			memberDto.setMemberGender(vo.getMemberGender());

			userDao.insertMember(userDto, memberDto);
		} else if (vo.getUsersType().equals("AIRLINE")) {
			// AIRLINE 유형에 대한 처리
			userDto.setUsersId(vo.getUsersId());
			userDto.setUsersPassword(vo.getUsersPassword());
			userDto.setUsersName(vo.getUsersName());
			userDto.setUsersType(vo.getUsersType());
			userDto.setUsersEmail(vo.getUsersEmail());
			userDto.setUsersContact(vo.getUsersContact());

			airlineDto.setUserId(vo.getUsersId());
			airlineDto.setAirlineNo(vo.getAirlineNo());
			airlineDto.setAirlineName(vo.getAirlineName());

			userDao.insertAirLine(userDto, airlineDto);
		} else {
			return; // 사용자의 유형이 null일 경우 반환
		}

	}

	@PostMapping("/myInfo")
	public InfoResponseVO getMyInfo(@RequestBody UserClaimVO userClaim) {
		String userId = userClaim.getUserId();
		String userType = userClaim.getUserType();

		InfoResponseVO response = userDao.getMyInfo(userId, userType);

		return response;
	}

	// 정보 수정 + 정보변경시 비밀번호 인증
	@PutMapping("/update")
	public boolean updateUserInfo(@RequestBody InfoResponseVO infoVo, @RequestParam String authPassword) {
		UserDto user = userDao.selectOne(infoVo.getUsersId());
		// 유저 정보 있는지 확인
		if (user != null) {
			// 비밀번호 검증
			boolean isValid = encoder.matches(authPassword, user.getUsersPassword());
			// 인증 완료 시
			if (isValid) {
				return userDao.updateInfo(infoVo);
			} else {
				// 실패시
				return false;
			}
		} else {
			return false;
		}

	}

	// 이미지 찾기
	@PostMapping("/myImage")
	public int myImage(@RequestHeader("Authorization") String accessToken) {

		if (tokenService.isBearerToken(accessToken) == false)
			throw new TargetNotFoundException("유효하지 않은 토큰");

		try {
			UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(accessToken));
			int attachmentNo = userDao.findImage(claimVO.getUserId());
			return attachmentNo;
		} catch (Exception e) {
			return -1;
		}
	}

	// 프로필 이미지만 업로드하는 매핑
	@Transactional
	@PostMapping("/profile")
	public void profile(@RequestHeader("Authorization") String accessToken, @RequestParam MultipartFile attach)
			throws IllegalStateException, IOException {
		if (attach.isEmpty())
			return;

		// 아이디 추출
		String userId = tokenService.check(tokenService.removeBearer(accessToken)).getUserId();

		// 기존 이미지가 있다면 제거
		try {
			int beforeNo = userDao.findImage(userId);
			attachmentService.delete(beforeNo);
		} catch (Exception e) {
			// 예외 무시
		}

		// 신규 이미지 저장
		int attachmentNo = attachmentService.save(attach);

		// 아이디와 신규 이미지를 연결
		userDao.connect(userId, attachmentNo);
	}

	// 비밀번호 재설정 페이지
	@PostMapping("/resetPw")
	public void resetPw(@RequestBody CertDto certDto, @RequestParam String userId) {

		boolean isValid = certDao.check(certDto, customCertProperties.getExpire());
		if (!isValid) {
			throw new TargetNotFoundException("올바르지 않은 접근");
		}
	}

	@PutMapping("/changePassword")
	public boolean changePassword(@RequestBody ChangePasswordRequestVO vo) {

		// vo가 null이면 취소
		if (vo == null) {
			return false;
		}

		// 인증 정보 삭제
		certDao.delete(vo.getCertEmail());

		return userDao.changeUserPassword(vo);
	}

	@Transactional
	@DeleteMapping("/delete")
	public boolean deleteUser(@RequestBody DeleteUserRequestVo requestVo) {
		// vo가 null이면 애초에 거짓
		if (requestVo == null) {
			return false;
		}
		
		// 비밀번호 검증 후
		UserDto userDto = userDao.selectOne(requestVo.getUserId());
		boolean isValid = encoder.matches(requestVo.getDelPw(), userDto.getUsersPassword());

		if(!isValid) {
			return false;
		}
		
		try {
		    int beforeNo = userDao.findImage(userDto.getUsersId());

		    // 이미지가 존재하는 경우에만 삭제
		    if (beforeNo > 0) {
		        attachmentService.delete(beforeNo);
		    } else {
		    }
		} catch (Exception e) {
		    // 예외는 업로드때와 동일하게 무시
		}
		
		
		// 삭제 처리
		return userDao.deleteUser(requestVo);
	}
	

	@PostMapping("/list")
	public List<UserDto> usersList() {
		return userDao.selectList();
	}
	
	@PostMapping("/search")//회원가입과 구분하기 위해 눈물을 머금고 주소 규칙을 깬다
	public UserComplexResponseVO search(
					@RequestBody UserComplexRequestVO vo) {
		int count = userDao.complexSearchCount(vo);
		//마지막 = 페이징을 안쓰는 경우 or 검색개수가 종료번호보다 작거나 같은 경우
		boolean last = vo.getEndRow() == null || count <= vo.getEndRow();
		
		UserComplexResponseVO response = new UserComplexResponseVO();
		response.setUserList(userDao.complexSearch(vo));
		response.setCount(count);
		response.setLast(last);
		return response;
	}
	
}
