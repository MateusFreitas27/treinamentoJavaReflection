package br.com.msfreitas.webframework.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.google.gson.Gson;

import br.com.msfreitas.webframework.datastructures.ControllerInstances;
import br.com.msfreitas.webframework.datastructures.ControllerMap;
import br.com.msfreitas.webframework.datastructures.DependencyInjectionMap;
import br.com.msfreitas.webframework.datastructures.RequestControllerData;
import br.com.msfreitas.webframework.datastructures.ServiceImplementationMap;
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

		// verificar se existir um instancia da classe correpondente, caso não, criar
		// uma
		Object controller;
		WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Procurar instancia da controladora");
		try {
			controller = ControllerInstances.instace.get(data.controllerClass);
			if (controller == null) {
				WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Criando uma nova instancia da controladora");
				controller = Class.forName(data.controllerClass).getDeclaredConstructor().newInstance();
				ControllerInstances.instace.put(data.controllerClass, controller);
				
				injectDependencies(controller);
			}

			// precisamos extrair o método desta classe - ou seja o método que vai atender a
			// requisição. Vamos executar esse método e escrever a saida dele.
			Method controllerMethod = null;
			for (Method method : controller.getClass().getMethods()) {
				if (method.getName().equals(data.getControllerMethod())) {
					controllerMethod = method;
					break;
				}
			}

			if (controllerMethod.getParameterCount() > 0) {
				WebFrameworkLogger.log("WebFrameworkDispatcherServlet",
						"Método " + controllerMethod.getName() + " tem parâmetros!");
				Object arg;
				Parameter parameter = controllerMethod.getParameters()[0];
				if (parameter.getAnnotations()[0].annotationType().getName()
						.equals("br.com.msfreitas.webframework.annotations.WebFrameworkBody")) {

					WebFrameworkLogger.log("",
							"     Procurando parâmetro da requisição do tipo " + parameter.getType().getName());
					String body = readBytesFromRequest(req);

					WebFrameworkLogger.log("", "     conteúdo do parâmetro: " + body);
					arg = gson.fromJson(body, parameter.getType());

					WebFrameworkLogger.log("WebFrameworkDispatcherServlet",
							"Invocar o método " + controllerMethod.getName() + " com o parâmetro do tipo "
									+ parameter.getType().toString() + " para requisição");
					out.println(gson.toJson(controllerMethod.invoke(controller, arg)));
				}
			}else {
				WebFrameworkLogger.log("WebFrameworkDispatcherServlet",
						"Invocar o método " + controllerMethod.getName() + " para requisição");

				out.println(gson.toJson(controllerMethod.invoke(controller)));
			}
			
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void injectDependencies(Object controller) throws Exception {
		//ver apenas os campos anotados por Inject
				for(Field attr : controller.getClass().getDeclaredFields()) {
					String attrTipo = attr.getType().getName();
					WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Injetar " + attr.getName() + " do tipo " + attrTipo);
					Object serviceImpl;
					if(DependencyInjectionMap.objects.get(attrTipo)== null) {
						//tem declaração da interface?
						String implType = ServiceImplementationMap.implementations.get(attrTipo);
						if(implType != null) {
							WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Procurar Instâncias de " + implType);
							serviceImpl = DependencyInjectionMap.objects.get(implType);
							if(serviceImpl == null) {
								WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Injetar novo objeto");
								serviceImpl = Class.forName(implType).getDeclaredConstructor()
										.newInstance();
								DependencyInjectionMap.objects.put(implType, serviceImpl);
							}
							//atribuir essa instancia ao atributo anotado - Injeção de dependência.
							attr.setAccessible(true);
							attr.set(controller, serviceImpl);
							WebFrameworkLogger.log("WebFrameworkDispatcherServlet", "Objeto injetado com sucesso!");
						}
					}
					
				}		
	}

	private String readBytesFromRequest(HttpServletRequest req) throws Exception {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		while((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}
		return stringBuilder.toString();
	}
	
}
