package io.aks.WebBrowser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Handles buttons in the navigation pane.
 * 
 * @author Akshay
 *
 */
public class NavigationButtonHandler implements EventHandler<ActionEvent>
{
    private static final String INSPECT_COMMAND = "inspect";
    private static final String REFRESH_COMMAND = "refresh";
    private static final String HOME_COMMAND = "home";
    private static final String FORWARD_COMMAND = "forward";
    private static final String BACK_COMMAND = "back";
    private String action;
    private WebViewController wv;

    /**
     * Initalize the action and WebViewController to act on.
     */
    public NavigationButtonHandler(String action, WebViewController wv)
    {
	this.action = action;
	this.wv = wv;
    }

    /**
     * Does the action specified.
     */
    @Override
    public void handle(ActionEvent arg0)
    {
	if (action.equals(BACK_COMMAND))
	{
	    wv.back();
	}
	else if (action.equals(FORWARD_COMMAND))
	{
	    wv.forward();
	}
	else if (action.equals(HOME_COMMAND))
	{
	    wv.goToURL(Main.homePage);
	}
	else if (action.equals(REFRESH_COMMAND))
	{
	    wv.refresh();
	}
	else if (action.equals(INSPECT_COMMAND))
	{
	    wv.startFireBug();
	}

    }

}
