package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.InvertedIndex.SingleSearchResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Outputs and responds to HTML form.
 */
public class SearchServlet extends HttpServlet {
    /** Class version for serialization, in [YEAR][TERM] format (unused). */
    private static final long serialVersionUID = 202210;

    /** Title of the webpage */
    private static final String TITLE = "Ale's Search";

    /**
     * The logger to use (Jetty is configured via the pom.xml to use Log4j2)
     */
    public static Logger log = LogManager.getLogger();

    /**
     * inverted Index
     */
    InvertedIndex index;

    /** Template for HTML. **/
    private final String htmlTemplate;

    /** The thread-safe data structure to use for storing messages. */
    private final ConcurrentLinkedQueue<String> searches;

    /** Base path with HTML templates. */
    private static final Path BASE = Path.of("src", "main", "java", "edu", "usfca", "cs272");

    /**
     * @param index to use
     * @throws IOException if unable to read template
     */
    public SearchServlet(InvertedIndex index) throws IOException {
        super();
        this.index = index;
        searches = new ConcurrentLinkedQueue<>();
        htmlTemplate = Files.readString(BASE.resolve("index.html"), UTF_8);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.info("{} handling: {}", this.hashCode(), request);
        Map<String, String> values = new HashMap<>();
        values.put("title", TITLE);
        values.put("thread", Thread.currentThread().getName());

        // setup form
        values.put("method", "POST");
        values.put("action", request.getServletPath());
        // compile all of the messages together
        // keep in mind multiple threads may access this at once!
        values.put("search", String.join("\n\n", searches));

        // generate html from template
        StringSubstitutor replacer = new StringSubstitutor(values);
        String html = replacer.replace(htmlTemplate);

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        // output generated html
        PrintWriter out = response.getWriter();

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        out.println(html);
        out.flush();

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("word");
        List<SingleSearchResult> result = null;
        if (query == null || query.isBlank()) {
            query = "";
        } else {
            String[] words = query.split(" ");

            Set<String> set = new HashSet<>();
            for (String word : words) {
                word = StringEscapeUtils.escapeHtml4(word);
                set.add(word);
            }
            result = index.partialSearch(set);
            for (SingleSearchResult search : result) {
                String formatted = String.format(
                        "<p>Location:%s Score: %s Query Count: %s<br><font size=\"-2\">[ posted at %s ]</font></p>",
                        search.getLocation(), search.getScore(), search.getQueryCount(), getDate());
                searches.add(formatted);

            }
        }

        response.sendRedirect(request.getServletPath());
    }

    /**
     * Returns the date and time in a long format. For example: "12:00 am on
     * Saturday, January 01 2000".
     *
     * @return current date and time
     */
    public static String getDate() {
        String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return today.format(formatter);
    }
}