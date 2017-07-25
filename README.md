# DiscordBot
A [Discord](https://discordapp.com/) bot made with [JDA](https://github.com/DV8FromTheWorld/JDA) and [Spring](https://spring.io/). This project is also running on [Heroku](https://dashboard.heroku.com/apps).

## Commands
All commands can be found in [src/main/java/com/ricky/spring/MessageResponder.java](https://github.com/RickyDiculous/DiscordBot/blob/master/src/main/java/com/ricky/spring/MessageResponder.java).
Here are a few of my favorites:

~pokedex: gets pokedex info from a name and entry number.
```
~pokedex pikachu 1
```

~speak: text to speech.
```
~speak Hello there!
```

~dictionary: gets dictionary definitions from the Oxford Dictionary.
```
~dictionary discord
```

## Dependencies
See the [pom.xml](https://github.com/RickyDiculous/DiscordBot/blob/master/pom.xml) for all the libraries and APIs used by this project.
Along with the pom.xml dependencies, I've also used these web APIs:
* [Oxford Dictionaries API](https://developer.oxforddictionaries.com/)
* [freegeoip.net](http://freegeoip.net/)