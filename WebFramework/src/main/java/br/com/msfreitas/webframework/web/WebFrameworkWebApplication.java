package br.com.msfreitas.webframework.web;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import br.com.msfreitas.webframework.annotations.WebFrameworkGetMethod;
import br.com.msfreitas.webframework.annotations.WebFrameworkPostMethod;
import br.com.msfreitas.webframework.datastructures.ControllerMap;
import br.com.msfreitas.webframework.datastructures.RequestControllerData;
import br.com.msfreitas.webframework.explorer.ClassExplorer;
import br.com.msfreitas.webframework.util.WebFrameworkLogger;

public class WebFrameworkWebApplication {
	public static void run(Class<?> sourceClass) {
		// desligar todos os logs do apache tomcat
		java.util.logging.Logger.getLogger("org.apache").setLevel(java.util.logging.Level.OFF);
		long ini, fim;

		WebFrameworkLogger.showBanner();

		try {
			// class explorer
			// começar a criar um método de extração de metadados:
			extractMetadata(sourceClass);

			ini = System.currentTimeMillis();

			WebFrameworkLogger.log("Embeded Web Container", "Iniciando WebFrameworkWebApplication");
			Tomcat tomcat = new Tomcat();
			Connector connector = new Connector();
			connector.setPort(8080);
			tomcat.setConnector(connector);
			WebFrameworkLogger.log("Embeded Web Container", "Iniciando na porta 8080");

			// contexto olhando a raiz da aplicação

			// procurando classes na raiz da app
			Context context = tomcat.addContext("", new File(".").getAbsolutePath());
			Tomcat.addServlet(context, "WebFrameworkDispatcherservlet", new WebFrameworkDispatcherServlet());

			// tudo que digitar na URL vai cair nesse ponto
			context.addServletMappingDecoded("/*", "WebFrameworkDispatcherservlet");

			fim = System.currentTimeMillis();
			WebFrameworkLogger.log("Embeded Web Container", "Tomcat iniciado em " + (double) (fim - ini) + " ms");

			// inicialiazando
			tomcat.start();
			tomcat.getServer().await();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void extractMetadata(Class<?> sourceClass) {
		try {
			List<String> allClasses = ClassExplorer.retrieveAllClasses(sourceClass);
			for (String classe : allClasses) {
				// recuperar as anotacoes da classe
				Annotation annotations[] = Class.forName(classe).getAnnotations();
				for (Annotation classAnnotation : annotations) {
					if (classAnnotation.annotationType().getName().equals("br.com.msfreitas.webframework.annotations.WebFrameworkController")) {
						WebFrameworkLogger.log("MetadataExplorer", "Found a controller: " + classe);
						extractMethods(classe);
					}
				}
			}
			for(RequestControllerData item : ControllerMap.values.values()) {
				WebFrameworkLogger.log(" - ", "   " + item.getHttpMethod() + ":" + item.getUrl() + " [ " + item.getControllerClass() + "." + item.getControllerMethod() + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void extractMethods(String className)throws Exception {
		String httpMethod = "";
		String path = "";
		
		//recuperar todos os metodos da classe
		for(Method method: Class.forName(className).getDeclaredMethods()) {
			//WebFrameworkLogger.log(" - ", method.getName());
			for(Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType().getName().equals("br.com.msfreitas.webframework.annotations.WebFrameworkGetMethod")) {
					httpMethod = "GET";
					path = ((WebFrameworkGetMethod)annotation).value();
				} else if (annotation.annotationType().getName().equals("br.com.msfreitas.webframework.annotations.WebFrameworkPostMethod")) {
					httpMethod = "POST";
					path = ((WebFrameworkPostMethod)annotation).value();
				}
			}
			//WebFrameworkLogger.log(" - ", httpMethod + " " + path);
			RequestControllerData getData = new RequestControllerData(httpMethod, path, className, method.getName());
			ControllerMap.values.put(httpMethod + path, getData);
		}
	}
}
