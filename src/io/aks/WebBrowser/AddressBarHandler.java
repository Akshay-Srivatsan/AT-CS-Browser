package io.aks.WebBrowser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

/**
 * This class handles the user pressing "enter" on the address bar.
 * 
 * @author Akshay
 *
 */
public class AddressBarHandler implements EventHandler<ActionEvent>
{
    private static final String HTTP = "http://";
    private static final String HAS_PROTOCOL_REGEX = "^.*://.*";
    private static final String JAVASCRIPT_SCHEME_2 = "js://";
    private static final String JAVASCRIPT_SCHEME_1 = "javascript://";
    /**
     * Matches most common top-level-domains.
     */
    private static final String DOMAIN_REGEX = "(localhost|.*\\.(com|org|edu|gov|uk|net|ca|de|jp|fr|au|us|ru|ch|it|br|jp|pl|info|cn|in|biz|tv|me|io|cc|local|[0-9]*).*)";
    /**
     * The url to use for searching. Combined with the actual query using printf
     * format (%s is replaced by the query).
     */
    private static final String SEARCH_URL = "http://google.com/search?q=%s";
    private final GUI gui;

    /**
     * Initializes the handler.
     * 
     * @param gui
     *            The GUI that contains the address bar.
     */
    AddressBarHandler(GUI gui)
    {
	this.gui = gui;
    }

    @Override
    /**
     * Identifies if the address bar content is a valid url. If so, navigate to it. If not, do a google search for it. If the url is a javascript url (begins with "javascript://" or "js://"), it executes the script instead.
     */
    public void handle(ActionEvent arg0)
    {
	String url = ((TextField) arg0.getSource()).getText();
	if (url.startsWith(JAVASCRIPT_SCHEME_1))
	{
	    this.gui.getWebViewController().getWebEngine()
		    .executeScript(url.substring(13));
	    return;
	}
	else if (url.startsWith(JAVASCRIPT_SCHEME_2))
	{
	    this.gui.getWebViewController().getWebEngine()
		    .executeScript(url.substring(5));
	    return;
	}
	final String domainNameRegex = DOMAIN_REGEX;
	if (!url.matches(HAS_PROTOCOL_REGEX) && url.matches(domainNameRegex))
	    url = HTTP + url;
	else if (!url.matches(HAS_PROTOCOL_REGEX)
		&& !url.matches(domainNameRegex))
	    url = String.format(SEARCH_URL, url.replace(' ', '+'));
	this.gui.loadURL(url);

    }
}