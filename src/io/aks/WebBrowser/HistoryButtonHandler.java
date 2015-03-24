package io.aks.WebBrowser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Handles navigation within the history pane.
 * 
 * @author Akshay
 *
 */
class HistoryButtonHandler implements EventHandler<ActionEvent>
{
    private String url;
    private HistoryNode node;
    private History history;
    private GUI gui;

    /**
     * Initialize all values.
     */
    public HistoryButtonHandler(GUI gui, String url, HistoryNode node,
	    History history)
    {
	this.gui = gui;
	this.url = url;
	this.node = node;
	this.history = history;
    }

    /**
     * Jump to the selected HistoryNode.
     */
    @Override
    public void handle(ActionEvent e)
    {
	history.setCurrent(node);
	if (node.getBackward() != null)
	    node.getBackward().setPrimary(node);
	gui.loadURL(url, true);

    }

}