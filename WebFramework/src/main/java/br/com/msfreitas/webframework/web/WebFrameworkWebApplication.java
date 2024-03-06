package br.com.msfreitas.webframework.web;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import br.com.msfreitas.webframework.util.WebFrameworkLogger;

public class WebFrameworkWebApplication {
	public static void run() {
		try {
			WebFrameworkLogger.log("Embeded Web Container", "Iniciando WebFrameworkWebApplication");
			Tomcat tomcat = new Tomcat();
			Connector connector = new Connector();
			connector.setPort(8080);
			tomcat.setConnector(connector);
			WebFrameworkLogger.log("Embeded Web Container", "Iniciando na porta 8080");
			
			
			//contexto olhando a raiz da aplicação
			
			//procurando classes na raiz da app
			Context context = tomcat.addContext("", new File(".").getAbsolutePath());
			Tomcat.addServlet(context, "WebFrameworkDispatcherservlet", new WebFrameworkDispatcherServlet());
			
			//tudo que digitar na URL vai cair nesse ponto
			context.addServletMappingDecoded("/*", "WebFrameworkDispatcherservlet");
			
			//inicialiazando
			tomcat.start();
			tomcat.getServer().await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}