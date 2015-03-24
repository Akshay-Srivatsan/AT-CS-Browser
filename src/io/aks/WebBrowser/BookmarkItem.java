package io.aks.WebBrowser;
/**
 * Represents a title-url pair for a bookmark.
 * 
 * @author Akshay
 *
 */
public class BookmarkItem
{
    private static final char PREFERENCES_SEPARATOR = '\t';
    private static final String TOSTRING_SEPARATOR = " – ";
    private String url;
    private String title;

    /**
     * Initialize the Bookmark.
     * 
     * @param url
     *            The URL.
     * @param title
     *            The Title.
     */
    public BookmarkItem(String url, String title)
    {
	this.url = url;
	this.title = title;
    }

    /**
     * Create a string representation of the object.
     */
    public String toString()
    {
	return this.title + TOSTRING_SEPARATOR + this.url;
    }

    /**
     * Get the URL component.
     * 
     * @return The URL.
     */
    public String getURL()
    {
	return url;
    }

    /**
     * Get the title component.
     * 
     * @return The title.
     */
    public String getTitle()
    {
	return title;
    }

    /**
     * Encode a bookmark into a single-line format.
     * 
     * @param bookmark
     *            The BookmarkItem to encode.
     * @return The String value.
     */
    public static String encode(BookmarkItem bookmark)
    {
	return bookmark.title + PREFERENCES_SEPARATOR + bookmark.url;
    }

    /**
     * Decode a bookmark from a single-line format.
     * 
     * @param string
     *            The String to decode.
     * @return The BookmarkItem that was encoded.
     */
    public static BookmarkItem decode(String string)
    {
	int split = string.indexOf(PREFERENCES_SEPARATOR);
	return new BookmarkItem(string.substring(0, split),
		string.substring(split + 1));
    }
}
