package org.pseudorandom.metronome;

import java.io.IOException;

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

		System.out.println("MetronomeServlet.doGet() params=" + request.getParameterMap());
		
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

}
