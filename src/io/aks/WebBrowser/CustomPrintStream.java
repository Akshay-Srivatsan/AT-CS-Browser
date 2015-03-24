package io.aks.WebBrowser;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.application.Platform;

/**
 * The class intercepts string console messages and redirects them into the
 * Javascript console as warnings. They will only be visible after Firebug is
 * turned on.
 * 
 * @author Akshay
 *
 */
class CustomPrintStream extends PrintStream
{
    private Main main;

    /**
     * Creates the custom print stream.
     * 
     * @param main
     *            The Main class that created this stream.
     * @param out
     *            An output stream which is used to initialize the print stream.
     *            This should be System.err.
     */
    public CustomPrintStream(Main main, OutputStream out)
    {
	super(out);
	this.main = main;
    }

    /**
     * Sends the message into Javascript, then prints it in Java.
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
			    .getContent()).getWebViewController().warn(error);
	    }
	});
	super.println(error);
    }

}