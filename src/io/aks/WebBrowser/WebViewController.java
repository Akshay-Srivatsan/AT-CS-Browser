package io.aks.WebBrowser;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;

/**
 * This class wraps the final WebView class and provides some utility functions
 * for it.
 * 
 * @author Akshay
 *
 */
public class WebViewController
{
    /*
     * The version numbers in the User Agent are meaningless. Most websites only
     * serve pages to modern web browsers, which they detect by the words
     * "Chrome", "Safari", and "Gecko".
     */
    private static final String CUSTOM_UA_ADDENDUM = " Akshay_Srivatsan_Web_Browser/1.0 MenloSchool/2014.15 Chrome/ProbablyInstalled Safari/MaybeInstalled Gecko/IsALizard (6x9=42) Java/"
	    + Main.javaVersion;
    private WebView webView;
    private WebEngine webEngine;
    private String title;
    private String url;
    private Boolean silent = true;
    private String defaultUA;
    private final GUI gui;
    private final String HOME_PAGE;
    private String originalUA;
    private String customUA;
    private static final String GMAIL_COMPATIBILITY_UA = "Mozilla/5.0 JavaFX/"
	    + System.getProperty("java.version");

    /**
     * Creates a new WebViewController. This method is responsible for making
     * the WebView and getting it into a usable form for the rest of the
     * browser, mainly by adding state change handlers.
     * 
     * @param main
     *            The Main class
     * @param bp
     *            The parent BorderPane
     * @param gui
     *            The GUI that contains this instance.
     * @param start
     *            The URL to start from.
     * @param tab
     *            The containing Tab.
     */
    public WebViewController(final Main main, BorderPane bp, final GUI gui,
	    String start, final Tab tab)
    {
	this.gui = gui;
	HOME_PAGE = start;

	webView = new WebView();
	bp.setCenter(webView);
	webEngine = webView.getEngine();

	if (Main.javaVersion >= Main.MIN_JAVA_VERSION)
	{
	    originalUA = webEngine.getUserAgent();
	    customUA = originalUA + CUSTOM_UA_ADDENDUM;
	    defaultUA = customUA;
	    webEngine.setUserAgent(defaultUA);
	}
	webEngine.load(HOME_PAGE);

	addTitleListener(gui, tab);
	addLocationListener();
	addExceptionListener();
	addStateListener(main, gui, tab);
	webEngine.getHistory().setMaxSize(0); // Disables the built in linear
					      // history.
	addAlertHandler();
	webView.setContextMenuEnabled(false);
	addPopupListener(main);

    }

    /**
     * Displays a popup by creating a new tab.
     * 
     * @param main
     *            The containing Main instance.
     */
    private void addPopupListener(final Main main)
    {
	webEngine
		.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>()
		{

		    @Override
		    public WebEngine call(PopupFeatures arg0)
		    {
			GUI newGUI = main.newTab("about://blank");
			return newGUI.getWebViewController().webEngine;
		    }

		});
    }

    /**
     * Displays a message box.
     */
    private void addAlertHandler()
    {
	webEngine.setOnAlert(new EventHandler<WebEvent<String>>()
	{

	    @Override
	    public void handle(WebEvent<String> arg0)
	    {
		Main.showMessageDialog(arg0.getData());
	    }

	});
    }

    /**
     * Listens for state change events.
     * 
     * @param main
     *            The containing Main instance.
     * @param gui
     *            The containing GUI.
     * @param tab
     *            The containing Tab.
     */
    private void addStateListener(final Main main, final GUI gui, final Tab tab)
    {
	webEngine.getLoadWorker().stateProperty()
		.addListener(new ChangeListener<State>()
		{

		    @Override
		    public void changed(ObservableValue<? extends State> state,
			    State old, State value)
		    {

			if (value == State.SUCCEEDED)
			{
			    /*
			     * If the tab has been closed, redirect to a blank
			     * page to save memory. The silent check is to
			     * prevent infinite recursion.
			     */
			    if (!main.getTabs().getTabs().contains(tab)
				    && !silent)
			    {
				webEngine.load("about://nothing");
				silent = true;
			    }

			    webView.setVisible(true);
			    gui.setURL(webEngine.getLocation());
			    if (!silent)
			    {
				gui.getHistory()
					.addURL(webEngine.getLocation());
				gui.getHistory().getCurrent().setTitle(title);
				gui.updateHistoryPane();
			    }
			    else
			    {
				gui.getHistory().getCurrent().setTitle(title);
				silent = false;
			    }

			    addMouseClickListeners(main, gui);
			}
			else if (value == State.FAILED && !silent)
			{
			    silent = true;
			    webView.setVisible(true);
			    System.err.println("Failure");
			    webEngine
				    .load("<strong>Website Load Failure</strong><p>Sorry, something's wrong.</p>");
			}
			else if (value == State.SCHEDULED)
			{
			    webView.setVisible(false);
			}
		    }

		    /**
		     * Adds click listeners to each hyperlink.
		     * 
		     * @param main
		     *            The containing Main instance.
		     * @param gui
		     *            The containing GUI.
		     */
		    private void addMouseClickListeners(final Main main,
			    final GUI gui)
		    {
			EventListener el = new EventListener()
			{

			    @Override
			    public void handleEvent(Event evt)
			    {
				if (evt.getType().equals("click"))
				{
				    MouseEvent mouseEvt = ((MouseEvent) evt);
				    if (mouseEvt.getMetaKey()
					    || mouseEvt.getButton() == 1)
				    {
					String href = ((Element) mouseEvt
						.getCurrentTarget())
						.getAttribute("href");
					if (href != null)
					{
					    evt.preventDefault();
					}
				    }
				}

			    }

			};
			Document doc = webEngine.getDocument();
			if (doc == null)
			    return;
			NodeList nl = doc.getElementsByTagName("a");
			for (int i = 0; i < nl.getLength(); i++)
			{
			    ((EventTarget) nl.item(i)).addEventListener(
				    "click", el, false);
			}
		    }

		});
    }

    /**
     * Listens for exceptions and displays an error page.
     */
    private void addExceptionListener()
    {
	webEngine.getLoadWorker().exceptionProperty()
		.addListener(new ChangeListener<Throwable>()
		{

		    @Override
		    public void changed(
			    ObservableValue<? extends Throwable> ex,
			    Throwable oldValue, Throwable newValue)
		    {
			if (newValue == null)
			{
			    webEngine
				    .loadContent("<strong>Unknown Error</strong>");
			    System.err.println("Unknown Error");
			}
			else
			{
			    webEngine.loadContent("<strong>Error: "
				    + newValue.getMessage()
				    + "</strong><br><p>Make sure you typed the URL correctly.</p>");
			    System.err.println("Error: "
				    + newValue.getMessage());
			}
		    }

		});
    }

    /**
     * Listens for location changes. If gmail, use an old user agent for
     * compatibility.
     */
    private void addLocationListener()
    {
	webEngine.locationProperty().addListener(new ChangeListener<String>()
	{

	    @Override
	    public void changed(ObservableValue<? extends String> observable,
		    String oldValue, String newValue)
	    {
		url = newValue;
		if (newValue == null)
		    url = "about://null";
		if (url.contains("mail.google.com"))
		{
		    if (Main.javaVersion >= 8)
			webEngine.setUserAgent(GMAIL_COMPATIBILITY_UA);

		}
		else
		{
		    if (Main.javaVersion >= 8)
			webEngine.setUserAgent(defaultUA);
		}
	    }

	});
    }

    /**
     * Listens for changes to the window title.
     * 
     * @param gui
     *            The containing GUI.
     * @param tab
     *            The containing Tab.
     */
    private void addTitleListener(final GUI gui, final Tab tab)
    {
	webEngine.titleProperty().addListener(new ChangeListener<String>()
	{

	    @Override
	    public void changed(ObservableValue<? extends String> observable,
		    String oldValue, String newValue)
	    {
		if (newValue == null)
		    return;
		title = newValue;

		String abbrTitle = title;
		if (abbrTitle.length() > HistoryNode.MAXIMUM_TITLE_LENGTH)
		    abbrTitle = abbrTitle.substring(0,
			    HistoryNode.MAXIMUM_TITLE_LENGTH - 3) + "...";

		gui.updateHistoryPane();
		tab.setText(abbrTitle);

	    }

	});
    }

    /**
     * Go back in the history.
     */
    public void back()
    {
	if (!gui.getHistory().canGoBack())
	    return;
	silent = true;
	gui.getHistory().back();
    }

    /**
     * Go forward to the primary child of the current node.
     */
    public void forward()
    {
	if (!gui.getHistory().canGoForward())
	    return;
	silent = true;
	gui.getHistory().forward();
    }

    /**
     * Reload the page.
     */
    public void refresh()
    {
	webEngine.reload();
    }

    /**
     * Navigate to a new location.
     * 
     * @param url
     *            The URL to go to.
     */
    public void goToURL(final String url)
    {
	gui.getHistory().addURL(url);
	gui.setURL(url);
	webEngine.load(url);
    }

    /**
     * Load a url without adding to history.
     * 
     * @param url
     *            The URL to go to.
     */
    public void loadURL(final String url)
    {
	gui.setURL(url);
	webEngine.load(url);
    }

    /**
     * Load a url without adding to history.
     * 
     * @param url
     *            The URL to go to.
     * @param runSilent
     *            Whether this should be in "slient mode" (none of the changes
     *            are recorded in the history) or not.
     */
    public void loadURL(final String url, final boolean runSilent)
    {
	silent = runSilent;
	gui.setURL(url);
	webEngine.load(url);
    }

    /**
     * Load HTML content.
     * 
     * @param html
     *            The HTML to load.
     */
    public void loadHTML(final String html)
    {
	webEngine.loadContent(html);
    }

    /**
     * Makes a URL based on an absolute URL and a relative URL.
     * 
     * @param current
     *            The absolute URL.
     * @param relative
     *            The relative URL.
     * @return The URL as a String.
     */
    public static String toURL(String current, String relative)
    {
	try
	{
	    URL url;
	    if (relative == null)
		url = new URL(current);
	    else if (!relative.matches("$.*://\\..*^"))
		url = new URL(new URL(current), relative);
	    else
		url = new URL(relative);
	    return url.toExternalForm();
	}
	catch (MalformedURLException e)
	{
	    e.printStackTrace();
	    return relative;
	}
    }

    /**
     * Launches the Javascript inspector.
     */
    public void startFireBug()
    {
	webView.getEngine()
		.executeScript(
			"if (typeof Firebug == 'undefined' || Firebug.context == null) {var firebug=document.createElement('script');firebug.setAttribute('src','http://getfirebug.com/releases/lite/1.4/firebug-lite.js#startOpened=true');document.body.appendChild(firebug);} else {Firebug.chrome.toggle();}");
    }

    /**
     * Logs to the Javascript console.
     * 
     * @param val
     *            The value to log.
     */
    public void log(String val)
    {
	webView.getEngine().executeScript("console.log('" + val + "');");
    }

    /**
     * Prints an error to the Javascript console.
     * 
     * @param val
     *            The value to print.
     */
    public void error(String val)
    {
	webView.getEngine().executeScript("console.error('" + val + "');");
    }

    /**
     * Prints a warning to the Javascript console.
     * 
     * @param val
     *            The value to print.
     */
    public void warn(String val)
    {
	webView.getEngine().executeScript("console.warn('" + val + "');");
    }

    /**
     * Prints an info message to the Javascript console.
     * 
     * @param val
     *            The value to print.
     */
    public void info(String val)
    {
	webView.getEngine().executeScript("console.info('" + val + "');");
    }

    /**
     * Get the current WebView.
     * 
     * @return the WebView
     */
    public WebView getWebView()
    {
	return webView;
    }

    /**
     * Get the current WebView's WebEngine
     * 
     * @return the WebEngine
     */
    public WebEngine getWebEngine()
    {
	return webEngine;
    }
}
