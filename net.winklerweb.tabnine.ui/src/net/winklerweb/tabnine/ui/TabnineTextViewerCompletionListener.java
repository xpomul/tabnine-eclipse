package net.winklerweb.tabnine.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension9;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.tabnine.Log;

import net.winklerweb.tabnine.core.CompletionProposal;
import net.winklerweb.tabnine.core.ITabnineCompletionCache;
import net.winklerweb.tabnine.core.ITabnineCompletionService;

/**
 * Listens for cursor position changes in the associated editor.
 * 
 * @author Stefan Winkler
 */
public class TabnineTextViewerCompletionListener implements CaretListener, PaintListener {
	private static final Rectangle NO_BOX = new Rectangle(0, 0, 0, 0);

	/**
	 * The associated editor in which we are listening for cursor position changes.
	 */
	private ITextViewer textViewer;

	private ServiceCaller<ITabnineCompletionCache> completionCache = new ServiceCaller<>(getClass(),
			ITabnineCompletionCache.class);

	private ServiceCaller<ITabnineCompletionService> completionServiceCaller = new ServiceCaller<>(getClass(),
			ITabnineCompletionService.class);

	private Rectangle currentCompletionBox = NO_BOX;

	private String editorInputPath;

	/**
	 * Create a new listener.
	 * 
	 * @param textViewer the viewer to which we add the listener
	 */
	public TabnineTextViewerCompletionListener(ITextViewer textViewer, String editorInputPath) {
		this.textViewer = textViewer;
		this.editorInputPath = editorInputPath;
		
		textViewer.getTextWidget().addCaretListener(this);
		textViewer.getTextWidget().addPaintListener(this);

		if (textViewer instanceof ITextViewerExtension9)
		{
			var offset = ((ITextViewerExtension9) textViewer).getLastKnownSelection();
			requestNewProposalsAndRedraw(offset.getOffset());
		}
		
		Log.debug("TabNine active for " + editorInputPath);
	}

	@Override
	public void caretMoved(CaretEvent event) {
		// invalidate previous proposals
		completionCache.call(ITabnineCompletionCache::invalidateCompletions);
		if (currentCompletionBox != NO_BOX) {
			textViewer.getTextWidget().redraw(currentCompletionBox.x, currentCompletionBox.y,
					currentCompletionBox.width, currentCompletionBox.height, true);
			currentCompletionBox = NO_BOX;
		}

		final int offset;
		if (textViewer instanceof ITextViewerExtension5) {
			offset = ((ITextViewerExtension5)textViewer).widgetOffset2ModelOffset(event.caretOffset);
		} else {
			offset = event.caretOffset;
		}
		
		requestNewProposalsAndRedraw(offset);
	}
		
	private void requestNewProposalsAndRedraw(int offset)
	{
		// request new proposals and send redraw request
		CompletableFuture.runAsync(() -> {
			var completions = new ArrayList<CompletionProposal>();
			completionServiceCaller.call(
					service -> completions.addAll(service.complete(textViewer, offset, editorInputPath)));
			completionCache.call(cache -> cache.cacheCompletions(completions));
			requestRedrawForNewCompletions(completions);
		});
	}

	private void requestRedrawForNewCompletions(List<CompletionProposal> completions) {
		if (completions.isEmpty()) {
			this.currentCompletionBox = NO_BOX;
			return;
		}

		Display.getDefault().asyncExec(() -> {
			var textWidget = textViewer.getTextWidget();
			GC gc = new GC(textWidget);
			Rectangle completionBox;
			try {
				completionBox = calculateCompletionBox(gc, textWidget, completions);
			} finally {
				gc.dispose();
			}
			textWidget.redraw(completionBox.x, completionBox.y, completionBox.width, completionBox.height, true);
		});
	}

	@Override
	public void paintControl(PaintEvent e) {
		completionCache.call(cache -> {
			var completions = cache.getCachedCompletions();
			if (!completions.isEmpty()) {
				draw(e.gc, textViewer.getTextWidget(), completions);
			}
		});
	}

	public void draw(GC gc, StyledText textWidget, List<CompletionProposal> completions) {
		var completionsBox = calculateCompletionBox(gc, textWidget, completions);

		gc.setBackground(
				JFaceResources.getColorRegistry().get(Constants.BACKGROUND_COLOR_KEY));
		gc.setForeground(
				JFaceResources.getColorRegistry().get(Constants.FOREGROUND_COLOR_KEY));
		gc.fillRectangle(completionsBox);
		gc.drawRectangle(completionsBox);

		int x = completionsBox.x + 1;
		int y = completionsBox.y + 1;

		for (int i = 0; i < completions.size(); i++) {
			var c = completions.get(i);
			var text = i + ": " + c.getReplacementString();
			gc.drawText(text, x, y);
			y += gc.textExtent(text).y + 3;
		}

		this.currentCompletionBox = completionsBox;
	}

	private Rectangle calculateCompletionBox(GC gc, StyledText textWidget, List<CompletionProposal> completions) {
		var firstCompletion = completions.get(0);
		var currentWidgetCursorPosition = textWidget.getSelectionRange().x;
		var lineOfCursor = textWidget.getLineAtOffset(currentWidgetCursorPosition);
		var firstCharOfLine = textWidget.getOffsetAtLine(lineOfCursor);
		var firstCharOfLineLocation = textWidget.getLocationAtOffset(firstCharOfLine);
		var cursorLocation = textWidget.getLocationAtOffset(currentWidgetCursorPosition);

		var prefixText = "1: "
				+ firstCompletion.getReplacementString().substring(0, firstCompletion.getPrefixOverlap());
		var prefixExtent = gc.textExtent(prefixText);

		var cursorLineHeight = textWidget.getLineHeight(currentWidgetCursorPosition);

		int x = Math.max(cursorLocation.x - prefixExtent.x - 1, firstCharOfLineLocation.x);
		int y = cursorLocation.y + cursorLineHeight;
		var resultBox = new Rectangle(x, y, 2, 2);

		for (int i = 0; i < completions.size(); i++) {
			var c = completions.get(i);
			var localExtent = gc.textExtent(i + ": " + c.getReplacementString());
			resultBox.height += localExtent.y + 3;
			resultBox.width = Math.max(resultBox.width, localExtent.x);
		}

		resultBox.width += 10;
		return resultBox;
	}

	public void dispose() {
		var widget = this.textViewer.getTextWidget();
		if (widget != null && !widget.isDisposed()) {
			widget.removeCaretListener(this);
			widget.removePaintListener(this);
			widget.redraw();
		}
		
		completionServiceCaller.unget();
		completionCache.unget();
	}
}
