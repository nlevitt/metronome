package org.pseudorandom.metronome;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MetronomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected String noteName(int n) {
		switch (n) {
		case 1: return "WHOLE_NOTE";
		case 2: return "HALF_NOTE";
		case 4: return "QUARTER_NOTE";
		case 8: return "EIGHTH_NOTE";
		case 16: return "SIXTEENTH_NOTE";
		case 32: return "THIRTYSECOND_NOTE";
		case 64: return "SIXTYFOURTH_NOTE";
		default: return null;
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logRequest(request);
		
		if (request.getParameter("bpm") != null) {
			request.setAttribute("bpm", request.getParameter("bpm")); 
		}
		if (request.getParameter("beats") != null) {
			request.setAttribute("beats", request.getParameter("beats")); 
		}
		if (request.getParameter("emp") != null) {
			request.setAttribute("emp", request.getParameter("emp")); 
		}
		
		if (request.getParameter("beat") != null) {
			int n = Integer.parseInt(request.getParameter("beat"));
			request.setAttribute("beat", noteName(n));
		}
		if (request.getParameter("tock") != null) {
			int n = Integer.parseInt(request.getParameter("tock"));
			request.setAttribute("tock", noteName(n));
		}

		request.getRequestDispatcher("/metronome.jspx").forward(request, response);
	}

	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	protected void logRequest(HttpServletRequest request) {
		StringBuffer buf = request.getRequestURL();
		if (request.getQueryString() != null) {
			buf.append('?').append(request.getQueryString());
		}
		System.out.println("[" + DATE_FORMAT.format(new Date()) + "] MetronomeServlet handling request from " + request.getRemoteAddr() + " for " + buf);
	}
}
