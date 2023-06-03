package net.winklerweb.tabnine.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ServiceCaller;

import net.winklerweb.tabnine.core.ITabnineCompletionService;

/**
 * This handler can be called to open the tabnine configuration in the browser by sending tabnine:config to the tabnine client process.
 */
public class OpenTabnineConfigHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ServiceCaller.callOnce(getClass(), ITabnineCompletionService.class, ITabnineCompletionService::openTabnineConfig);
		return null;
	}
}
