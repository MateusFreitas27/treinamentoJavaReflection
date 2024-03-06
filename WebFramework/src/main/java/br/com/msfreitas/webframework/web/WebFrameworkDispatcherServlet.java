package br.com.msfreitas.webframework.web;

import java.io.IOException;

import br.com.msfreitas.webframework.util.WebFrameworkLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WebFrameworkDispatcherServlet extends HttpServlet {
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//ignorar favicon
		if(req.getRequestURL().toString().endsWith("/favicon.ico")) return ;
		
		String url = req.getRequestURL().toString();
		String httpMethod = req.getMethod().toUpperCase();
		
		WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "URL: " + url + "(" + httpMethod + ")");
	}
}
