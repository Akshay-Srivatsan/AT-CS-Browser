package io.aks.WebBrowser;
import java.awt.BorderLayout;
import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javax.swing.JPanel;

/**
 * This class is a web browser. It handles loading pages and other basic
 * features, as well as history. It is designed to work with the Main class, but
 * could be modified to function without it.
 * 
 * @author Akshay
 *
 */
public class GUI extends BorderPane
{
    private static final String DEFAULT_TITLE = "New Tab";
    private TextField addressBar;
    private BorderPane historyPane;
    private ScrollPane historyScrollPane;
    private Button backButton;
    private Button forwardButton;
    private WebViewController webViewController;
    private History history;
    private Main main;

    /**
     * Creates a GUI, with history, navigation buttons, and a Web View.
     * 
     * @param main
     *            The containing Main class.
     * @param start
     *            The url to start on.
     * @param tab
     *            The containing tab.
     */
    public GUI(Main main, String start, Tab tab)
    {
	history = new History(this, start, DEFAULT_TITLE);
	this.main = main;

	JPanel tools = new JPanel();
	tools.setLayout(new BorderLayout());

	BorderPane bp = new BorderPane();
	BorderPane navPane = new BorderPane();

	webViewController = new WebViewController(main, bp, this, start, tab);
	
	bp.setTop(navPane);
	navPane.setLeft(createNavTools());

	createMainTools(bp, navPane);

	this.setOnKeyReleased(new KeyEventHandler(this));
	this.setCenter(bp);
    }

    /**
     * Creates the address bar and history box.
     * 
     * @param bp
     *            The BorderPane that contains the history scroll pane.
     * @param navPane
     *            The BorderPane that contains the address bar.
     */
    private void createMainTools(BorderPane bp, BorderPane navPane)
    {
	addressBar = new TextField();
	addressBar.setOnAction(new AddressBarHandler(this));
	navPane.setCenter(addressBar);

	historyPane = new BorderPane();
	historyPane.setMaxWidth(200);
	historyScrollPane = new ScrollPane(historyPane);
	historyScrollPane.setVisible(true);
	historyScrollPane.setManaged(true);
	historyScrollPane.setMaxWidth(200);
	bp.setRight(historyScrollPane);
    }

    /**
     * Creates back forward, home, refresh, and inspect buttons.
     * 
     * @return A HBox containing the buttons.
     */
    private HBox createNavTools()
    {
	HBox hb = new HBox();

	hb.setPadding(new Insets(5, 10, 5, 10));
	hb.setSpacing(10);

	backButton = new Button("<");
	backButton.setOnAction(new NavigationButtonHandler("back",
		webViewController));
	hb.getChildren().add(backButton);

	forwardButton = new Button(">");
	forwardButton.setOnAction(new NavigationButtonHandler("forward",
		webViewController));
	hb.getChildren().add(forwardButton);

	Button homeButton = new Button();
	homeButton.setOnAction(new NavigationButtonHandler("home",
		webViewController));
	Image homeImage = new Image(getClass().getResourceAsStream("Home.png"),
		12, 12, true, false);
	ImageView home = new ImageView(homeImage);
	homeButton.setGraphic(home);
	hb.getChildren().add(homeButton);

	Button refreshButton = new Button();
	refreshButton.setOnAction(new NavigationButtonHandler("refresh",
		webViewController));
	Image refreshImage = new Image(getClass().getResourceAsStream(
		"Refresh.png"), 12, 12, false, true);
	ImageView refresh = new ImageView(refreshImage);
	refreshButton.setGraphic(refresh);
	hb.getChildren().add(refreshButton);

	Button inspectButton = new Button("{}");
	inspectButton.setOnAction(new NavigationButtonHandler("inspect",
		webViewController));
	hb.getChildren().add(inspectButton);
	return hb;
    }

    /**
     * Navigate to a URL.
     * 
     * @param url
     *            The URL to navigate to.
     */
    public void goToURL(String url)
    {
	webViewController.goToURL(url);
    }

    /**
     * Update content to reflect new location (i.e., after the user clicked a
     * link).
     * 
     * @param url
     *            The new URL.
     */
    public void setURL(String url)
    {
	addressBar.setText(url);
	updateHistoryPane();
    }

    /**
     * Load a URL without affecting the GUI's other content.
     * 
     * @param url
     *            The URL.
     */
    public void loadURL(String url)
    {
	webViewController.loadURL(url);
    }

    /**
     * Load a URL without adding it to history.
     * 
     * @param url
     *            The URL.
     * @param silent
     *            If true, don't add to history.
     */
    public void loadURL(String url, boolean silent)
    {
	webViewController.loadURL(url, silent);
    }

    /**
     * The first method called in the updateHistoryPane recursive sequence. This
     * sets up the pane for the root, then calls the recursive method.
     */
    public void updateHistoryPane()
    {
	if (getHistory().getRoot() == null)
	    return;
	backButton.setDisable(!getHistory().canGoBack());
	forwardButton.setDisable(!getHistory().canGoForward());
	historyPane.getChildren().removeAll(historyPane.getChildren());

	Label home = new Label();
	home.setText(getHistory().getRoot().getTitle());
	home.setAlignment(Pos.CENTER);
	historyPane.setTop(home);

	BorderPane newPane = new BorderPane();
	historyPane.setCenter(newPane);
	updateHistoryPane(newPane, getHistory().getRoot(), getHistory());
    }

    /**
     * The recursive updateHistoryPane method. This creates a tabview for all of
     * the node's children.
     * 
     * @param panel
     *            The panel to add to.
     * @param currentNode
     *            The current node in the sequence.
     * @param history
     *            The history to use.
     */
    public void updateHistoryPane(BorderPane panel, HistoryNode currentNode,
	    History history)
    {
	panel.setMaxWidth(200);
	BorderPane topPane = new BorderPane();
	Button jump = new Button("->");
	jump.setOnAction(new HistoryButtonHandler(this, currentNode.getUrl(),
		currentNode, history));
	topPane.setTop(jump);
	panel.setTop(topPane);

	if (history.getCurrent() == currentNode)
	{
	    jump.setDisable(true);
	    jump.setText("You are here.");

	}

	ArrayList<HistoryNode> historyItems = currentNode.getForward();
	if (historyItems.size() == 0)
	{
	    return;
	}

	TabPane tabs = new TabPane();
	topPane.setBottom(tabs);
	for (HistoryNode i : historyItems)
	{
	    String abbrTitle = i.getTitle();
	    if (abbrTitle.length() > 30)
		abbrTitle = abbrTitle.substring(0, 30) + "...";
	    Tab tab = new Tab(abbrTitle);
	    tab.setClosable(false);
	    BorderPane currentPane = new BorderPane();

	    updateHistoryPane(currentPane, i, history);
	    tab.setContent(currentPane);
	    tabs.getTabs().add(tab);

	}

    }

    public void toggleHistory()
    {
	if (historyScrollPane.isVisible())
	{
	    historyScrollPane.setVisible(false);
	    historyScrollPane.setManaged(false);
	}
	else
	{
	    historyScrollPane.setVisible(true);
	    historyScrollPane.setManaged(true);
	}
    }

    public void print()
    {
	if (Main.javaVersion < Main.MIN_JAVA_VERSION)
	    Main.showMessageDialog("Printing is a feature added in Java 8");
	PrinterJob print = PrinterJob.createPrinterJob();
	if (print != null)
	{
	    print.showPrintDialog(main.getStage());
	    getWebViewController().getWebEngine().print(print);
	    print.endJob();
	}
    }

    /**
     * @return This GUI's WebViewController
     */
    public WebViewController getWebViewController()
    {
	return webViewController;
    }

    /**
     * @return This GUI's History
     */
    public History getHistory()
    {
	return history;
    }

    /**
     * @return This GUI's Main class
     */
    public Main getMain()
    {
	return main;
    }

}

class KeyEventHandler implements EventHandler<KeyEvent>
{
    GUI gui;

    public KeyEventHandler(GUI gui)
    {
	this.gui = gui;
    }

    @Override
    public void handle(KeyEvent e)
    {
	if (e.isMetaDown() && e.getCode() == KeyCode.Y)
	{
	    gui.toggleHistory();
	}
	else if (e.isMetaDown() && e.getCode() == KeyCode.P)
	{
	    gui.print();
	}
	else if (e.isMetaDown() && e.getCode() == KeyCode.RIGHT)
	{
	    gui.getWebViewController().back();
	}
	else if (e.isMetaDown() && e.getCode() == KeyCode.LEFT)
	{
	    gui.getWebViewController().forward();
	}
    }

}