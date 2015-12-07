package amidst.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import amidst.AmidstMetaData;
import amidst.Application;
import amidst.Options;
import amidst.gui.menu.AmidstMenu;
import amidst.gui.menu.LevelFileFilter;
import amidst.gui.menu.MenuActions;
import amidst.gui.menu.PNGFileFilter;
import amidst.gui.worldsurroundings.WorldSurroundings;
import amidst.logging.Log;
import amidst.minecraft.world.CoordinatesInWorld;
import amidst.minecraft.world.WorldType;
import amidst.minecraft.world.icon.WorldIcon;
import amidst.mojangapi.MojangApi;

public class MainWindow {
	private static final String ABOUT_MESSAGE = "Advanced Minecraft Interfacing and Data/Structure Tracking (AMIDST)\n"
			+ "By Skidoodle (amidst.project@gmail.com)";

	private final SeedPrompt seedPrompt = new SeedPrompt();

	private final Application application;
	private final Options options;
	private final MojangApi mojangApi;

	private final JFrame frame;
	private final Container contentPane;
	private final AmidstMenu menuBar;

	private volatile AtomicReference<WorldSurroundings> worldSurroundings;

	public MainWindow(Application application, Options options,
			MojangApi mojangApi) {
		this.application = application;
		this.options = options;
		this.mojangApi = mojangApi;
		this.frame = createFrame();
		this.contentPane = createContentPane();
		this.menuBar = createMenuBar(options);
		initKeyListener();
		initCloseListener();
		showFrame();
		checkForUpdates();
	}

	private JFrame createFrame() {
		JFrame frame = new JFrame();
		frame.setTitle(getSimpleVersionString());
		frame.setSize(1000, 800);
		frame.setIconImage(AmidstMetaData.ICON);
		return frame;
	}

	private String getLongVersionString(String version) {
		return getVersionString(" [Using Minecraft version: " + version + "]");
	}

	private String getSimpleVersionString() {
		return getVersionString("");
	}

	private String getVersionString(String string) {
		return "Amidst v" + AmidstMetaData.getFullVersionString() + string;
	}

	private Container createContentPane() {
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		return contentPane;
	}

	private AmidstMenu createMenuBar(Options options) {
		AmidstMenu menuBar = new AmidstMenu(options, new MenuActions(
				application, this, worldSurroundings,
				options.biomeColorProfileSelection));
		frame.setJMenuBar(menuBar.getMenuBar());
		return menuBar;
	}

	private void initKeyListener() {
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_PLUS) {
					adjustZoom(-1);
				} else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
					adjustZoom(1);
				}
			}
		});
	}

	private void initCloseListener() {
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				System.exit(0);
			}
		});
	}

	private void showFrame() {
		frame.setVisible(true);
	}

	private void checkForUpdates() {
		application.checkForUpdatesSilently();
	}

	public String askForSeed() {
		return seedPrompt.askForSeed(frame);
	}

	@SuppressWarnings("unchecked")
	public <T> T askForOptions(String title, String message, List<T> choices) {
		Object[] choicesArray = choices.toArray();
		return (T) JOptionPane.showInputDialog(frame, message, title,
				JOptionPane.PLAIN_MESSAGE, null, choicesArray, choicesArray[0]);
	}

	public File askForMinecraftWorldFile() {
		return getSelectedFileOrNull(createMinecraftWorldFileChooser());
	}

	private JFileChooser createMinecraftWorldFileChooser() {
		JFileChooser result = new JFileChooser();
		result.setFileFilter(new LevelFileFilter());
		result.setAcceptAllFileFilterUsed(false);
		result.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		result.setCurrentDirectory(mojangApi.getSaves());
		result.setFileHidingEnabled(false);
		return result;
	}

	public File askForScreenshotSaveFile() {
		return getSelectedFileOrNull(createScreenshotSaveFileChooser());
	}

	private JFileChooser createScreenshotSaveFileChooser() {
		JFileChooser result = new JFileChooser();
		result.setFileFilter(new PNGFileFilter());
		result.setAcceptAllFileFilterUsed(false);
		return result;
	}

	private File getSelectedFileOrNull(JFileChooser fileChooser) {
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	public void displayMessage(String message) {
		JOptionPane.showMessageDialog(frame, message);
	}

	public void displayException(Exception exception) {
		displayMessage(getStackTraceAsString(exception));
	}

	private String getStackTraceAsString(Exception exception) {
		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	public int askToConfirm(String title, String message) {
		return JOptionPane.showConfirmDialog(frame, message, title,
				JOptionPane.YES_NO_OPTION);
	}

	public WorldType askForWorldType() {
		String worldTypePreference = options.worldType.get();
		if (worldTypePreference.equals("Prompt each time")) {
			return askForOptions("New Project", "Enter world type\n",
					WorldType.getSelectable());
		} else {
			return WorldType.from(worldTypePreference);
		}
	}

	public CoordinatesInWorld askForCoordinates() {
		String coordinates = askForString("Go To",
				"Enter coordinates: (Ex. 123,456)");
		if (coordinates != null) {
			return parseCoordinates(coordinates);
		} else {
			return null;
		}
	}

	private String askForString(String title, String message) {
		return JOptionPane.showInputDialog(frame, message, title,
				JOptionPane.QUESTION_MESSAGE);
	}

	private CoordinatesInWorld parseCoordinates(String coordinates) {
		String[] parsedCoordinates = coordinates.replaceAll(" ", "").split(",");
		if (parsedCoordinates.length != 2) {
			return null;
		}
		try {
			return CoordinatesInWorld.from(
					Long.parseLong(parsedCoordinates[0]),
					Long.parseLong(parsedCoordinates[1]));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void dispose() {
		clearWorldSurroundings();
		frame.dispose();
	}

	// TODO: call from constructor?
	public void clearWorldSurroundings() {
		WorldSurroundings worldSurroundings = this.worldSurroundings
				.getAndSet(null);
		if (worldSurroundings != null) {
			menuBar.disableWorldMenu();
			menuBar.disableSavePlayerLocationsMenu();
			frame.setTitle(getSimpleVersionString());
			contentPane.remove(worldSurroundings.getComponent());
			worldSurroundings.dispose();
		}
	}

	/**
	 * This ensures that the instance variable worldSurroundings is assigned
	 * AFTER frame.validate() is called. This is important, because the
	 * repainter thread will draw the new worldSurroundings as soon as it is
	 * assigned to the instance variable.
	 */
	public void setWorldSurroundings(WorldSurroundings worldSurroundings) {
		contentPane.add(worldSurroundings.getComponent(), BorderLayout.CENTER);
		frame.validate();
		menuBar.enableWorldMenu();
		menuBar.setEnabledForSavePlayerLocationsMenu(worldSurroundings
				.canSavePlayerLocations());
		frame.setTitle(getLongVersionString(worldSurroundings
				.getRecognisedVersionName()));
		this.worldSurroundings.set(worldSurroundings);
	}

	public void capture(File file) {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			BufferedImage image = worldSurroundings.createCaptureImage();
			saveToFile(image, file);
			image.flush();
		}
	}

	private void saveToFile(BufferedImage image, File file) {
		try {
			ImageIO.write(image, "png", appendPNGFileExtensionIfNecessary(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File appendPNGFileExtensionIfNecessary(File file) {
		String filename = file.toString();
		if (!filename.toLowerCase().endsWith(".png")) {
			filename += ".png";
		}
		return new File(filename);
	}

	private void adjustZoom(int notches) {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.adjustZoom(notches);
		}
	}

	public void reloadBiomeLayer() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.getLayerReloader().reloadBiomeLayer();
		}
	}

	public void reloadPlayerLayer() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.getLayerReloader().reloadPlayerLayer();
		}
	}

	public void tickRepainter() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.tickRepainter();
		}
	}

	public void tickFragmentLoader() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.tickFragmentLoader();
		}
	}

	public void savePlayerLocations() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			worldSurroundings.savePlayerLocations();
		}
	}

	public void findStronghold() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			WorldIcon stronghold = askForOptions("Go to", "Select Stronghold:",
					worldSurroundings.getStrongholdWorldIcons());
			if (stronghold != null) {
				worldSurroundings.centerOn(stronghold.getCoordinates());
			}
		}
	}

	public void gotoCoordinate() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			CoordinatesInWorld coordinates = askForCoordinates();
			if (coordinates != null) {
				worldSurroundings.centerOn(coordinates);
			} else {
				displayMessage("You entered an invalid location.");
				Log.w("Invalid location entered, ignoring.");
			}
		}
	}

	public void gotoPlayer() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			List<WorldIcon> playerWorldIcons = worldSurroundings
					.getPlayerWorldIcons();
			if (!playerWorldIcons.isEmpty()) {
				WorldIcon player = askForOptions("Go to", "Select player:",
						playerWorldIcons);
				if (player != null) {
					worldSurroundings.centerOn(player.getCoordinates());
				}
			} else {
				displayMessage("There are no players in this world.");
			}
		}
	}

	public void copySeedToClipboard() {
		WorldSurroundings worldSurroundings = this.worldSurroundings.get();
		if (worldSurroundings != null) {
			String seed = "" + worldSurroundings.getSeed();
			StringSelection selection = new StringSelection(seed);
			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(selection, selection);
		}
	}

	public void newFromSeed() {
		String seed = askForSeed();
		if (seed != null) {
			if (seed.isEmpty()) {
				newFromRandom();
			} else {
				WorldType worldType = askForWorldType();
				if (worldType != null) {
					application.setWorld(mojangApi.createWorldFromSeed(seed,
							worldType));
				}
			}
		}
	}

	public void newFromRandom() {
		WorldType worldType = askForWorldType();
		if (worldType != null) {
			application.setWorld(mojangApi.createRandomWorld(worldType));
		}
	}

	public void newFromFileOrFolder() {
		File worldFile = askForMinecraftWorldFile();
		if (worldFile != null) {
			try {
				application.setWorld(mojangApi.createWorldFromFile(worldFile));
			} catch (Exception e) {
				displayException(e);
			}
		}
	}

	public void capture() {
		File file = askForScreenshotSaveFile();
		if (file != null) {
			capture(file);
		}
	}

	public void displayAboutMessage() {
		displayMessage(ABOUT_MESSAGE);
	}
}
