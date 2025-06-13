/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.util;

public enum Sound {
	MOVE("/sounds/move_piece.wav"), CAPTURE("/sounds/capture_piece.wav"), UNDO("/sounds/undo.wav"), CHECK(
			"/sounds/check.wav");

	private final String path;

	Sound(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}
