package com.ricky.spring.controller;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
	@RequestMapping("/")
	public String home () {
		return "Hello from Ronit!";
	}
	
	@RequestMapping("/wakemydyno.txt")
	public Resource wakemydyno () {
		FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext ();
		
		Resource retval = ctx.getResource("file:wakemydyno.txt");
		
		ctx.close();
		
		return retval;
	}
}
