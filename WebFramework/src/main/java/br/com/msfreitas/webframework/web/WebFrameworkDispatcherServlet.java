package br.com.msfreitas.webframework.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import com.google.gson.Gson;

import br.com.msfreitas.webframework.datastructures.ControllerInstances;
import br.com.msfreitas.webframework.datastructures.ControllerMap;
import br.com.msfreitas.webframework.datastructures.RequestControllerData;
import br.com.msfreitas.webframework.util.WebFrameworkLogger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WebFrameworkDispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ignorar favicon
		if (req.getRequestURL().toString().endsWith("/favicon.ico"))
			return;
		
		PrintWriter out = new PrintWriter(resp.getWriter());
		Gson gson = new Gson();

		String url = req.getRequestURI();
		String httpMethod = req.getMethod().toUpperCase();
		String key = httpMethod + url;
		// busca a informação da classe; método; parâmetros da req
		RequestControllerData data = ControllerMap.values.get(key);

		WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "URL: " + url + "(" + httpMethod + ") - Handler "
				+ data.getControllerClass() + "." + data.getControllerMethod());
		
		//verificar se existir um instancia da classe correpondente, caso não, criar uma 
		Object controller;
		WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Procurar instancia da controladora");
		try {
			controller = ControllerInstances.instace.get(data.controllerClass);
			if(controller == null) {
				WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Criando uma nova instancia da controladora");
				controller = Class.forName(data.controllerClass).getDeclaredConstructor().newInstance();
				ControllerInstances.instace.put(data.controllerClass, controller);
			}
			
			//precisamos extrair o método desta classe - ou seja o método que vai atender a requisição. Vamos executar esse método e escrever a saida dele.
			Method controllerMethod = null;
			for(Method method : controller.getClass().getMethods()) {
				if(method.getName().equals(data.getControllerMethod())) {
					controllerMethod = method;
					break;
				}
			}
			
			WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Invocar o método " + controllerMethod.getName() + " para requisição");
			
			out.println(gson.toJson(controllerMethod.invoke(controller)));
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
