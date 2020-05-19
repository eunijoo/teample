package com.travel;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.MyUploadServlet;

@WebServlet("/travel/*")
@MultipartConfig
public class TravelServlet extends MyUploadServlet{
	private static final long serialVersionUID = 1L;
	private String pathname;
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");
		String cp = req.getContextPath();
		String uri = req.getRequestURI();
		
		/* ���Ƿα��� ĭ */
		
		if(uri.indexOf("seoul.do") != -1) {
			list(req, resp);
		}else if(uri.indexOf("created.do") != -1){
			createdForm(req,resp);
		}
	}
	
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		TravelDAO dao = new TravelDAO();
		String cp = req.getContextPath();
		
		// �˻�
		String condition = req.getParameter("condition");
		String keyword = req.getParameter("keyword");
		
		if(condition==null) {
			condition="subject";
			keyword="";
		}
		
		if(req.getMethod().equalsIgnoreCase("GET")) {
			keyword=URLDecoder.decode(keyword,"utf-8");
		}
		
		// ���� ��¥ ���
		Date nowTime = new Date();
		SimpleDateFormat day = new SimpleDateFormat("MM dd, yyyy");
		String date = day.format(nowTime);

		int dataCount;
		List<TravelDTO> list = null;
		
		if(keyword.length()!=0)
			dataCount= dao.dataCount(condition, keyword);
		else
			dataCount= dao.dataCount();
		
		if(keyword.length()!=0)
			list= dao.listTravel(condition, keyword);
		else
			list= dao.listTravel();
		
		String query="";
		String listUrl = "";
		String articleUrl ="";
		
		listUrl = cp+"/travel/list.do";
		articleUrl = cp+"/travel/article.do";
		
		if(keyword.length()!=0) {
			query="condition="+condition+"&keyword="+URLEncoder.encode(keyword,"utf-8");
			
			listUrl += "?"+query;
			articleUrl += "?"+query;
		}
		
		// ������ jsp�� �ѱ� ������
		req.setAttribute("list", list);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("listUrl", listUrl);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("condition", condition);
		req.setAttribute("keyword", keyword);
		req.setAttribute("date", date);
		
		// JSP�� ������
		forward(req, resp, "/WEB-INF/views/travel/list.jsp");
		
	}
	
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
	}
	
}
