package io.aks.WebBrowser;
/**
 * Akshay Srivatsan
 * AT CS; C Block
 * Project 1: Build your own web browser.
 * 
 * Classes: History, HistoryNode
 * 
 * 
 */
import java.util.ArrayList;

/**
 * Represents a tree-based history comprised of HistoryNodes.
 * 
 * @author Akshay
 *
 */
public class History
{
    private HistoryNode current;
    private HistoryNode root;
    private GUI gui;

    /**
     * Initialize to a certain location for the root.
     * 
     * @param gui
     *            The GUI that uses this.
     * @param startURL
     *            The URL to start at.
     * @param title
     *            The title to set for the root.
     */
    public History(GUI gui, String startURL, String title)
    {
	this.gui = gui;
	current = new HistoryNode(startURL, title);
	root = getCurrent();
    }

    /**
     * Add a URL as a child of the current node. Uses the URL as the default
     * title.
     * 
     * @param url
     *            The new URL.
     */
    public void addURL(String url)
    {
	getCurrent().addForward(new HistoryNode(url, url));
	current = getCurrent().timeTravelForward();
    }

    /**
     * @return True if this node can go backward.
     */
    public boolean canGoBack()
    {
	return getCurrent().getBackward() != null;
    }

    /**
     * Go Backward.
     */
    public void back()
    {
	current = getCurrent().timeTravelBackward();
	gui.loadURL(getCurrent().getUrl());
    }

    /**
     * @return True if this node can go forward.
     */
    public boolean canGoForward()
    {
	return getCurrent().getForward().size() != 0;
    }

    /**
     * Go forward.
     */
    public void forward()
    {
	current = getCurrent().timeTravelForward();
	gui.loadURL(getCurrent().getUrl());
    }

    /**
     * Create a string version of the tree.
     * 
     * @return the String version.
     */
    public String toString()
    {
	return stringValue(root, 0);
    }

    /**
     * Make a String version of the tree in human-readable format, similar to
     * JSON.
     * 
     * @param node
     *            The node to start with.
     * @param depth
     *            The depth the recursion is currently at.
     * @return A String version of the current node and children
     */
    private String stringValue(HistoryNode node, int depth)
    {
	String retval = "";
	for (int i = 0; i < depth; i++)
	    retval += " ";
	retval += node.toString() + ": {\n";
	for (HistoryNode n : node.getForward())
	{
	    retval += stringValue(n, depth + 1) + "\n";
	}
	for (int i = 0; i < depth; i++)
	    retval += " ";
	retval += "}";
	return retval;
    }

    /**
     * @return the current node
     */
    public HistoryNode getCurrent()
    {
	return current;
    }

    /**
     * Set a new current node.
     * 
     * @param the
     *            new current node
     */
    public void setCurrent(HistoryNode newCurrent)
    {
	current = newCurrent;
    }

    /**
     * @return the root node
     */
    public HistoryNode getRoot()
    {
	return root;
    }
}

/**
 * Represents one item in a History tree.
 * 
 * @author Akshay
 *
 */
class HistoryNode
{
    private HistoryNode backward = null;
    private ArrayList<HistoryNode> forward = new ArrayList<HistoryNode>();
    private final String url;
    private String title;
    public final static int MAXIMUM_TITLE_LENGTH = 30;

    public HistoryNode(String location, String name)
    {
	url = location;
	title = name;
    }

    /**
     * Go forward, if possible.
     * 
     * @return The new current HistoryNode.
     */
    public HistoryNode timeTravelForward()
    {
	if (forward.size() > 0)
	{
	    return forward.get(0);
	}
	return this;
    }

    /**
     * Go backward, if possible.
     * 
     * @return The new current HistoryNode.
     */
    public HistoryNode timeTravelBackward()
    {
	if (backward != null)
	{
	    return backward;
	}
	return this;
    }

    /**
     * Add a forward and go to it.
     * 
     * @return The new current HistoryNode.
     */
    public HistoryNode addForward(HistoryNode newForward)
    {
	forward.add(0, newForward);
	newForward.backward = this;
	return timeTravelForward();
    }

    /**
     * Get the backward node, if it exists.
     * 
     * @return The backward HistoryNode.
     */
    public HistoryNode getBackward()
    {
	if (backward != null)
	{
	    return backward;
	}
	return null;
    }

    /**
     * Get all the forwards as an array.
     * 
     * @return The forwards as an array.
     */
    public HistoryNode[] getForwardAsArray()
    {
	return forward.toArray(new HistoryNode[] {});
    }

    /**
     * Set the primary forward node. May have the side effect of adding the node
     * if it doesn't already exist.
     * 
     * @param primary
     *            The primary node to set.
     */
    public void setPrimary(HistoryNode primary)
    {
	forward.remove(primary);
	forward.add(0, primary);
    }

    /**
     * Set the title of this webpage.
     * 
     * @param name
     *            The new title.
     */
    public void setTitle(String name)
    {
	title = name;
    }

    /**
     * Create a string version of the node.
     */
    public String toString()
    {
	String abbrTitle = title;
	if (abbrTitle.length() > MAXIMUM_TITLE_LENGTH)
	    abbrTitle = abbrTitle.substring(0, MAXIMUM_TITLE_LENGTH - 3)
		    + "...";

	// If you want the URL in the history pane.
	String abbrURL = url;
	if (abbrURL.length() > MAXIMUM_TITLE_LENGTH)
	    abbrURL = abbrURL.substring(0, MAXIMUM_TITLE_LENGTH - 3) + "...";

	return abbrTitle;
    }

    /**
     * @return the title
     */
    String getTitle()
    {
	return title;
    }

    /**
     * @return the forward arraylist
     */
    public ArrayList<HistoryNode> getForward()
    {
	return forward;
    }

    /**
     * @return the url
     */
    String getUrl()
    {
	return url;
    }
}