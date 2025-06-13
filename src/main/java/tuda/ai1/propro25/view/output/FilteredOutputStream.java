/* (C) 2025 TUDA AI1 team - ProPro 2025 - Chess */
package tuda.ai1.propro25.view.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class FilteredOutputStream extends OutputStream {
	private final PrintStream original;
	private final String[] filterStrings = {"Unsupported JavaFX configuration", "PlatformImpl startup"};
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public FilteredOutputStream(PrintStream original) {
		this.original = original;
	}

	@Override
	public void write(int b) throws IOException {
		if (b == '\n') {
			flush();
		} else {
			buffer.write(b);
		}
	}

	@Override
	public void flush() throws IOException {
		String raw = buffer.toString();
		buffer.reset();
		for (String line : raw.split("[\r\n]")) {
			boolean isFiltered = false;
			for (String s : filterStrings) {
				if (line.contains(s)) {
					isFiltered = true;
					break;
				}
			}
			if (!isFiltered) {
				original.println(line);
			}
		}
	}

	@Override
	public void close() throws IOException {
		flush();
		original.close();
	}
}