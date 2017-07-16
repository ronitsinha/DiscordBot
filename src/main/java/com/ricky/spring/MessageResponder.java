package com.ricky.spring;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.github.codingricky.marvel.model.MarvelCharacter;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageResponder extends ListenerAdapter {
	void logCommand (MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		
		if (event.getAuthor().isBot()) {
			System.out.println("BOT " + event.getAuthor().getName() + " issued command: " + message);
		} else {
			System.out.println("USER " + event.getAuthor().getName() + " issued command: " + message);			
		}
	}
	
	// Get a Marvel character's id from name.
	int marvelCharacterIDFromName (String name) {
		for (int i = 0; i < Constants.allCharacters.size(); i ++) {
			String parsedSearch = name;
			String parsedName = Constants.allCharacters.get(i).getName();
			
			int beginParenthRemove = -1;
			int endParenthRemove = -1;
			
			if (parsedName.contains("(") && parsedName.contains(")")) {
				beginParenthRemove = parsedName.indexOf("(");
				endParenthRemove = parsedName.indexOf(")");
				
				if (parsedName.indexOf(")") == parsedName.length() - 1) {
					parsedName = parsedName.substring(0, beginParenthRemove);
				} else {
					parsedName = parsedName.substring(0, beginParenthRemove) + parsedName.substring(endParenthRemove + 1, parsedName.length());
				}
			}
						
			parsedSearch = parsedSearch.toUpperCase().replaceAll(" of ", "").replaceAll(" the ", "").replaceAll("\\s+", "").replaceAll("-", "");
			parsedName = parsedName.toUpperCase().replaceAll("\\s+", "").replaceAll("-", "");
			
			System.out.println(Constants.allCharacters.get(i).getName());
			
			if (parsedSearch.equals(parsedName)) {
				System.out.println("\nSearch Result: " + Constants.allCharacters.get(i).getName());
				return Constants.allCharacters.get(i).getId();
			}
		}
		
		// No character found: return -1;
		return -1;
	}
	
	public void onMessageReceived (MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		
		// Say hello using the player's user name
		if (message.startsWith(Constants.COMMAND_PREFIX + "hello")) {
			String senderName = event.getAuthor().getName();			
			String response = "Hello, **" + senderName + "**, it's good to hear from you!";
			
			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);
		} else if (message.startsWith(Constants.COMMAND_PREFIX + "knockjoke")) {
			// Tell a random knock-knock joke from a list of them stored in Constants.knockjokes			
			int index = Constants.gen.nextInt(Constants.knockjokes.size ());
			
			String response = Constants.knockjokes.get(index);

			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);

		} else if (message.startsWith(Constants.COMMAND_PREFIX + "roll")) {
			// Split the string into command and modifier, where command is .roll and modifier is the die to roll.
			
			String response = "*Unrecognized die to roll. I support d20, d6, d4, and percentile.*";
			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+");
				
				if (messageSplit.length > 0) {
					if (messageSplit[1].equals ("d20")) {
						response = "Rolled a **d20**! Result: " + String.valueOf(Constants.gen.nextInt(20) + 1);
					} else if (messageSplit[1].equals("d6")) {
						response = "Rolled a **d6**! Result: " + String.valueOf(Constants.gen.nextInt(6) + 1);
					} else if (messageSplit[1].equals("d4")) {
						response = "Rolled a **d4**! Result: " + String.valueOf(Constants.gen.nextInt(4) + 1);
					} else if (messageSplit[1].equals("percentile")) {
						response = "Rolled for **percentile**! Result: " + String.valueOf(Constants.gen.nextInt(100) + 1);
					}
				
				}
			}
			
			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);

		} else if (message.startsWith(Constants.COMMAND_PREFIX + "pokedex")) {
			// The second part of the message (the name of the pokemon) is used to search the online pokedex
			// on the pokemon website.
			// Text is taken from "version-descriptions active" <div> with that class in website's HTML
			// There are two entries for each pokemon, so the user must specify to receive either entry 1 or 2.
			
			
			Document pkweb = new Document ("Please specify a pokemon and an entry number.");
			String response = "*Please specify a real pokemon and an entry number.*";

			
			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+");

				if (messageSplit.length > 2) {
					try {
						int chosenEntry = Integer.valueOf(messageSplit[2]);
						
						// Connects to a pokemon's pokedex page
						pkweb = Jsoup.connect("http://www.pokemon.com/us/pokedex/" + messageSplit[1].toLowerCase()).get();
												// Finds the div which houses the description information
						Element entries = pkweb.getElementsByClass("version-descriptions active").first();
						// Gets the image of the pokemon
						Element imgDiv = pkweb.getElementsByClass("profile-images").first();
						
						// Finds the first entry "version-y"
						Element entry0 = entries.getElementsByTag("p").last();
						// Finds the second entry "version-x"
						Element entry1 = entries.getElementsByTag("p").first();
						
						// Adds the URL to the message to the image will be embedded.
						String imgURL = imgDiv.getElementsByClass("active").attr("src");
						
						// Displays a certain entry based on the user's request.
						if (chosenEntry == 1) {
							response = "**Pokémon: " + WordUtils.capitalize(messageSplit[1]) + "**\n" + entry0.text() + "\n\n" + imgURL;
						} else if (chosenEntry == 2) {
							response = "**Pokémon: " + WordUtils.capitalize(messageSplit[1]) + "**\n" + entry1.text() + "\n\n" + imgURL;							
						} else {
							response = "*Unrecongized entry number: " + String.valueOf(chosenEntry) + ". Must be either 1 or 2.*";
						}
					} catch (IOException e) {
						// If URL is invalid, then pkmn does not exist.
						e.printStackTrace();
						response = "Cannot find pokemon: " + messageSplit[1] + ".";
					}
				} else if (messageSplit.length > 1) {
					// Not second modifier (entry number) specified.
					response = "*There are two entries for each pokemon. Specify which one to receive by using '1' or '2'. For example, '.pokedex pikachu 1'.*";
				}
			} 
			
			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);

		} else if (message.startsWith(Constants.COMMAND_PREFIX + "ign")) {
			// Finds IGN game review from a title (i.e portal 2)
			
			String response = "No game found";
			
			if (message.contains (" ")) {
				// Split the message only at the first space, the other spaces
				// may be part of the game's name.
				String[] messageSplit = message.split("\\s+", 2);
				
				if (messageSplit.length > 1) {
					String searchString = messageSplit[1];
						
					// if the second part of the sting has spaces, replace them with dashes so it can be used for the URL
					// (i.e super mario galaxy -> super-mario-galaxy)
					if (messageSplit[1].contains(" ")) {
						String[] message1Split = messageSplit[1].split("\\s+");
						searchString = "";
							
						for (int i = 0; i < message1Split.length; i ++) {
							searchString += message1Split[i];
							
							if (i < message1Split.length - 1) {
								searchString += "-";
							}
						}
					}
					
					try {	
						Document ignWeb = Jsoup.connect("http://www.ign.com/games/" + searchString.toLowerCase()).get();
						
						Element platforms = ignWeb.getElementsByClass("contentPlatformsText").first();
						// Since not all console pages have a rating/description, grab two of them just to be safe.
						String platformUrl = platforms.getElementsByTag("a").last().attr("href");
						String platformUrl2 = platforms.getElementsByTag("a").first().attr("href");
						
						
						Element summary = ignWeb.getElementById("summary");
						Element header = ignWeb.getElementsByClass("contentHead clear").first();
						Element title = header.getElementsByTag("h1").first();
						
						// Since not all console pages have a rating/description, grab two of them just to be safe.
						Document platformDoc = Jsoup.connect(platformUrl).get();
						Document platformDoc2 = Jsoup.connect(platformUrl2).get();
						
						try {
							Element scoreBox = platformDoc.getElementsByClass("scoreBox-score").first();	
							Element score = scoreBox.getElementsByClass("value").first();
								
							Element descBox = platformDoc.getElementsByClass("scoreBox-description").first();
							Element description = descBox.getElementsByClass("scoreBox-descriptionLink").first ();
							
							Element verdict = summary.getElementsByTag("p").first();
							
							String imgURL = platformDoc.getElementsByClass ("mainBoxArt").first ().getElementsByClass("highlight-boxArt").first().attr("src");
							
							response = "**"+ title.text() + ":**\n__Rating:__ " + score.text() + "/10.0" + "\n__Description:__ " + description.text() + "\n__Summary:__ " + verdict.text() + "\n\n" + imgURL;
						
						} catch (NullPointerException e) {
							try {
								// if we couldn't find the rating/desc in the last one, try the first one.
								// This ensures that virtually all games will have the information.
								Element scoreBox = platformDoc2.getElementsByClass("scoreBox-score").first();	
								Element score = scoreBox.getElementsByClass("value").first();
									
								Element descBox = platformDoc2.getElementsByClass("scoreBox-description").first();
								Element description = descBox.getElementsByClass("scoreBox-descriptionLink").first ();
								
								Element verdict = summary.getElementsByTag("p").first();
								
								String imgURL = platformDoc2.getElementsByClass ("mainBoxArt").first ().getElementsByClass("highlight-boxArt").first().attr("src");
								
								response = "**" + title.text() + ":**\n__Rating:__ " + score.text() + "/10.0" + "\n__Description:__ " + description.text() + "\n__Summary:__ " + verdict.text() + "\n\n" + imgURL;
								e.printStackTrace();
							} catch (NullPointerException e1) {
								response = "*Could not find info for game: " + messageSplit[1] + "*";
							}
						}
						
						
						
					} catch (IOException e) {
						response = "*Could not find game: " + messageSplit[1] + "*";
						e.printStackTrace();
					}
				}
			} else {
				response = "*To use this command, do '.ign [name of game]', such as '.ign super mario galaxy'.*";
			}
			
			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);

		} else if (message.startsWith(Constants.COMMAND_PREFIX + "marvel")) {
			String response = "Cool Marvel stuff!";
			
			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+", 3);
				
				if (messageSplit.length > 2) {
					if (messageSplit[1].equals("character")) {
						try {
							int id = marvelCharacterIDFromName (messageSplit[2]);
							
							if (id != -1) {
								MarvelCharacter character = Constants.marvelClient.getCharacter(id).getData().getResults().get(0);
								
								response = "**" + character.getName() + "**\n" + character.getDescription() + "\n" + character.getThumbnail().getPath() + "/standard_amazing." + character.getThumbnail().getExtension();
							} else {
								response = "*Error: could not find character id: " + messageSplit[2] + "*";
							}
						} catch (IOException e) {
							response = "*Error: could not find character: " + messageSplit[2] + "*";
							e.printStackTrace();
						}
					}
				}
			}
			
			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);	
			
		} else if (message.startsWith(Constants.COMMAND_PREFIX + "dictionary")) {
			
			String response = "Define words using the Oxford Dictionary!";

			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+");	
				
				if (messageSplit.length > 1) {
					String requestURL = Constants.OXFORD_BASE_URL + "/entries/en/" + messageSplit[1].toLowerCase();
					try {
						Document data = Jsoup.connect(requestURL).data("app_id", Constants.OXFORD_APP_ID, "app_key", Constants.OXFORD_APP_KEY).get();
						System.out.println(data.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		
			event.getTextChannel().sendMessage(response).queue();
			logCommand (event);
			
		}
	}
}
