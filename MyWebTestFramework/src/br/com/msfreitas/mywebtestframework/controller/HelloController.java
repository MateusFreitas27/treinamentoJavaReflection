package br.com.msfreitas.mywebtestframework.controller;

import br.com.msfreitas.mywebtestframework.model.Produto;
import br.com.msfreitas.mywebtestframework.service.IService;
import br.com.msfreitas.webframework.annotations.WebFrameworkBody;
import br.com.msfreitas.webframework.annotations.WebFrameworkController;
import br.com.msfreitas.webframework.annotations.WebFrameworkGetMethod;
import br.com.msfreitas.webframework.annotations.WebFrameworkPostMethod;

@WebFrameworkController
public class HelloController {
	
	private IService iService;
	
	@WebFrameworkGetMethod("/hello")
	public String returnHelloWorld() {
		return "Hello World!!";
	}
	
	@WebFrameworkGetMethod("/produto")
	public Produto exibirProduto() {
		Produto p = new Produto(1, "Nome1",200.00,"teste.jpg");
		return p;
	}
	
	@WebFrameworkPostMethod("/produto")
	public String cadastrarProduto(@WebFrameworkBody Produto produtoNovo) {
		System.out.println(produtoNovo);
		return "Produto cadastrado";
	}
	
	@WebFrameworkGetMethod("/injected")
	public String chamadaCustom() {
		return iService.chamadaCustom("Hello injected");
	}
}
