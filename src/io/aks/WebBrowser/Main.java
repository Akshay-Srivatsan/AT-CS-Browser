package io.aks.WebBrowser;
import java.awt.EventQueue;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.swing.JOptionPane;

/**
 * This class is the window that shows up when the application is launched. It
 * contains a tab pane for the different browsers and a tool bar on the right.
 * 
 * @author Akshay
 *
 */
public class Main extends Application implements Runnable
{

    public static final int MIN_JAVA_VERSION = 8;
    private static final String TITLE = "Akshay Srivatsan's Web Browser";
    private static final String PREFERENCES_ERROR = "Warning:\nThere was a problem accessing your preferences. Any changes made during this session will not be saved.";
    public static final String DEFAULT_HOME_PAGE = "http://en.wikipedia.org/wiki/Main_Page";

    public static String homePage;
    public static String[] launchArgs;
    public static int javaVersion = Integer.parseInt(System.getProperty(
	    "java.version").split("\\.")[1]);

    private ObservableList<BookmarkItem> bookmarks = FXCollections
	    .observableArrayList();
    private TabPane tabs;
    private ToolPane tools;
    private Stage stage;
    private static Preferences prefs;
    private final String BOOKMARK_PREFIX = "_bookmark_";
    private final String HOME_PAGE_KEY = "home_page";

    /**
     * Initalizes the bookmarks and home page. Other actions are done in the
     * JavaFX start() method.
     */
    public Main()
    {
	homePage = prefs.get(HOME_PAGE_KEY, DEFAULT_HOME_PAGE);
	int i = 0;
	String current = prefs.get(BOOKMARK_PREFIX + i, null);
	while (current != null)
	{
	    bookmarks.add(BookmarkItem.decode(current));
	    i++;
	    current = prefs.get(BOOKMARK_PREFIX + i, null);
	}
	Runtime.getRuntime().addShutdownHook(new Thread(this));

    }

    /**
     * Saves the current bookmarks and homepage into the preferences file.
     */
    public void run()
    {
	try
	{
	    prefs.clear();
	}
	catch (BackingStoreException e1)
	{
	    System.err.println(PREFERENCES_ERROR);
	}
	prefs.put(HOME_PAGE_KEY, homePage);
	for (int i = 0; i < bookmarks.size(); i++)
	{
	    prefs.put(BOOKMARK_PREFIX + i,
		    BookmarkItem.encode(bookmarks.get(i)));
	}
	try
	{
	    prefs.flush();
	}
	catch (BackingStoreException e)
	{
	    Main.showMessageDialog(PREFERENCES_ERROR);
	}
    }

    /**
     * Creates a new tab with the provided url.
     * 
     * @param url
     *            The url to load.
     * @return The GUI that is in the new tab.
     */
    public GUI newTab(String url)
    {
	Tab tab = new Tab(url);
	GUI gui = new GUI(this, url, tab);
	tab.setContent(gui);
	getTabs().getTabs().add(tab);
	return gui;
    }

    /**
     * Sets up basic values before JavaFX takes over.
     * 
     * @param args
     *            The command line parameters.
     */
    public static void main(String args[])
    {
	prefs = Preferences.userNodeForPackage(Main.class);
	launchArgs = args;
	launch(args);
    }

    /**
     * Shows a message dialog using JOptionPane. Because JOptionPane is from
     * Swing, it won't run normally in the JavaFX Thread.
     * 
     * @param message
     *            The message to display.
     */
    public static void showMessageDialog(final String message)
    {
	EventQueue.invokeLater(new Runnable()
	{
	    @Override
	    public void run()
	    {
		JOptionPane.showMessageDialog(null, message);
	    }
	});
    }

    /**
     * Called when JavaFX is ready to display my content. Creates the entire web
     * browser.
     * 
     * @param stage
     *            The stage to add content to.
     */
    @Override
    public void start(Stage stage)
    {
	this.setStage(stage);
	System.setOut(new CustomPrintStream(this, System.out));
	System.setErr(new CustomErrorStream(this, System.err));

	BorderPane content = new BorderPane();

	setTabs(new TabPane());

	content.setCenter(getTabs());

	tools = new ToolPane(this, bookmarks);
	content.setRight(tools);

	Tab tab = new Tab(homePage);
	tab.setOnClosed(new EventHandler<Event>()
	{

	    @Override
	    public void handle(Event arg0)
	    {
		((GUI) ((Tab) arg0.getTarget()).getContent())
			.getWebViewController().getWebView().setVisible(false);
	    }

	});
	getTabs().getTabs().add(tab);
	if (launchArgs.length == 0)
	    tab.setContent(new GUI(this, homePage, tab));
	else
	    tab.setContent(new GUI(this, launchArgs[0], tab));
	getTabs().getSelectionModel().select(1);

	if (javaVersion >= MIN_JAVA_VERSION)
	    stage.setMaximized(true);

	content.setOnKeyReleased(new MainKeyEventHandler(this));

	stage.setScene(new Scene(content));
	stage.setTitle(TITLE);
	stage.show();
    }

    public ObservableList<BookmarkItem> getBookmarks()
    {
	return bookmarks;
    }

    public void setBookmarks(ObservableList<BookmarkItem> bookmarks)
    {
	this.bookmarks = bookmarks;
    }

    public TabPane getTabs()
    {
	return tabs;
    }

    public void setTabs(TabPane tabs)
    {
	this.tabs = tabs;
    }

    public Stage getStage()
    {
	return stage;
    }

    public void setStage(Stage stage)
    {
	this.stage = stage;
    }

}

/**
 * Handles keypresses on the main class.
 * 
 * @author Akshay
 *
 */
class MainKeyEventHandler implements EventHandler<KeyEvent>
{
    Main main;

    /**
     * Sets up the event handler.
     * 
     * @param main
     */
    public MainKeyEventHandler(Main main)
    {
	this.main = main;
    }

    /**
     * Handles the key event. Takes care of Meta-N (new tab), Meta-T (new tab),
     * and Meta-W (close tab). Meta is Control on Windows and Command on Mac.
     * 
     * @param e
     *            The KeyEvent
     */
    @Override
    public void handle(KeyEvent e)
    {
	if (e.isMetaDown()
		&& (e.getCode() == KeyCode.N || e.getCode() == KeyCode.T))
	{
	    main.newTab(Main.homePage);
	    main.getTabs().getSelectionModel().selectLast();
	}
	else if (e.isMetaDown() && e.getCode() == KeyCode.W)
	{
	    if (main.getTabs().getSelectionModel().getSelectedIndex() > -1)
		main.getTabs()
			.getTabs()
			.remove(main.getTabs().getSelectionModel()
				.getSelectedIndex());
	}
    }

}
