package com.ricky.audio;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class TrackScheduler extends AudioEventAdapter {
	
	 private final AudioPlayer player;
	 private final Queue<AudioInfo> queue;
	
	 public TrackScheduler (AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
      }
	 
	  public void queue(AudioTrack track, Member author) {
		  AudioInfo info = new AudioInfo(track, author);
	        queue.add(info);

	        if (player.getPlayingTrack() == null) {
	            player.playTrack(track);
	      }
	  }

	
	  @Override
	  public void onPlayerPause(AudioPlayer player) {
	    // Player was paused
	  }

	  @Override
	  public void onPlayerResume(AudioPlayer player) {
	    // Player was resumed
	  }

	  @Override
	  public void onTrackStart(AudioPlayer player, AudioTrack track) {
		  AudioInfo info = queue.element();
	      VoiceChannel vChan = info.getAuthor().getVoiceState().getChannel();
	      if (vChan == null) { // User has left all voice channels
	          player.stopTrack();
	      } else {
	          info.getAuthor().getGuild().getAudioManager().openAudioConnection(vChan);
	      }
	  }

	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
	        Guild g = queue.poll().getAuthor().getGuild();
	        if (queue.isEmpty() && queue != null) {
	            g.getAudioManager().closeAudioConnection();
	        } else {
	            player.playTrack(queue.element().getTrack());
	        }
	  }

	  public void purgeQueue() {
	        queue.clear();
	  }

	  public void remove(AudioInfo entry) {
	      queue.remove(entry);
	  }

	  public AudioInfo getTrackInfo(AudioTrack track) {
	      return queue.stream().filter(audioInfo -> audioInfo.getTrack().equals(track)).findFirst().orElse(null);
	  }

	  @Override
	  public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
	    // An already playing track threw an exception (track end event will still be received separately)
	  }

	  @Override
	  public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
	    // Audio track has been unable to provide us any audio, might want to just start a new track
	  }
}