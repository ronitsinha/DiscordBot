package com.ricky.spring;

import javax.security.auth.login.LoginException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		Discord ();
	}
	
	private static void Discord () {
		JDA discord = null;
		
		Constants.Init ();
						
		try {
			discord = new JDABuilder (AccountType.BOT).setToken(Constants.DISCORD_TOKEN).setGame(Game.of("everyone")).buildBlocking();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RateLimitedException e) {
			e.printStackTrace();
		}

		
		discord.addEventListener(new MessageResponder ());
	}
}
