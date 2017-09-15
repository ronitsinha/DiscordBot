package com.ricky.spring;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.sound.sampled.AudioInputStream;

import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.MaryAudioUtils;

import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import com.ricky.audio.AudioPlayerSendHandler;
import com.ricky.audio.TrackScheduler;

public class MessageResponder extends ListenerAdapter {
	void processCommand (MessageReceivedEvent event, String response) {
		
		event.getTextChannel().sendMessage(response).queue();
		
		String message = event.getMessage().getContent();
		
		if (event.getAuthor().isBot()) {
			System.out.println("BOT " + event.getAuthor().getName() + " issued command: " + message);
		} else {
			System.out.println("USER " + event.getAuthor().getName() + " issued command: " + message);		
		}
	}
	
	public void onMessageReceived (MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		
		// Say hello using the player's user name
		if (message.startsWith(Constants.COMMAND_PREFIX + "hello")) {
			String senderName = event.getAuthor().getName();			
			String response = "Hello, **" + senderName + "**, it's good to hear from you!";
			
			processCommand (event, response);

		} else if (message.startsWith(Constants.COMMAND_PREFIX + "knockjoke")) {
			// Tell a random knock-knock joke from a list of them stored in Constants.knockjokes			
			int index = Constants.gen.nextInt(Constants.knockjokes.size ());
			
			String response = Constants.knockjokes.get(index);

			processCommand (event, response);

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
			
			processCommand (event, response);

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
			
			processCommand (event, response);

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
			
			processCommand (event, response);

		} else if (message.startsWith(Constants.COMMAND_PREFIX + "dictionary")) {
			// Defines a word using Oxford Dictionaries API
			
			String response = "Define words using the Oxford Dictionary!";

			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+");	
				
				if (messageSplit.length > 1) {
					String requestURL = Constants.OXFORD_BASE_URL + "/entries/en/" + messageSplit[1].toLowerCase();
					try {
						JSONObject data = new JSONObject (Jsoup.connect(requestURL).header("app_id", Constants.OXFORD_APP_ID).header("app_key", Constants.OXFORD_APP_KEY).ignoreContentType(true).execute().body());
						
						JSONArray results = data.getJSONArray("results");
						JSONObject resultsObj0 = results.getJSONObject(0);
						JSONArray lexicalEntires = resultsObj0.getJSONArray("lexicalEntries");
						JSONObject lexObj0 = lexicalEntires.getJSONObject(0);
						
						String partOfSpeech = lexObj0.getString("lexicalCategory");
						JSONArray entries = lexObj0.getJSONArray("entries");
						
						response = "**" + WordUtils.capitalize(messageSplit[1]) + "** *(" + partOfSpeech + ")*:\n";
						
						for (int i = 0; i < entries.length(); i ++) {
							JSONObject entryObj = entries.getJSONObject(i);
							JSONArray senses = entryObj.getJSONArray("senses");
							JSONObject senseObj = senses.getJSONObject(0);
							JSONArray definitions = senseObj.getJSONArray("definitions");
							
							StringBuilder strb = new StringBuilder (definitions.getString(0));
							strb.setCharAt(0, Character.toUpperCase(strb.charAt(0)));
							
							
							response += "	-" + strb.toString() + "\n";
						}
												
						System.out.println(data.toString());
					} catch (IOException | JSONException e) {
						response = "*Sorry, I couldn't find a definition for " + messageSplit[1] + ".*";
						e.printStackTrace();
					}
				}
			}
		
			processCommand (event, response);
			
		} else if (message.startsWith(Constants.COMMAND_PREFIX + "speak")) {
			// Text to speech in voice channel.
			String response = "Success!";
			
			VoiceChannel userChan = event.getGuild().getMember(event.getAuthor()).getVoiceState().getChannel();
			
			if (userChan != null) {
				if (message.contains(" ")) {
					String[] messageSplit = message.split("\\s+", 2);
					
					if (messageSplit.length > 1) {
						String outputFileName = "target/output.wav";
						
						AudioInputStream audio = null;
						try {
							audio = Constants.mary.generateAudio(messageSplit [1]);
						} catch (SynthesisException e) {
							System.err.println("Synthesis failed: " + e.getMessage());
							System.exit(1);
						}

						// Write to output (saved in target/output.wav as indicated by outputFileName).
						double[] samples = MaryAudioUtils.getSamplesAsDoubleArray(audio);
						try {
							MaryAudioUtils.writeWavFile(samples, outputFileName, audio.getFormat());
							System.out.println("Output written to " + outputFileName);
						} catch (IOException e) {
							System.err.println("Could not write to file: " + outputFileName + "\n" + e.getMessage());
							System.exit(1);
						}
						
						TrackScheduler trackScheduler = new TrackScheduler (Constants.player);
						Constants.player.addListener(trackScheduler);
						
						event.getGuild().getAudioManager ().setSendingHandler(new AudioPlayerSendHandler (Constants.player));
						
						Constants.playerManager.loadItemOrdered(event.getGuild(), "https://springbot.herokuapp.com/output.wav", new AudioLoadResultHandler() {
							  @Override
							  public void trackLoaded(AudioTrack track) {
							    trackScheduler.queue(track, event.getGuild().getMember(event.getAuthor()));
							  }

							  @Override
							  public void playlistLoaded(AudioPlaylist playlist) {
							    for (AudioTrack track : playlist.getTracks()) {
							      trackScheduler.queue(track, event.getGuild().getMember(event.getAuthor()));
							    }
							  }

							  @Override
							  public void noMatches() {
							    // Notify the user that we've got nothing
								System.out.println("We've got nothin'");
							  }

							  @Override
							  public void loadFailed(FriendlyException throwable) {
							    // Notify the user that everything exploded
								System.out.println("Everything exploded!");
							  }
						});
											
					}
				}
			} else {
				response = "*Could not find " + event.getAuthor().getName() + " in any voice channels. Please join a voice channel to use this command.*";
			}
				
			processCommand (event, response);
			
		} else if (message.startsWith(Constants.COMMAND_PREFIX + "iplocate")) {
			// Locate physical location of ip address using freegeoip.net and Jsoup.
			String response = "Find a computer's location from its ip address or hostname!";
			
			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+");
				
				if (messageSplit.length > 1) {
					String requestURL = Constants.FREEGEOIP_BASE_URL + messageSplit[1];
					try {
						JSONObject data = new JSONObject (Jsoup.connect(requestURL).ignoreContentType(true).execute().body());
						response = data.toString();
						
						response = "Located **" + messageSplit[1] + "**! Results follow:" 
								+ "\n\nIP: " + data.getString("ip") 
								+ "\nCity: " + data.getString("city") 
								+ "\nRegion: " + data.getString("region_name") 
								+ "\nCountry: " + data.getString("country_name")
								+ "\nTime Zone: " + data.getString("time_zone")
								+ "\n\nLatitude: " + data.getDouble ("latitude")
								+ "\nLongitude: " + data.getDouble ("longitude");
					} catch (JSONException e) {
						response = "Oops! Something went wrong with getting JSON!";
						e.printStackTrace();
					} catch (IOException e) {
						response = "IP address or hostname not found.";
						e.printStackTrace();
					}
				}
			}
						
			processCommand (event, response);
			
		} else if (message.startsWith(Constants.COMMAND_PREFIX + "dadjokes")) {
			String response = "Dad jokes.";
			
			Constants.dadJokes = !Constants.dadJokes;
			
			if (Constants.dadJokes) { response = "Dad jokes have been enabled."; } else { response = "Dad jokes have been disabled."; }
			
			processCommand (event, response);
			
		} else if (message.startsWith(Constants.COMMAND_PREFIX + "wake")) {
			String response = "If the bot is offline, use .wake to wake it up";
			
			try {
				@SuppressWarnings("unused")
				Document wake = Jsoup.connect("https://springbot.herokuapp.com/").get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			processCommand (event, response);
		} else if (Constants.dadJokes) {
			String keyword = "";
			
			if (message.contains(" ")) {
				String[] messageSplit = message.split("\\s+");
				
				if (messageSplit.length > 1) {
					for (int i = 0; i < messageSplit.length; i++) {
						if (i < messageSplit.length - 2) {
							if (messageSplit[i].equalsIgnoreCase("i")) {
								if (messageSplit[i+1].equalsIgnoreCase("am") || messageSplit[i+1].equalsIgnoreCase("m")) {
									keyword = messageSplit[i+2];
								}
							}
						} else if (i < messageSplit.length - 1) {
							if (messageSplit[i].equalsIgnoreCase("i'm") || messageSplit[i].equalsIgnoreCase("im") || messageSplit[i].equalsIgnoreCase("iam")) {
								keyword = messageSplit[i+1];
							}
						}
					}
				}
			}
			
			if (!keyword.equals("")) {
				event.getTextChannel().sendMessage("Hi, " + keyword + ", I'm Dad! Nice to meet you!").queue();
			}
		}
	}
}
