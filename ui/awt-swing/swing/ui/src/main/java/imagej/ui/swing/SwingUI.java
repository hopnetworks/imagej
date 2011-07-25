//
// SwingUI.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

// TODO - see TODOs below

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayWindow;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ext.module.menu.ShadowMenu;
import imagej.ext.plugin.PluginService;
import imagej.ext.ui.swing.JMenuBarCreator;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.MessageType;
import imagej.ui.DialogPrompt.OptionType;
import imagej.ui.OutputWindow;
import imagej.ui.UI;
import imagej.ui.UserInterface;
import imagej.ui.swing.display.SwingDisplayWindow;
import imagej.util.Prefs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Swing-based user interface for ImageJ.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@UI
public class SwingUI implements UserInterface {

	private static final String README_FILE = "README.txt";
	private static final String PREF_FIRST_RUN = "firstRun-" + ImageJ.VERSION;

	private JFrame frame;
	private SwingToolBar toolBar;
	private SwingStatusBar statusBar;

	private ShadowMenu rootMenu;

	private ArrayList<EventSubscriber<?>> subscribers;

	// -- UserInterface methods --

	@Override
	public void initialize() {
		frame = new JFrame("ImageJ");
		toolBar = new SwingToolBar();
		statusBar = new SwingStatusBar();
		initializeMenus();

		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				Events.publish(new AppQuitEvent());
			}
		});

		pane.add(toolBar, BorderLayout.NORTH);
		pane.add(statusBar, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);

		subscribeToEvents();

		displayReadme();
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public SwingToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public SwingStatusBar getStatusBar() {
		return statusBar;
	}

	// -- Helper methods --

	/**
	 * Creates the master {@link ShadowMenu} structure from which {@link JMenuBar}
	 * s are generated.
	 */
	private void initializeMenus() {
		// CTR FIXME - rework this
		final PluginService pluginService = ImageJ.get(PluginService.class);
		rootMenu = new ShadowMenu(pluginService);
		createMenuBar(frame);
		Events.publish(new AppMenusCreatedEvent(frame.getJMenuBar()));
	}

	/**
	 * Creates a {@link JMenuBar} from the master {@link ShadowMenu} structure,
	 * and adds it to the given {@link JFrame}.
	 */
	private void createMenuBar(final JFrame f) {
		final JMenuBar menuBar = new JMenuBar();
		new JMenuBarCreator().createMenus(rootMenu, menuBar);
		f.setJMenuBar(menuBar);
	}

	private void deleteMenuBar(final JFrame f) {
		f.setJMenuBar(null);
		// HACK - w/o this next call the JMenuBars do not get garbage collected.
		// At least its true on the Mac. This might be a Java bug. Update:
		// I hunted on web and have found multiple people with the same problem.
		// The Apple ScreenMenus don't GC when a Frame disposes. Their workaround
		// was exactly the same. I have not found any official documentation of
		// this issue.
		f.setMenuBar(null);
	}

	private void displayReadme() {
		final String firstRun = Prefs.get(getClass(), PREF_FIRST_RUN);
		if (firstRun != null) return;
		Prefs.put(getClass(), PREF_FIRST_RUN, false);

		final JFrame readmeFrame = new JFrame();
		final JTextArea text = new JTextArea();
		text.setEditable(false);
		final JScrollPane scrollPane = new JScrollPane(text);
		scrollPane.setPreferredSize(new Dimension(600, 500));
		readmeFrame.setLayout(new BorderLayout());
		readmeFrame.add(scrollPane, BorderLayout.CENTER);
		readmeFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		readmeFrame.setTitle("ImageJ v" + ImageJ.VERSION + " - " + README_FILE);
		readmeFrame.pack();

		final String readmeText = loadReadmeFile();
		text.setText(readmeText);

		readmeFrame.setVisible(true);
		readmeFrame.dispose();
	}

	private String loadReadmeFile() {
		final File baseDir = getBaseDirectory();
		final File readmeFile = new File(baseDir, README_FILE);

		try {
			final DataInputStream in =
				new DataInputStream(new FileInputStream(readmeFile));
			final int len = (int) readmeFile.length();
			final byte[] bytes = new byte[len];
			in.readFully(bytes);
			in.close();
			return new String(bytes);
		}
		catch (final FileNotFoundException e) {
			throw new IllegalArgumentException(README_FILE + " not found at " +
				baseDir.getAbsolutePath());
		}
		catch (final IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	private File getBaseDirectory() {
		final File pathToClass = getPathToClass();
		final String path = pathToClass.getPath();

		final File baseDir;
		if (path.endsWith(".class")) {
			// assume class is in a subfolder of Maven target
			File dir = pathToClass;
			while (dir != null && !dir.getName().equals("target")) {
				dir = up(dir);
			}
			baseDir = up(up(up(dir)));
		}
		else if (path.endsWith(".jar")) {
			// assume class is in a library folder of the distribution
			final File dir = pathToClass.getParentFile();
			baseDir = up(dir);
		}
		else baseDir = null;

		// return current working directory if not found
		return baseDir == null ? new File(".") : baseDir;
	}

	/**
	 * Gets the file on disk containing this class.
	 * <p>
	 * This could be a jar archive, or a standalone class file.
	 * </p>
	 */
	private File getPathToClass() {
		final Class<?> c = getClass();
		final String className = c.getSimpleName();
		String path = getClass().getResource(className + ".class").toString();
		path = path.replaceAll("^jar:", "");
		path = path.replaceAll("^file:", "");
		path = path.replaceAll("^/*/", "/");
		path = path.replaceAll("^/([A-Z]:)", "$1");
		path = path.replaceAll("!.*", "");
		try {
			path = URLDecoder.decode(path, "UTF-8");
		}
		catch (final UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String slash = File.separator;
		if (slash.equals("\\")) slash = "\\\\";
		path = path.replaceAll("/", slash);
		return new File(path);
	}

	private File up(final File file) {
		if (file == null) return null;
		return file.getParentFile();
	}

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<DisplayCreatedEvent> createSubscriber =
			new EventSubscriber<DisplayCreatedEvent>() {

				@Override
				public void onEvent(final DisplayCreatedEvent event) {
					final Display display = event.getObject();
					final DisplayWindow window = display.getDisplayWindow();
					if (!(window instanceof SwingDisplayWindow)) return;
					final SwingDisplayWindow swingWindow = (SwingDisplayWindow) window;
					// add a copy of the JMenuBar to the new display
					if (swingWindow.getJMenuBar() == null) createMenuBar(swingWindow);
				}
			};
		subscribers.add(createSubscriber);
		Events.subscribe(DisplayCreatedEvent.class, createSubscriber);

		final EventSubscriber<DisplayDeletedEvent> deleteSubscriber =
			new EventSubscriber<DisplayDeletedEvent>() {

				@Override
				public void onEvent(final DisplayDeletedEvent event) {
					final Display display = event.getObject();
					final DisplayWindow window = display.getDisplayWindow();
					if (!(window instanceof SwingDisplayWindow)) return;
					final SwingDisplayWindow swingWindow = (SwingDisplayWindow) window;
					deleteMenuBar(swingWindow);
				}
			};
		subscribers.add(deleteSubscriber);
		Events.subscribe(DisplayDeletedEvent.class, deleteSubscriber);
	}

	@Override
	public OutputWindow newOutputWindow(final String title) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		return new SwingDialogPrompt(message, title, msg, option);
	}

}
