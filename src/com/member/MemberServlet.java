package com.member;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.util.FileManager;
import com.util.MyUploadServlet;

@MultipartConfig
@WebServlet("/member/*")

public class MemberServlet extends MyUploadServlet {
	private static final long serialVersionUID = 1L;
	
	private String pathname;


	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
				
		HttpSession session=req.getSession();
		
		// �̹����� ������ ���(pathname)
		String root=session.getServletContext().getRealPath("/");
		pathname=root+File.separator+"uploads"+File.separator+"photo";
		
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
			delete(req, resp);
		} else if(uri.indexOf("myPage.do")!=-1) {
			myPage(req, resp);
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
	
//ȸ�����԰���	
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
		dto.setImageFilename(req.getParameter("imageFilename"));
		
		try {
			dao.insertMember(dto);
		} catch (Exception e) {
			String message = "ȸ�� ������ ���� �߽��ϴ�.";
			
			req.setAttribute("title", "Sign up");
			req.setAttribute("mode", "created");
			req.setAttribute("message", message);
			forward(req, resp, "/WEB-INF/views/member/member.jsp");
			return;
		}
		
		
		  Part p = req.getPart("upload"); 
		  Map<String, String> map = doFileUpload(p,pathname);
		  
		  // map�� null�̸� �������� ������ ���� �������� 
		  if(map!=null) { 
		  String saveFilename = map.get("saveFilename");
		  String originalFilename = map.get("originalFilename");
		  long fileSize = p.getSize();
		  
		  dto.setSaveFilename(saveFilename); 
		  dto.setOriginalFilename(originalFilename);
		  dto.setFilesize(fileSize); 
		  
		  
		  }
		  resp.sendRedirect(cp+"/member/main.do");
	}
		 

		/*
		 * String filename=null; Part p = req.getPart("upload"); Map<String, String> map
		 * = doFileUpload(p, pathname); if(map != null) { filename =
		 * map.get("saveFilename"); } if(filename!=null) {
		 * dto.setImageFilename(filename); }
		 */
		
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
			req.setAttribute("message", "<span style='color:red;'>�н����尡 ��ġ���� �ʽ��ϴ�.</span>");
			forward(req, resp, "/WEB-INF/views/member/pwd.jsp");
			return;
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
		resp.sendRedirect(cp+"/member/update.do");
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
		dto.setUserPwd(req.getParameter("userName"));
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
		if(req.getParameter("filesize")!=null) {
			dto.setFilesize(Long.parseLong(req.getParameter("filesize")));			
		}
		
		dto.setUserId(info.getUserId());
		
		Part p =req.getPart("upload");
		Map<String, String> map = doFileUpload(p, pathname);
		if(map!=null) {
			// ���� ���� ����
			if(req.getParameter("saveFilename").length()!=0) {
				FileManager.doFiledelete(pathname, req.getParameter("saveFilename"));
			}
			
			//���ο� ����
			String saveFilename = map.get("saveFilename");
			String originalFilename = map.get("originalFilename");
			long size = p.getSize();
			dto.setSaveFilename(saveFilename);
			dto.setOriginalFilename(originalFilename);
			dto.setFilesize(size);
		}
		
		try {
			dao.updateMember(dto);
		} catch (Exception e) {
			String message = "ȸ�� ������ ���� �߽��ϴ�.";
			
			req.setAttribute("title", "update");
			req.setAttribute("mode", "update");
			req.setAttribute("message", message);
			forward(req, resp, "/WEB-INF/views/main/main.jsp");
			return;
		}
		resp.sendRedirect(cp+"/main/main.do");
		
	}
//ȸ��Ż��	
	private void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	private void myPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	}
	
}
