package com.kh.topgunFinal.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.topgunFinal.dao.RoomMessageDao;
import com.kh.topgunFinal.error.TargetNotFoundException;
import com.kh.topgunFinal.service.TokenService;
import com.kh.topgunFinal.vo.UserClaimVO;
import com.kh.topgunFinal.vo.WebsocketMessageMoreVO;
import com.kh.topgunFinal.vo.WebsocketMessageVO;

@CrossOrigin(origins = {"https://topguntravel.shop", "https://www.topguntravel.shop"})
@RestController
@RequestMapping("/room/{roomNo}")
public class WebsocketRestController {
	
	@Autowired
	private RoomMessageDao roomMessageDao;
	@Autowired
	private TokenService tokenService;
	
	@GetMapping("/more/{firstMessageNo}")
	public WebsocketMessageMoreVO more(@PathVariable int firstMessageNo, @PathVariable int roomNo,
												@RequestHeader(value="Authorization", required = false)String token) {
		String usersId = null;
		if(token != null) {
			UserClaimVO claimVO = tokenService.check(tokenService.removeBearer(token));
			usersId = claimVO.getUserId();
		}
		
		//사용자에게 보여줄 목록 조회
		List<WebsocketMessageVO> messageList = 
				roomMessageDao.selectListMemberComplete(usersId, 1, 100, firstMessageNo, roomNo);
		if(messageList.isEmpty()) throw new TargetNotFoundException("보여줄 메세지 없음");
		
		//남은 목록이 더 있는지 확인
		List<WebsocketMessageVO> prevMessageList = 
				roomMessageDao.selectListMemberComplete(usersId, 1, 100, messageList.get(0).getNo(), roomNo);
		
		WebsocketMessageMoreVO moreVO = new WebsocketMessageMoreVO();
		moreVO.setMessageList(messageList);
		moreVO.setLast(prevMessageList.isEmpty());
		
		return moreVO;
	}

}
