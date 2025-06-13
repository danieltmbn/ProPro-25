/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25;

import java.io.PrintStream;
import javafx.application.Application;
import tuda.ai1.propro25.view.GUIManager;
import tuda.ai1.propro25.view.output.FilteredOutputStream;

public class Main {

	public static void main(String[] args) {
		System.setErr(new PrintStream(new FilteredOutputStream(System.err)));
		Application.launch(GUIManager.class, args); // Diese Methode wird verwendet, um die JavaFX-Anwendung zu starten.
	}
}
