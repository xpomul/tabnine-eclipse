package net.winklerweb.tabnine.core.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.osgi.service.component.annotations.Component;

import com.tabnine.Log;
import com.tabnine.binary.requests.autocomplete.AutocompleteRequest;
import com.tabnine.binary.requests.autocomplete.ResultEntry;
import com.tabnine.general.DependencyContainer;
import com.tabnine.general.StaticConfig;

import net.winklerweb.tabnine.core.CompletionProposal;
import net.winklerweb.tabnine.core.ICommentInjectionService;
import net.winklerweb.tabnine.core.ITabnineCompletionService;

/**
 * Implementation of the {@link ITabnineCompletionService}.
 * 
 * @author Stefan Winkler
 */
@Component
public class TabnineCompletionService implements ITabnineCompletionService {

	/**
	 * A regex pattern that matches a completion detail percent value
	 */
	private static final Pattern PERCENT_PATTERN = Pattern.compile("\\s*(\\d+)%");

	/**
	 * A regex pattern that matches a completion detail double value
	 */
	private static final Pattern DOUBLE_PATTERN = Pattern.compile("(\\d\\.\\d+)");
	
	@Override
	public List<CompletionProposal> complete(ITextViewer viewer, int offset, String path) {

		// get the lowlevel facade to send the request
		var requestFacade = DependencyContainer.instanceOfBinaryRequestFacade();

		// prepare the request
		var request = new AutocompleteRequest();

		// calculate the number context characters for the current cursor position and
		// document
		// up to 100k characters (StaticConfig.MAX_OFFSET) before and after the cursor
		// position
		var document = viewer.getDocument();
		var prefixStart = Math.max(offset - StaticConfig.MAX_OFFSET, 0);
		var suffixEnd = Math.min(offset + StaticConfig.MAX_OFFSET, document.getLength());

		// check whether the whole beginning and/or end of the document are included in
		// the request
		request.regionIncludesBeginning = prefixStart == 0;
		request.regionIncludesEnd = suffixEnd == document.getLength();

		StringBuilder prefixBuilder = new StringBuilder();
		ServiceCaller.callOnce(getClass(), ICommentInjectionService.class, service -> {
			service.getCurrentInjectionComment().ifPresent(comment -> {
				prefixBuilder.append("/* ").append(comment).append(" */\n\n");
			});
		});
		
		try {
			// set the context before and after the current cursor position to the request
			prefixBuilder.append(document.get(prefixStart, offset - prefixStart));
			request.before = prefixBuilder.toString();
			request.after = document.get(offset, suffixEnd - offset);

			// provide filename and file location information
			request.filename = path;
			request.offset = offset;
			request.line = document.getLineOfOffset(offset);
			request.character = offset - document.getLineOffset(request.line);
		} catch (BadLocationException e) {
			Log.error("Error while reading text from file", e);
			return Collections.emptyList();
		}

		// finally set the maximum results and submit the request
		request.maxResults = StaticConfig.MAX_COMPLETIONS;
		var response = requestFacade.executeRequest(request);
		if (response == null) {
			// if no response, then just return an empty list
			return Collections.emptyList();
		}

		// If there are user messages, then report them in the error log view
		// (TODO: show them along with the proposals?)
		Stream.of(response.user_message).map(s -> "Tabnine Message: " + s).forEach(Platform.getLog(getClass())::info);

		// Group the response by completion string
		// Sometimes TabNine reports the same proposal from local and cloud models, so
		// we eliminate these duplicates
		var responseByCompletion = Stream.of(response.results)
				.collect(Collectors.toMap(e -> e.new_prefix + e.new_suffix, Function.identity(), (u, v) -> u));

		// Then filter out empty proposals, sort by the "detail" property, and convert
		// to a list of CompletionProposal objects.
		var sortedCompletionProposals = responseByCompletion.entrySet().stream().filter(e -> !e.getKey().isBlank())
				.map(Map.Entry::getValue).sorted(this::compareResults)
				.map(e -> createCompletionProposal(viewer, offset, response.old_prefix, e))
				.collect(Collectors.toList());

		return sortedCompletionProposals;
	}

	/**
	 * Create a {@link CompletionProposal} object from the given properties
	 * 
	 * @param viewer         the viewer to which this proposal applies
	 * @param cursorPosition the current cursor position to which this proposal
	 *                       applies
	 * @param old_prefix     the old_prefix that is common to all proposals
	 * @param entry          the concrete proposal result received from the TabNine
	 *                       binary.
	 * @return the {@link CompletionProposal} object created.
	 */
	private CompletionProposal createCompletionProposal(ITextViewer viewer, int cursorPosition, String old_prefix,
			ResultEntry entry) {
		var prefixOverlap = old_prefix.length();
		var replacementLength = old_prefix.length() + entry.old_suffix.length();
		var cursorOffset = entry.new_prefix.length() - old_prefix.length();
		return new CompletionProposal(viewer, cursorPosition, entry.new_prefix + entry.new_suffix, prefixOverlap,
				replacementLength, cursorOffset);
	}

	/**
	 * Comparison function to compare two {@link ResultEntry} objects by comparing
	 * the "detail" property.
	 * 
	 * @param e1 the first {@link ResultEntry}
	 * @param e2 the second {@link ResultEntry}
	 * @return the comparison result
	 */
	private int compareResults(ResultEntry e1, ResultEntry e2) {
		if (e1.completion_metadata == null && e2.completion_metadata != null) {
			return 1;
		} else if (e1.completion_metadata != null && e2.completion_metadata == null) {
			return -1;
		} else if (e1.completion_metadata == null) {
			return Integer.compare(e1.hashCode(), e2.hashCode());
		}

		var d1 = convertDetail(e1.completion_metadata.getDetail());
		var d2 = convertDetail(e2.completion_metadata.getDetail());

		// descending order:
		return -Double.compare(d1, d2);
	}

	/**
	 * The detail String can be both a double value or a percent value.
	 * 
	 * We detect both and convert the percent value to a double as well, so we can
	 * compare both.
	 * 
	 * @param detail the detail value string
	 * @return the double representation
	 */
	private double convertDetail(String detail) {
		if (detail == null) {
			return 0d;
		}

		var doubleMatcher = DOUBLE_PATTERN.matcher(detail);
		if (doubleMatcher.matches()) {
			return Double.parseDouble(doubleMatcher.group(1));
		}

		var percentMatcher = PERCENT_PATTERN.matcher(detail);
		if (percentMatcher.matches()) {
			var percent = Integer.parseInt(percentMatcher.group(1));
			return ((double) percent) / 100.0d;
		}

		Log.error("Could not parse detail string " + detail);
		return 0d;
	}

	@Override
	public void openTabnineConfig() {
		// get the lowlevel facade to send the request
		var requestFacade = DependencyContainer.instanceOfBinaryRequestFacade();

		// prepare the request
		var request = new AutocompleteRequest();
		request.regionIncludesBeginning = true;
		request.regionIncludesEnd = true;
		request.before = "tabnine::config";
		request.after = "";
		request.filename = "";
		request.offset = 0;
		request.line = 0;
		request.character = 0;
		request.maxResults = 1;
		
		var response = requestFacade.executeRequest(request);
		if (response != null && response.results.length > 0)
		{
			Log.info(response.results[0].new_prefix);
		} else {
			Log.error("Something went wrong when trying to open TabNine config in the browser");
		}
	}
}
