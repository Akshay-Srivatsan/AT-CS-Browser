package io.aks.WebBrowser;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.application.Platform;

/**
 * The class intercepts string error messages and redirects them into the
 * Javascript console as error messages. They will only be visible after Firebug
 * is turned on.
 * 
 * @author Akshay
 *
 */
public class CustomErrorStream extends PrintStream
{
    private Main main;

    /**
     * Creates the custom error stream.
     * 
     * @param main
     *            The Main class that created this stream.
     * @param out
     *            An output stream which is used to initialize the print stream.
     *            This should be System.err.
     */
    public CustomErrorStream(Main main, OutputStream out)
    {
	super(out);
	this.main = main;
    }

    /**
     * Sends the error into Javascript, then prints it in Java.
     * 
     * @param error
     *            The error message.
     */
    @Override
    public void println(String error)
    {
	Platform.runLater(new Runnable()
	{
	    @Override
	    public void run()
	    {
		if (!main.getTabs().getSelectionModel().isEmpty())
		    ((GUI) main.getTabs().getSelectionModel().getSelectedItem()
			    .getContent()).getWebViewController().error(error);
	    }
	});
	super.println(error);
    }

}