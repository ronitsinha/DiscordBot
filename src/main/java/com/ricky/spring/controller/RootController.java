package com.ricky.spring.controller;

import org.springframework.context.support.ClassPathXmlApplicationContext;
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
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ();
		
		Resource retval = ctx.getResource("classpath:com/ricky/spring/controller/wakemydyno.txt");
		
		ctx.close ();
		
		return retval;
	}
	
	@RequestMapping("/output.wav")
	public Resource output () {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ();
		
		Resource retval = ctx.getResource("file:target/output.wav");
		
		ctx.close ();
		
		return retval;
	}
	
	@RequestMapping("/knockjokes.txt")
	public Resource knockjokes () {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext ();
		
		Resource retval = ctx.getResource("classpath:com/ricky/spring/controller/knockjokes.txt");
		
		ctx.close ();
		
		return retval;
	}
}
