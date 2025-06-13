/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.AudioClip;

public class SoundManager {
	/**
	 * Cached the Audioclips damit sie nicht jedes Mal neu von der Festplatte
	 * geladen werden müssen
	 */
	private final Map<Sound, AudioClip> clipMap = new HashMap<>();

	private boolean soundEnabled = true;

	public SoundManager() {
		for (Sound sound : Sound.values()) {
			loadClip(sound);
		}
	}

	private void loadClip(Sound sound) {
		URL soundURL = SoundManager.class.getResource(sound.getPath());
		if (soundURL != null) {
			AudioClip clip = new AudioClip(soundURL.toExternalForm());
			clipMap.put(sound, clip);
		}
	}

	public void setSoundEnabled(boolean enabled) {
		this.soundEnabled = enabled;
	}

	public boolean isSoundEnabled() {
		return soundEnabled;
	}

	/**
	 * Spielt eine Audiodatei ab, sofern soundEnabled == true ist.
	 *
	 * @param sound
	 *            Das Geräusch, welches abgespielt werden soll
	 */
	public void playSound(Sound sound) {
		if (!soundEnabled)
			return;

		AudioClip clip = clipMap.get(sound);
		if (clip != null) {
			clip.play(); // Spielt asynchron und erlaubt gleichzeitiges Abspielen
		}
	}

}
