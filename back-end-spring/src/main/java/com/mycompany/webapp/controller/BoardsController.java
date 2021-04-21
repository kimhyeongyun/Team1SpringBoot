package com.mycompany.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardsService;

// restful 컨트롤러는 @Controller 컨트롤러를 안쓰고 대신에
@RestController
// RestController 이렇게 되면 return이 view로 되는게 아니라 JSON 타입으로 리턴됨
@RequestMapping("/boards")
public class BoardsController {
	private final Logger logger = LoggerFactory.getLogger(BoardsController.class);

	@Autowired
	private BoardsService boardsService;

	@GetMapping("") // "/boards" 이렇게 요청했을때 실행한다라는 말
	// 기본값일 경우 1을 주기
	public Map<String, Object> list(@RequestParam(defaultValue = "1") int pageNo) {
		int totalRows = boardsService.getCount();
		Pager pager = new Pager(5, 5, totalRows, pageNo);
		List<Board> list = boardsService.getList(pager);
		Map<String, Object> map = new HashMap<>();
		map.put("pager", pager);
		map.put("boards", list);

		return map;
		// 이게 차이점. 원래 스프링프레임웍에서는 view "boardlist"가 와야
		// json이 되는지 확인 [{key : value}] 형식으로 나오는 지 확인 검사->네크워크->클릭-> / postman으로 확인하면
		// 더편함.
		// 새로운 클라스가 생기면 재시작을 해라.
		// tomcat 따로 설정할 필요도 없이 제티를 통해 실행.
	}

	// 생성-게시물저장
	@PostMapping("") // get은 정보를 가져올때 , post는 정보를 전달할때
	public Board create(Board board) { // 리턴 타입이 void인 이유: 응답이 없다는 뜻이 아니라 이거 처리를 다하면 보내줄 데이터는 없지만
		// 이걸로 result, success한 응답이 간다는 뜻.
		// 하지만 스프링프레임워크는 무조건 응답을 제시해야해요.
		// 좀더 정확히 말하자면 @Controller가 annotation으로 들어가면 응답이 들어가야합니다.
		logger.info(board.getBtitle());
		logger.info(board.getBcontent());
		logger.info(board.getBwriter());
		if (board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("D:/MyProject/uploadfiles/" + board.getBattachsname());
				mf.transferTo(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boardsService.insert(board);
		board.setBattach(null); // JSON은 문자열이기 때문에 거기에 데이터가 들어갈 수가 없어요.
		return board;
	}

	@GetMapping("/{bno}") // 뭔가를 달라고 하는 것, 경로상에 bno를 남기는 것, 앞에 boards 생략
	public Board read(@PathVariable int bno) { // int bno가 안되는 이유: get방식 ?bno=3 or post방식으로 body에 bno=3 // @PathVariable - 이거 넣어주기
		boardsService.addHitcount(bno);
		Board board = boardsService.getBoard(bno);
		return board;
	}

	@GetMapping("/battach/{bno}")
	public void download(@PathVariable int bno, HttpServletResponse response) {
		try {
			Board board = boardsService.getBoard(bno);
			String battachoname = board.getBattachoname();
			if (battachoname == null)
				return;
			battachoname = new String(battachoname.getBytes("UTF-8"), "ISO-8859-1");
			String battachsname = board.getBattachsname();
			String battachspath = "D:/MyProject/uploadfiles/" + battachsname;
			String battachtype = board.getBattachtype();

			response.setHeader("Content-Disposition", "attachment; filename=\"" + battachoname + "\";");
			response.setContentType(battachtype);

			InputStream is = new FileInputStream(battachspath);
			OutputStream os = response.getOutputStream();
			FileCopyUtils.copy(is, os);
			is.close();
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PutMapping("")
	public Board update(Board board) {
		if (board.getBattach() != null && !board.getBattach().isEmpty()) {
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachsname(new Date().getTime() + "-" + mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				File file = new File("D:/MyProject/uploadfiles/" + board.getBattachsname());
				mf.transferTo(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boardsService.update(board);
		board.setBattach(null);

		return board;
	}

	@DeleteMapping("{bno}")
	public void delete(@PathVariable int bno) {
		boardsService.delete(bno);
	}
}
