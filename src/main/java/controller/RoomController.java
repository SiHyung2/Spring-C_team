package controller;


import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


import model.Booking;
import model.Business;
import model.Picture;
import model.Review;
import model.Room;
import repository.ReserveDao;
import repository.RoomDao;
import util.DateParse;

@Controller
@RequestMapping("/room/")
public class RoomController{
	
	private static Map<String, Object> map = new HashMap<String, Object>();

	HttpServletRequest request;
	Model model;
	HttpSession session;
	
	
	@Autowired
	RoomDao rd;
	
	@Autowired
	ReserveDao reserveDao;
	
	@ModelAttribute
	void init(HttpServletRequest request, Model model) {
		this.request = request;
		this.model = model;
		this.session = request.getSession();
	}

	
	// 객실정보 페이지
	
	@RequestMapping("roomlist")
	public String List() {
		
		String bu_email =(String)session.getAttribute("bu_email");
		Map<Integer, Object> map = new HashMap<>();
		// business 메일을 사용하는 사업자의 객실 리스트 저장
		List<Room> list = rd.roomList(bu_email);
		List<Picture> picList = new ArrayList<Picture>();
		for(Room room : list) {
			picList = rd.selectPic(room.getPic_num());
			map.put(room.getRo_num(), picList.get(0).getLocation().trim());
		}
		model.addAttribute("picMap", map);
		model.addAttribute("list", list);
		
		return "/view/entrepreneur/roomlist";
	}
	
	
	// 객실등록 페이지
	@RequestMapping("roomInsert")
	public String roomInsert() {
		
		
		return "/view/entrepreneur/roomInsert";
	}
	
	
	@RequestMapping("roomInsertPro")
	public String roomInsertPro(Room room) {

		characterEncoding();
		  
		String bu_email =(String)session.getAttribute("bu_email");
		 
		Picture p = new Picture();
		
		int roomNum = rd.nextRoNum();
		int picNum = rd.nextPicNum();
		
		room.setRo_num(roomNum);
		room.setBu_email(bu_email);
		room.setPic_num(picNum);
		
		String[] picList = room.getLocation().split("\\n");
		
		for (String pic : picList) {
			p = new Picture(picNum, pic.trim());
			rd.insertPicture(p);
		}
		
		// room객체에 저장된 값을 room table에 저장
		int rnum = rd.insertRoom(room);
		
		// p객체에 picture table에 저장
		
		String msg = "객실 등록시 오류가 발생했습니다.";
		String url = request.getContextPath() + "/room/roomInsert?bu_email="+bu_email;
		
		
		if(rnum > 0) {
			msg = "객실 등록이 완료되었습니다.";
			url = request.getContextPath() + "/room/roomlist?bu_email="+bu_email;
		}
		model.addAttribute("msg", msg);
		model.addAttribute("url", url);
		return "/view/alert";
		
	}
	
	
	@RequestMapping("roominfo")
	public String roominfo() {
		int ro_num = Integer.parseInt(request.getParameter("ro_num"));
		Room room = rd.selectRoom(ro_num);
		
		List<Picture> picList = rd.selectPic(room.getPic_num());
		List<String> p_list = new ArrayList<String>();
		
		for(int i=0; i<picList.size();i++) {
			p_list.add(picList.get(i).getLocation());
		}
		
		// 선택한 객실의 정보를 가져와서 저장
		
		String info = room.getRo_info().replace("\r\n", "<br/>");
		
		model.addAttribute("p_list", p_list);
		model.addAttribute("room", room);
		model.addAttribute("ro_num", ro_num);
		model.addAttribute("pic_num", room.getPic_num());
		model.addAttribute("info", info);
		
		return "/view/entrepreneur/roominfo";
	}
	
	
	@RequestMapping("roomUpdate")
	public String roomUpdate() {
		  
		int ro_num = Integer.parseInt(request.getParameter("ro_num"));
		int pic_num = Integer.parseInt(request.getParameter("pic_num"));
		
		String pic = "";
		
		Room room = rd.selectRoom(ro_num);
		List<Picture> piclist = rd.selectPic(pic_num);
		
		for(Picture p : piclist) {
			pic += p.getLocation()+"\n";
		}
		
		model.addAttribute("pic_num", pic_num);
		model.addAttribute("room", room);
		model.addAttribute("ro_num", ro_num);
		model.addAttribute("pic", pic);
		
		return "/view/entrepreneur/roomUpdate";
	}
	
	
	@RequestMapping("roomUpdatePro")
	public String roomUpdatPro(Room room) {
		
		characterEncoding();
		  
		String bu_email =(String)session.getAttribute("bu_email");
		 
		
		int ro_num = room.getRo_num();
		int pic_num = room.getPic_num();
		
		int p = rd.deleteLocation(pic_num);
		
		room.setBu_email(bu_email);
		
		String[] picList = room.getLocation().split("\n");
		Picture picLocation = null;
		int pic = 0;	
		for(String lo : picList) {
			picLocation = new Picture(pic_num,lo);
			pic = rd.insertPicture(picLocation);
		}
		
		// room객체에 저장된 값을 room table에 저장
		int rnum = rd.updateRoom(room);
		
		
		String msg = "객실 수정시 오류가 발생했습니다.";
		String url = request.getContextPath() + "/room/roomUpdate?ro_num="+ro_num+"&pic_num="+pic_num;
		
		
		if(rnum > 0 && pic > 0) {
			msg = "객실 수정이 완료되었습니다.";
			url = request.getContextPath() + "/room/roomlist?bu_email="+bu_email;
		}
		model.addAttribute("msg", msg);
		model.addAttribute("url", url);
		
		
		return "/view/alert";
	}
	
	
	@RequestMapping("roomDelete")
	public String roomDelete(Room room) {
		
		int ro_num = room.getRo_num();
		model.addAttribute("ro_num", ro_num);
		
		return "/view/entrepreneur/roomDelete";
	}
	
	
	@RequestMapping("roomDeletePro")
	public String roomDeletePro(Room r, Business bu) {
		
		
		String bu_email =(String)session.getAttribute("bu_email");
		
		String pwd = bu.getBu_password();
		int ro_num = r.getRo_num();
		map.clear();
		map.put("bu_email", bu_email);
		map.put("ro_num", ro_num);
		
		// 사업자 비밀번호 찾기
		Business business = rd.selectBu(bu_email);
		int room = 0;
		String msg = "객실 삭제시 오류가 발생했습니다.";
		String url = request.getContextPath() + "/room/roomDelete?ro_num="+ro_num;
		
		if(pwd == null || "".equals(pwd) || !pwd.equals(business.getBu_password())) {
			msg = "비밀번호가 틀렸습니다.";
		}else {
			// 비밀번호가 일치하면 객실 삭제
			room = rd.deleteRoom(map);
		}
		
		if(room > 0) {
			msg = "객실 삭제가 완료되었습니다.";
			url = request.getContextPath() + "/room/roomlist?bu_email="+bu_email;
		}
		model.addAttribute("msg", msg);
		model.addAttribute("url", url);
		
		
		return "/view/alert";
	}
	
	@RequestMapping("reservation")
	public String reservation() {
		
		
		 
		String bu_email =(String)session.getAttribute("bu_email");
		
				
		// 페이지 번호
		int pageInt;
		// 한페이지에 출력할 게시글 갯수
		int limit = 10;
		String pageNum;
		
		// pageNum을 세션에 저장해서 작업후 뒤로가기할때 바로전에 보던 페이지 출력
		if(request.getParameter("pageNum") != null){
			session.setAttribute("pageNum", request.getParameter("pageNum"));
		}
		pageNum = (String) session.getAttribute("pageNum");
		if(pageNum == null)
			pageNum = "1";
				
		pageInt = Integer.parseInt(pageNum);
				
		// 한페이지에 출력할 게시글 rownum의 번호
		int startPage = (pageInt-1)*limit + 1;
		int endPage = (pageInt-1)*limit + limit;
//		게시글 갯수
		int count = 0;
		
		// =========== 현재 시간 ==============
		LocalDate now = LocalDate.now();
		// 포맷 정의
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		// 포맷 적용
		String nowDay = now.format(formatter);
		
		map.clear();
		map.put("startPage", startPage);
		map.put("endPage", endPage);
		map.put("nowDay", nowDay);
		map.put("bu_email", bu_email);
		
		// 예약 내역 찾기
		String searchName = request.getParameter("searchName");
		String search = request.getParameter("search");
		List<Booking> bk = new ArrayList<Booking>();
		
		System.out.println("searchName : " + searchName);
		System.out.println("search : " + search);
//		검색할 컬럼이름
		map.put("searchName", searchName);
//		검색할 컬럼 값
		map.put("search", search);
		
		if("".equals(searchName) || searchName == null || search == null || "".equals(search)) {
			bk = rd.selectBkList(map);
			count = rd.countBoard(map);
		}
		else if("status".equals(searchName)) {
			if("예약완료".equals(search)) {
				map.put("status", "1");
			}
			else if("결제취소".equals(search)) {
				map.put("status", "2");
			}
			else if("이용완료".equals(search)) {
				map.put("status", "3");
			}
			else if("입실완료".equals(search)) {
				map.put("status", "4");
			}
			else {
				String msg = "예약완료, 결제취소, 이용완료 , 입실완료중 하나를 입력하세요.";
				String url = request.getContextPath()+"/room/reservation";
				model.addAttribute("msg", msg);
				model.addAttribute("url", url);
				return "/view/alert";
			}
			bk = rd.searchStatus(map);
			count = rd.countBoardStatus(map);
		}
		else {
			bk = rd.searchName(map);
			count = rd.countBoardSearchName(map);
		}
		
		System.out.println("bk : " + bk);
		
		// -----------------------------------------------------------------------------
		// 게시글 갯수를 확인하는 메서드
		
		int boardNum = count - (pageInt - 1) *limit;
		
		int bottomLine = 3;
		int startNum = (pageInt - 1) / bottomLine * bottomLine + 1;
		int endNum = startNum + bottomLine - 1;
		
		int maxNum = (count / limit) + (count % limit == 0 ? 0 : 1);
		if(endNum >= maxNum){
			endNum = maxNum;
		}
		
		model.addAttribute("search", search);
		model.addAttribute("searchName", searchName);
		model.addAttribute("bk", bk);
		model.addAttribute("boardNum", boardNum);
		model.addAttribute("bottomLine", bottomLine);
		model.addAttribute("startNum", startNum);
		model.addAttribute("endNum", endNum);
		model.addAttribute("maxNum", maxNum);
		model.addAttribute("pageInt", pageInt);
		
		return "/view/entrepreneur/reservation";
	}
	
	@RequestMapping("sales")
	public String sales() {
	
		
		
		String bu_email =(String)session.getAttribute("bu_email");
		String[] month = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
		map.clear();
		map.put("bu_email", bu_email);
		String result = "";
		
		for(String mon : month) {
			map.put("mon", mon);
			Booking bo = rd.selectSales(map);
			System.out.println("bo : "+bo);
			if(result!="") { 
				result += ","; 
			}

			if(bo == null) {
				result += "['"+mon+"월', "+"0"+"]";

			}
			else {
				result += "['"+mon+"월', "+bo.getPrice()+"]";
			}
		}
		
		model.addAttribute("result", result);

		return "/view/entrepreneur/sales";
	}
	
	@RequestMapping("areaSales")
	public String areaSales() {
		
		map.clear();
		
		String[] areas = {"서울", "경기", "강원", "부산"};
		String month = request.getParameter("month");
		
		if(month == null) {
			LocalDate now = LocalDate.now();
			int month1 = now.getMonthValue();
			month = "0"+month1;
		}
		
		String result = "";
		map.put("month", month);
		for(String area : areas) {
			map.put("area", area);
			Booking bo = rd.selectAreaSales(map);
			if(result!="") { 
				result += ","; 
			}
			if(bo == null) {
				result += "['"+area+"', "+"0"+"]";
			}
			else {
				result += "['"+area+"', "+bo.getPrice()+"]";
			}
		}
		
		model.addAttribute("month", month);
		model.addAttribute("result", result);
		
		return "/view/entrepreneur/areaSales";
	}
	
	
	
	
	
	@RequestMapping("todayCheckin")
	public String todayCheckin() {
		
		
		  
		String bu_email =(String)session.getAttribute("bu_email");
		DateParse dp = new DateParse();
		String checkin = dp.getTodayPlus(0);
		
		map.clear();
		map.put("bu_email", bu_email);
		map.put("checkin", checkin);
		
		List<Booking> notCheckin = rd.selectNotCheckin(map);
		List<Booking> checkinOk = rd.selectcheckinOk(map);
		
		System.out.println("bu_email = " + bu_email);
		System.out.println("checkin = "+ checkin);
		System.out.println("notCheckin = "+ notCheckin);
		System.out.println("checkinOk = " + checkinOk);
		
		model.addAttribute("notCheckin", notCheckin);
		model.addAttribute("checkinOk", checkinOk);
		
		return "/view/entrepreneur/todayCheckin";
	}
	
	
	@RequestMapping("updateTodayCheckin")
	public String updateTodayCheckin() {
		
		
		DateParse dp = new DateParse();
		String checkin = dp.getTodayPlus(0);
		String bu_email =(String)session.getAttribute("bu_email");
		String bo_num = request.getParameter("bo_num");
		
		map.clear();
		map.put("bu_email", bu_email);
		map.put("bo_num", bo_num);
		map.put("checkin", checkin);
		
		int rowCnt = rd.updateTodayCheckin(map);
		
		return "redirect:/room/todayCheckin";
	}
	
	
	@RequestMapping("todayCheckOut")
	public String todayCheckOut() {
		
		
		  
		String bu_email =(String)session.getAttribute("bu_email");
		DateParse dp = new DateParse();
		String checkout = dp.getTodayPlus(0);
		
		map.clear();
		map.put("bu_email", bu_email);
		map.put("checkout", checkout);
		
		List<Booking> notCheckOut = rd.selectNotCheckOut(map);
		
		List<Booking> checkOutOk = rd.selectcheckOutOk(map);
		
		
		System.out.println("checkout = "+checkout);
		System.out.println("bu_email = "+bu_email);
		System.out.println("notCheckOut = "+notCheckOut);
		System.out.println("checkOutOk = "+checkOutOk);
		
		model.addAttribute("notCheckOut", notCheckOut);
		model.addAttribute("checkOutOk", checkOutOk);
		
		return "/view/entrepreneur/todayCheckOut";
	}
	
	
	@RequestMapping("updateTodayCheckOut")
	public String updateTodayCheckOut() {
		
		
		DateParse dp = new DateParse(); 
		String bu_email =(String)session.getAttribute("bu_email");
		String bo_num = request.getParameter("bo_num");
		String checkout = dp.getTodayPlus(0);
		System.out.println("bo_num"+bo_num);
		
		map.clear();
		map.put("bu_email", bu_email);
		map.put("bo_num", bo_num);
		map.put("checkout", checkout);
		
		int rowCnt = rd.updateAndDeleteTodayCheckOut(map);
		
		return "redirect:/room/todayCheckOut";
	}
	
	@RequestMapping("roomReview")
	public String roomReview() {
		
		String bu_email = (String)session.getAttribute("bu_email");
		List<Review> reviewList = reserveDao.businessReviewList(bu_email);
		model.addAttribute("reviewList", reviewList);
		return "/view/entrepreneur/roomReview";
	}
	
	
	
	
	private void characterEncoding() {
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}

