package io.aks.WebBrowser;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * This class is the toolbar on the right of the browser, which is always
 * visible. Contains features for making a new tab, opening history, setting the
 * home page, using bookmarks, and printing.
 * 
 * @author Akshay
 *
 */
public class ToolPane extends VBox
{
    /**
     * Create the Toolpane
     * 
     * @param main
     *            The main class this is a member of.
     * @param bookmarks
     *            The bookmarks to use in the dropdown.
     */
    public ToolPane(final Main main, ObservableList<BookmarkItem> bookmarks)
    {
	newTabButton(main);

	BorderPane row2 = new BorderPane();
	Button history = historyButton(main);
	row2.setLeft(history);

	Button setHome = setHomeButton(main);
	row2.setRight(setHome);

	this.getChildren().add(row2);

	bookmarkTools(main, bookmarks);

	printButton(main);

    }

    /**
     * Creates an animated print button.
     * 
     * @param main
     *            The Main class.
     */
    private void printButton(final Main main)
    {
	Button printButton = new Button("Print This Page");
	StackPane printGraphic = new StackPane();
	new PrintAnimation(printGraphic);
	printButton.setMaxWidth(Double.MAX_VALUE);
	printButton.setGraphic(printGraphic);
	printButton.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		((GUI) main.getTabs().getSelectionModel().getSelectedItem()
			.getContent()).print();
	    }

	});
	this.getChildren().add(printButton);
    }

    /**
     * Creates the bookmark tools (open, add, delete). These buttons rely on
     * each other's existence, which is why the code in this method is long and
     * messy.
     * 
     * @param main
     *            The Main class.
     * @param bookmarks
     *            The bookmarks.
     */
    private void bookmarkTools(final Main main,
	    ObservableList<BookmarkItem> bookmarks)
    {

	BorderPane row4 = new BorderPane();
	final ComboBox<BookmarkItem> bookmarkBox = new ComboBox<BookmarkItem>(
		bookmarks);
	bookmarkBox.setPrefWidth(175);
	bookmarkBox.getSelectionModel().selectFirst();
	this.getChildren().add(bookmarkBox);

	Button loadBookmark = new Button("Go!");
	loadBookmark.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		main.newTab(bookmarkBox.getSelectionModel().getSelectedItem()
			.getURL());
		main.getTabs().getSelectionModel().selectLast();
	    }
	});
	row4.setLeft(loadBookmark);

	Button addBookmark = new Button("Add");
	addBookmark.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		HistoryNode currentHistoryNode = ((GUI) main.getTabs()
			.getSelectionModel().getSelectedItem().getContent())
			.getHistory().getCurrent();
		BookmarkItem newBookmark = new BookmarkItem(currentHistoryNode
			.getUrl(), currentHistoryNode.getTitle());
		main.getBookmarks().add(newBookmark);
		bookmarkBox.getSelectionModel().select(newBookmark);
	    }

	});
	row4.setCenter(addBookmark);

	final HBox areYouSure = new HBox();
	areYouSure.setVisible(false);

	Button confirm = new Button("Confirm Delete");
	confirm.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		main.getBookmarks().remove(
			bookmarkBox.getSelectionModel().getSelectedItem());
		areYouSure.setVisible(false);
		areYouSure.setManaged(false);
	    }

	});
	confirm.setMaxWidth(Double.MAX_VALUE);
	areYouSure.getChildren().add(confirm);

	Button cancel = new Button("Cancel");
	cancel.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		areYouSure.setVisible(false);
		areYouSure.setManaged(false);
	    }

	});
	cancel.setMaxWidth(Double.MAX_VALUE);
	areYouSure.getChildren().add(cancel);

	Button removeBookmark = new Button("X");
	removeBookmark.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		areYouSure.setVisible(true);
		areYouSure.setManaged(true);
	    }

	});
	row4.setRight(removeBookmark);

	this.getChildren().add(row4);
	this.getChildren().add(areYouSure);
	areYouSure.setManaged(false);
    }

    /**
     * Create a "Set Home" button.
     * 
     * @param main
     *            The main class.
     * @return The button.
     */
    private Button setHomeButton(final Main main)
    {
	Button setHome = new Button("Set Home");
	setHome.setGraphic(new ImageView(new Image(main.getClass()
		.getResourceAsStream("Home.png"), 33, 33, true, false)));
	setHome.setTooltip(new Tooltip("Set the Home Page"));
	setHome.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		try
		{
		    Main.homePage = ((GUI) main
			    .getTabs()
			    .getTabs()
			    .get(main.getTabs().getSelectionModel()
				    .getSelectedIndex()).getContent())
			    .getWebViewController().getWebEngine()
			    .getLocation();
		    Main.showMessageDialog("New home page set!\n"
			    + Main.homePage);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{

		}
	    }
	});
	return setHome;
    }

    /**
     * Creates an animated "Show History" Button.
     * 
     * @param main
     *            The Main class.
     * @return the button
     */
    private Button historyButton(final Main main)
    {
	Button history = new Button();
	history.setTooltip(new Tooltip("Show History"));
	history.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		try
		{
		    ((GUI) main
			    .getTabs()
			    .getTabs()
			    .get(main.getTabs().getSelectionModel()
				    .getSelectedIndex()).getContent())
			    .toggleHistory();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{

		}

	    }

	});
	StackPane historySP = new StackPane();
	new HistoryAnimation(historySP);
	history.setGraphic(historySP);
	return history;
    }

    /**
     * Creates a "New Tab" Button.
     * 
     * @param main
     *            The button.
     */
    private void newTabButton(final Main main)
    {
	this.setPadding(new Insets(5, 5, 5, 5));
	this.setSpacing(5);
	Button newTab = new Button("+");
	newTab.setTooltip(new Tooltip("Open a New Tab"));
	newTab.setOnAction(new EventHandler<ActionEvent>()
	{

	    @Override
	    public void handle(ActionEvent arg0)
	    {
		main.newTab(Main.homePage);
		main.getTabs().getSelectionModel().selectLast();
	    }

	});
	newTab.setMaxWidth(Double.MAX_VALUE);
	this.getChildren().add(newTab);
    }
}

/**
 * Allows for the History Animation. Interpolate is called automatically with
 * values ranging from 0 to 1 over the course of 48 seconds.
 * 
 * @author Akshay
 *
 */
class HistoryAnimation extends Transition
{
    StackPane sp;

    public HistoryAnimation(StackPane sp)
    {
	this.sp = sp;
	this.setCycleDuration(Duration.millis(48000));
	this.setInterpolator(Interpolator.LINEAR);
	this.play();
    }

    @Override
    protected void interpolate(double pos)
    {
	sp.getChildren().clear();
	double w = 33;
	double h = 33;

	Canvas canvas = new Canvas(w, h);
	GraphicsContext g = canvas.getGraphicsContext2D();
	g.strokeOval(1, 1, w - 2, h - 2);
	double posMinDeg = -pos * 360 * 12;
	g.strokeLine(w / 2, h / 2, Math.cos(Math.toRadians(posMinDeg)) * 15 + w
		/ 2, Math.sin(Math.toRadians(posMinDeg)) * 15 + w / 2);
	sp.getChildren().add(canvas);

	canvas = new Canvas(w, h);
	g = canvas.getGraphicsContext2D();
	g.setLineCap(StrokeLineCap.ROUND);
	g.setLineWidth(2);
	double posHoursDeg = -pos * 360;
	g.strokeLine(w / 2, h / 2, Math.cos(Math.toRadians(posHoursDeg)) * 7
		+ w / 2, Math.sin(Math.toRadians(posHoursDeg)) * 7 + w / 2);
	sp.getChildren().add(canvas);

	if (pos == 1)
	    this.playFromStart();
    }

}

/**
 * Allows for the Print Animation. Interpolate is called automatically with
 * values from 0 to 1 over 3 seconds.
 * 
 * @author Akshay
 *
 */
class PrintAnimation extends Transition
{
    StackPane sp;
    double w = 33;
    double h = 33;
    Image print = new Image(getClass().getResourceAsStream("Print.png"), w, h,
	    false, true);

    public PrintAnimation(StackPane sp)
    {
	this.sp = sp;
	this.setCycleDuration(Duration.millis(3000));
	this.setInterpolator(Interpolator.LINEAR);
	this.play();
    }

    @Override
    protected void interpolate(double pos)
    {
	sp.getChildren().clear();

	Canvas canvas = new Canvas(w, h);
	GraphicsContext g = canvas.getGraphicsContext2D();
	g.drawImage(print, 0, 0);
	sp.getChildren().add(canvas);

	double pos1 = Math.min(pos * 3, 1);
	double pos2 = Math.min(Math.max(0, (pos - 1 / 3.0) * 3), 1);

	g.setFill(Paint.valueOf("blue"));
	g.setLineWidth(2);
	if (pos > 2 / 3.0)
	    g.setLineWidth(6 * (4 / 3.0 - pos - 1 / 3.0));
	g.setLineCap(StrokeLineCap.ROUND);

	g.strokeLine(w / 4.0 + 3, h * 2 / 3.0 + 1, w / 4.0 + 3 + (w / 2.0 - 5)
		* pos1, h * 2 / 3.0 + 1);
	if (pos2 > 0)
	    g.strokeLine(w / 4.0 + 3, h * 2 / 3.0 + 5, w / 4.0 + 3
		    + (w / 2.0 - 5) * pos2, h * 2 / 3.0 + 5);

	if (pos == 1)
	    this.playFromStart();
    }

}
