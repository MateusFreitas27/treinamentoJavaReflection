package br.com.msfreitas.mywebtestframework.service;

import br.com.msfreitas.mywebtestframework.service.IService;
import br.com.msfreitas.webframework.annotations.WebFrameworkService;

@WebFrameworkService
public class ServiceImplementation implements IService {

	@Override
	public String chamadaCustom(String mensagem) {
		return "Teste chamada servico: " + mensagem;
	}

}
