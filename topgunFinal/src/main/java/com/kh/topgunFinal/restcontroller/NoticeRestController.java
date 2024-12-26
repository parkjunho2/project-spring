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

import com.kh.topgunFinal.dao.NoticeDao;
import com.kh.topgunFinal.dto.NoticeDto;
import com.kh.topgunFinal.error.TargetNotFoundException;

@CrossOrigin(origins = {"https://topguntravel.shop", "https://www.topguntravel.shop"})
@RestController
@RequestMapping("/notice")
public class NoticeRestController {

    @Autowired
    private NoticeDao noticeDao;

    // 공지사항 등록
    @PostMapping("/post") // Create (등록) - 200, 500
    public void insert(@RequestBody NoticeDto noticeDto) {
        noticeDto.setNoticeId(noticeDao.getNextSequence());
        noticeDao.insert(noticeDto);
    }

    // 공지사항 목록 조회
    @GetMapping("/") // Read (목록) - 200, 500
    public List<NoticeDto> list() {
        return noticeDao.selectList();
    }

    // 단일 공지사항 조회
    @GetMapping("/{noticeId}") // Read (상세) - 200, 404, 500
    public NoticeDto detail(@PathVariable int noticeId) {
        NoticeDto noticeDto = noticeDao.selectOne(noticeId);
        if (noticeDto == null) {
            throw new TargetNotFoundException();
        }
        return noticeDto;
    }

 // 공지사항 수정
    @PutMapping("/edit/{noticeId}") // Update (수정) - 200, 404, 500
    public void update(@PathVariable int noticeId, @RequestBody NoticeDto noticeDto) {
        noticeDto.setNoticeId(noticeId); // ID를 noticeDto에 설정
        boolean result = noticeDao.update(noticeDto);
        if (!result) {
            throw new TargetNotFoundException();
        }
    }


    // 공지사항 삭제
    @DeleteMapping("/delete/{noticeId}") // Delete (삭제) - 200, 404, 500
    public void delete(@PathVariable int noticeId) {
        boolean result = noticeDao.delete(noticeId);
        if (!result) {
            throw new TargetNotFoundException();
        }
    }
}