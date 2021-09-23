package com.jyh.board;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.jyh.board.model.Board;
import com.jyh.board.model.Navigator;
import com.jyh.board.model.User;
import com.jyh.board.service.BoardService;
import com.jyh.board.service.UserService;

@Controller
public class MobileController {

	@Autowired
	BoardService boardService;
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value="/mobile/list",method=RequestMethod.GET)
	public String listView() {
		return "mobile/list";
	}
	
	@RequestMapping(value="/mobile/list_proc",method=RequestMethod.GET)
	public ModelAndView list(String page) {
		int intPage=1;
		if(page!=null) {
			intPage=Integer.parseInt(page);
		}
		
		List<Board> boardList=boardService.getBoardList(intPage);
		Navigator navigator=boardService.getNavigator(intPage);
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("boardList",boardList);
		mav.addObject("nav",navigator);
		return mav;
	}
	
	@RequestMapping(value="/mobile/login",method=RequestMethod.GET)
	public String loginView() {
		return "mobile/login";
	}
	
	@RequestMapping(value= "/mobile/login_proc",method=RequestMethod.POST)
	public ModelAndView login(@RequestBody User user, HttpSession session) {
		User loginUser=userService.login(user); //로그인된 사용자 정보
		if(loginUser!=null) {	//로그인 성공했을 때
			session.setAttribute("user", loginUser);
			ModelAndView mav=new ModelAndView("jsonView");
			mav.addObject("result",true);
			mav.addObject("message","로그인이 성공하였습니다.");
			return mav;
		}
		else {
			session.setAttribute("user", loginUser);
			ModelAndView mav=new ModelAndView("jsonView");
			mav.addObject("result",false);
			mav.addObject("message", "아이디 또는 패스워드가 틀렸습니다");
			return mav;
		}
	}
	
	@RequestMapping(value= "/mobile/userinfo",method=RequestMethod.POST)
	public ModelAndView userinfo(HttpSession session) {
		User user= (User)session.getAttribute("user");
		//User user = new User();
		//user.setId(3);
		//user.setEmail("kmove");
		
		ModelAndView mav = new ModelAndView("jsonView");
		if(user==null) { //로그인이 안되어 있을 때
			mav.addObject("result",false);
		}
		else { //로그인이 되어 있을 때
			mav.addObject("result",true);
			mav.addObject("user",user);
		}
		return mav;
	}
	
	@RequestMapping(value="/mobile/write")
	public String writeView() {
		return "mobile/write";
	}
	
	
	@RequestMapping(value="/mobile/board/write",method=RequestMethod.POST)
	public ModelAndView boardWrite(@RequestBody Board board, HttpSession session) {
		ModelAndView mav = userinfo(session);
		User user=(User)mav.getModel().get("user");
		board.setUserId(user.getId());
		boardService.write(board);
		
		mav = new ModelAndView("jsonView");
		mav.addObject("result",true);
		mav.addObject("boardId",board.getId());
		return mav;
	}
	
	@RequestMapping(value="/mobile/view")
	public String view() {
		return "mobile/view";
	}
	
	@RequestMapping(value="/mobile/view",method=RequestMethod.POST)
	public ModelAndView boardView(@RequestBody Board board) {
		//int intId=Integer.parseInt(id);
		
		boardService.viewCountUp(board.getId());
		board=boardService.getBoard(board.getId());
		
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("board",board);
		return mav;
	}
	
	@RequestMapping(value="/mobile/remove",method=RequestMethod.POST)
	public ModelAndView remove(@RequestBody Board board,HttpSession session) {
		
		board=boardService.getBoard(board.getId());
		User user=(User)session.getAttribute("user");
		ModelAndView mav = new ModelAndView("jsonView");
		
		//로그인되지 않았거나 로그인 된 사용자의 게시물이 아닐 경우
		if(user==null||board.getUserId()!=user.getId()) { 
			mav.addObject("result",false);
			mav.addObject("message","삭제 권한이 없습니다");
			return mav;
		}
		boardService.delete(board.getId());
		mav.addObject("result",true);
		return mav;
	}
	
	@RequestMapping(value="/mobile/modify")
	public String modifyView() {
		return "mobile/modify";
	}
	
	@RequestMapping(value="/mobile/modify",method=RequestMethod.POST)
	public ModelAndView modify(@RequestBody Board board, HttpSession session) {
		Board tmpBoard=boardService.getBoard(board.getId());
		User user=(User)session.getAttribute("user");
		ModelAndView mav = new ModelAndView("jsonView");
		
		//로그인이 안 되어있거나 수정하려는 게시글이 로그인 된 사용자와 다른 경우
		if(user==null||tmpBoard.getUserId()!=user.getId()) {
			mav.addObject("result",false);
			mav.addObject("message","게시글을 수정할 권한이 없습니다.");
			return mav;
		}
		
		boardService.modify(board);
		mav.addObject("result",true);
		return mav;
	}
}
