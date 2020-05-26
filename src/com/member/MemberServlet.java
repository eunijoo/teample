package com.member;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.notice.NoticeDTO;
import com.util.FileManager;
import com.util.MyUploadServlet;
import com.util.MyUtil;

@MultipartConfig
@WebServlet("/member/*")

public class MemberServlet extends MyUploadServlet {
	private static final long serialVersionUID = 1L;
	
	private String pathname;


	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
				
		HttpSession session=req.getSession();
		
		// �̹����� ������ ���(pathname)
		String root = session.getServletContext().getRealPath("/");
     	pathname = root + "uploads" + File.separator + "travel";
		
		
		String uri=req.getRequestURI();
		if(uri.indexOf("login.do")!=-1) {
			loginForm(req, resp);
		} else if(uri.indexOf("login_ok.do")!=-1) {
			loginSubmit(req, resp);
		} else if(uri.indexOf("logout.do")!=-1) {
			logout(req, resp);
		} else if(uri.indexOf("member.do")!=-1) {
			memberForm(req, resp);
		} else if(uri.indexOf("member_ok.do")!=-1) {
			memberSubmit(req, resp);
		} else if(uri.indexOf("pwd.do")!=-1) {
			pwdForm(req, resp);
		} else if(uri.indexOf("pwd_ok.do")!=-1) {
			pwdSubmit(req, resp);
		} else if(uri.indexOf("update.do")!=-1) {
			updateForm(req,resp);
		} else if(uri.indexOf("update_ok.do")!=-1) {
			updateSubmit(req, resp);
		} else if(uri.indexOf("delete.do")!=-1) {
			deleteSubmit(req, resp);
		} else if(uri.indexOf("myPage.do")!=-1) {
			myPage(req, resp);
		} else if(uri.indexOf("list.do")!=-1) {
			listForm(req, resp);
		}
	}
//�α��� ��
	private void loginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		String path="/WEB-INF/views/member/login.jsp";
		forward(req, resp, path);
	}
	
//�α��� ����	
	private void loginSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cp=req.getContextPath();
		
		MemberDAO dao=new MemberDAO();
		
		String userId=req.getParameter("userId");
		String userPwd=req.getParameter("userPwd");
		
		MemberDTO dto=dao.readMember(userId);
		
		if(dto==null || !dto.getUserPwd().equals(userPwd)) {
			String s="���̵� �Ǵ� �н����尡 ��ġ���� �ʽ��ϴ�. �ٽ� �Է����ּ���.";
			req.setAttribute("messege", s);
			forward(req, resp, "/WEB-INF/views/member/login.jsp");
			return;
		}
		//�α��� ����
		HttpSession session = req.getSession();
		
		//���� �����ð� 30��
		session.setMaxInactiveInterval(30*60);
		
		//���ǿ� �α��� ���� ����
		SessionInfo info=new SessionInfo();
		info.setUserId(dto.getUserId());
		info.setUserName(dto.getUserName());
		
		session.setAttribute("member", info);
	
		resp.sendRedirect(cp);//ùȭ������ ���ư��󤿤���������
	}
	
//�α׾ƿ�
	private void logout(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cp=req.getContextPath();
		HttpSession session=req.getSession();
		
		session.invalidate();
		
		resp.sendRedirect(cp);
	}
	
//ȸ��������	
	private void memberForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("title", "Sign up");
		req.setAttribute("mode", "created");
		
		forward(req, resp, "/WEB-INF/views/member/member.jsp");
	}
	
//ȸ������ �Ϸ�
	private void memberSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MemberDAO dao=new MemberDAO();
		MemberDTO dto = new MemberDTO();
		String cp=req.getContextPath();
		
		dto.setUserId(req.getParameter("userId"));
		dto.setUserPwd(req.getParameter("userPwd"));
		dto.setUserName(req.getParameter("userName"));
		
		String tel1 = req.getParameter("tel1");
		String tel2 = req.getParameter("tel2");
		String tel3 = req.getParameter("tel3");	
		if (tel1.length() != 0 && tel2.length() != 0 && tel3.length() != 0) {
			dto.setUserTel(tel1 + "-" + tel2 + "-" + tel3);
		}
		
		String email1 =req.getParameter("email1");
		String email2 =req.getParameter("email2");		
		if (email1.length() != 0 && email2.length() != 0) {
			dto.setUserEmail(email1 + "@" + email2);
		}
		
		dto.setUserBirth(req.getParameter("userBirth"));
	
		Part p = req.getPart("upload"); 
		Map<String, String> map = doFileUpload(p,pathname);
		  
		 // map�� null�̸� �������� ������ ���� �������� 
		if(map!=null) { 
			String saveFilename = map.get("saveFilename");
			if(saveFilename!=null) {
				dto.setImageFilename(saveFilename);
			}	  
		}
		
		try {
			dao.insertMember(dto);
		}catch(Exception e){
			String message = "ȸ�� ������ ���� �߽��ϴ�.";
						
			req.setAttribute("title", "Sign up");
			req.setAttribute("mode", "created");	
			req.setAttribute("message", message);
			forward(req, resp, "/WEB-INF/views/member/member.jsp");
					  
			return;
		}
		
		resp.sendRedirect(cp+"/member/main.do");
	}		 

		
// �н����� Ȯ�� ��	
	private void pwdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		HttpSession session=req.getSession();
		String cp=req.getContextPath();
				
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		// �α׾ƿ������̸�
		if(info==null) {
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
				
		String mode=req.getParameter("mode");
		if(mode.equals("update")) {
			req.setAttribute("title", "ȸ�� ���� ����");
		}else {
			req.setAttribute("title", "ȸ�� Ż��");
		}
		
		req.setAttribute("mode", mode);
		forward(req, resp, "/WEB-INF/views/member/pwd.jsp");	
	}
	
//�н�����Ȯ��	
	private void pwdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session=req.getSession();
		String cp=req.getContextPath();
		MemberDAO dao=new MemberDAO();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //�α׾ƿ� �� ���
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// DB���� �ش� ȸ�� ���� ��������
		MemberDTO dto=dao.readMember(info.getUserId());
		if(dto==null) {
			session.invalidate();
			resp.sendRedirect(cp);
			return;
		}
		String userPwd=req.getParameter("userPwd");
		String mode=req.getParameter("mode");
		
		if(! dto.getUserPwd().equals(userPwd)) {
			if(mode.equals("update")) {
				req.setAttribute("title", "ȸ�� ���� ����");
			}else {
				req.setAttribute("title", "ȸ�� Ż��");
			}
			req.setAttribute("mode", mode);
			req.setAttribute("message","<span style='color:red;'>�н����尡 ��ġ���� �ʽ��ϴ�.</span>");
			forward(req, resp, "/WEB-INF/views/member/pwd.jsp");
			return;
		}else {
			myPage(req,resp);
		}
		
		if(mode.equals("delete")) {
			// ȸ��Ż��
			try {
				dao.deleteMember(info.getUserId());
			} catch (Exception e) {
			}
			
			session.removeAttribute("member");
			session.invalidate();
			
			resp.sendRedirect(cp);
			
			return;
		}
	}	
	
//ȸ����������	
	private void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//���� �̿��ؼ� ȸ������ �˻��ϱ� (DAO)
		// ȸ���������� - ȸ������������ �̵�
		HttpSession session=req.getSession();
		MemberDAO dao=new MemberDAO();
		String cp=req.getContextPath();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		
		if(info==null) { //�α׾ƿ� �� ���
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// DB���� �ش� ȸ�� ���� ��������
		MemberDTO dto=dao.readMember(info.getUserId());
		if(dto==null) {
			session.invalidate();
			resp.sendRedirect(cp);
			return;
		}
			
		req.setAttribute("title", "ȸ�� ���� ����");
		req.setAttribute("dto", dto);
		req.setAttribute("mode", "update");

		forward(req, resp, "/WEB-INF/views/member/member.jsp");
	}
	
	
//ȸ������ �����Ϸ�	
	private void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session=req.getSession();
		String cp=req.getContextPath();
		MemberDAO dao=new MemberDAO();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //�α׾ƿ� �� ���
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		MemberDTO dto = new MemberDTO();
	
		dto.setUserId(req.getParameter("userId"));
		dto.setUserPwd(req.getParameter("userPwd"));
		dto.setUserName(req.getParameter("userName"));
		String tel1 = req.getParameter("tel1");
		String tel2 = req.getParameter("tel2");
		String tel3 = req.getParameter("tel3");
		if (tel1.length() != 0 && tel2.length() != 0 && tel3.length() != 0) {
			dto.setUserTel(tel1 + "-" + tel2 + "-" + tel3);
		}
		String email1 =req.getParameter("email1");
		String email2 =req.getParameter("email2");
		if (email1.length() != 0 && email2.length() != 0) {
			dto.setUserEmail(email1 + "@" + email2);
		}
		dto.setUserBirth(req.getParameter("userBirth"));

	//����	

		dto.setUserId(info.getUserId());
		
		Part p =req.getPart("upload");
		Map<String, String> map = doFileUpload(p, pathname);
		if(map!=null) {
			// ���� ���� ����
			if(req.getParameter("imageFilename").length()!=0) {
				FileManager.doFiledelete(pathname, req.getParameter("imageFilename"));
			}
			
			//���ο� ����
			String saveFilename = map.get("saveFilename");
			dto.setImageFilename(saveFilename);
		}
		
		try {
			dao.updateMember(dto);
			resp.sendRedirect(cp+"/member/myPage.do");
		} catch (Exception e) {
			String message = "ȸ�� ������ ���� �߽��ϴ�.";
			
			dto=dao.readMember(info.getUserId());
			if(dto==null) {
				session.invalidate();
				resp.sendRedirect(cp);
				return;
			}
				
			req.setAttribute("title", "ȸ�� ���� ����");
			req.setAttribute("dto", dto);
			req.setAttribute("mode", "update");
			req.setAttribute("message", message);
			
			forward(req, resp, "/WEB-INF/views/member/member.jsp");
		}
	}
//ȸ��Ż��	
	private void deleteSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session=req.getSession();
		MemberDAO dao=new MemberDAO();
		String cp=req.getContextPath();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //�α׾ƿ� �� ���
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// DB���� �ش� ȸ�� ���� ��������
		MemberDTO dto=dao.readMember(info.getUserId());
		if(dto==null) {
			session.invalidate();
			resp.sendRedirect(cp);
			return;
		}
			try {
				dao.deleteMember(info.getUserId());
			} catch (Exception e) {
			}
			
			session.removeAttribute("member");
			session.invalidate();
			
			resp.sendRedirect(cp);
			
			return;
		//}
	}
//myPage(������ ��)	
	private void myPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session=req.getSession();
		MemberDAO dao=new MemberDAO();
		String cp = req.getContextPath();
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //�α׾ƿ� �� ���
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		// DB���� �ش� ȸ�� ���� ��������
		MemberDTO dto = dao.readMember(info.getUserId());
		
		if(dto==null) {
			session.invalidate();
			resp.sendRedirect(cp);
			return;
		}
		
		req.setAttribute("title", "MyPage");
		 
		req.setAttribute("dto", dto);	

		forward(req, resp, "/WEB-INF/views/member/myPage.jsp");
	}
//ȸ������Ʈ
	private void listForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		HttpSession session=req.getSession();
		MemberDAO dao = new MemberDAO();
		String cp=req.getContextPath();
		MyUtil myUtil=new MyUtil();
		
		SessionInfo info=(SessionInfo)session.getAttribute("member");
		if(info==null) { //�α׾ƿ� �� ���
			resp.sendRedirect(cp+"/member/login.do");
			return;
		}
		
		String page=req.getParameter("page");
		int current_page=1;
		if(page!=null) {
			current_page=Integer.parseInt(page);
		}
	
		String condition=req.getParameter("condition");
		String keyword=req.getParameter("keyword");
		if(condition==null) {  //condition�� null�϶� keyword�� �˻��� �ƴ�...
			condition="userId";
			keyword="";
		} 
		//�˻���ư ������ Post�� �Ѿ��
		
		//���ڵ��� �ؾ��ϴϱ� ���ڵ��� ����
		
		if(req.getMethod().equalsIgnoreCase("GET")) {
			keyword=URLDecoder.decode(keyword,"utf-8");
		}
		
		int dataCount;
		if(keyword.length()==0) {
			dataCount=dao.dataCount();
		}else {
			dataCount=dao.dataCount(condition,keyword);
		}
		
		int rows=10;
		int total_page=myUtil.pageCount(rows, dataCount);
		if(current_page>total_page) {
			current_page=total_page;
		}
		
		int offset = (current_page - 1) * rows;
	
		List<MemberDTO> list;
		if (keyword.length() != 0)
			list = dao.listBoard(offset, rows, condition, keyword);
		else
			list = dao.listBoard(offset, rows);
		
		// �Խù� ��ȣ�� ������, �������� �����Ǹ� �߱������̶�
		String query="";
		if(keyword.length()!=0) {
			query="condition="+condition+"&keyword="+URLEncoder.encode(keyword,"utf-8");
		}
		//����¡ó��
		String listUrl=cp+"/member/list.do";
		if (query.length() != 0) {
			listUrl += "?" + query;
		}
		
		String paging = myUtil.paging(current_page, total_page, listUrl);
		
		// DB���� �ش� ȸ�� ���� ��������
		MemberDTO dto=dao.readMember(info.getUserId());
		if(dto==null) {
			session.invalidate();
			resp.sendRedirect(cp);
			return;
		}
		
		//list.jsp�� �Ѱ��� ������
		
		req.setAttribute("dto", dto);	
		req.setAttribute("list", list);
		req.setAttribute("paging", paging);
		req.setAttribute("page", current_page);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("total_page", total_page);
		req.setAttribute("condition", condition);
		req.setAttribute("keyword", keyword);
		
		forward(req, resp, "/WEB-INF/views/member/list.jsp");
	}
	
}
