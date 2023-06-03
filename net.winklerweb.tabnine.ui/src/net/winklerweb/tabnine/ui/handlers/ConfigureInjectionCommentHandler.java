package net.winklerweb.tabnine.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import net.winklerweb.tabnine.core.ICommentInjectionService;

/**
 * Handle the command by opening the injection comment configuration dialog.
 */
public class ConfigureInjectionCommentHandler extends AbstractHandler 
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new CommentConfigurationDialog(HandlerUtil.getActiveShell(event)).open();
		return null;
	}
}

/**
 * The comment configuration dialog:
 * 
 * This dialog contains a multiline text field that allows the user to enter the comment to be stored as the current injection comment.
 * Below the text field, there is also a combo box that lists the recent comments.
 * 
 * If the user selects a comment from this box, it is set to the text field.
 * 
 *  The user can close the dialog by clicking the OK button in which case the selected comment from the text field is stored as the current injection comment..
 *  Alternatively, the user can close the dialog by clicking the Cancel button, in which case the comment is not stored.
 */
class CommentConfigurationDialog extends TitleAreaDialog {

	private Text textField;
	
	private Combo comboBox;
	
	public CommentConfigurationDialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Please enter a prompt");
		setMessage("Enter a prompt describing the code context (e.g., Java source level, libraries to use or to avoid, etc.)");

		Composite parentComposite = (Composite) super.createDialogArea(parent);
		parentComposite.setLayout(new GridLayout());

		// create the text field
		textField = new Text(parentComposite, SWT.MULTI | SWT.BORDER);
		textField.setLayoutData(new GridData(GridData.FILL_BOTH));
				
		// create the combo box
		comboBox = new Combo(parentComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ServiceCaller.callOnce(getClass(), ICommentInjectionService.class, service -> {
			String[] recentComments = service.getRecentInjectionComments().toArray(String[]::new);
			comboBox.setItems(recentComments);
			
			service.getCurrentInjectionComment().ifPresent(textField::setText);
		});
		
		comboBox.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			textField.setText(comboBox.getItem(comboBox.getSelectionIndex()));
		}));

		textField.addModifyListener(l -> {
			getButton(OK).setEnabled(!textField.getText().isBlank());
		});

		Display.getCurrent().asyncExec(() -> getButton(OK).setEnabled(!textField.getText().isBlank()));
		
		return parentComposite;
	}
	
	@Override
	protected void okPressed() {
		ServiceCaller.callOnce(getClass(), ICommentInjectionService.class, service -> {
			service.setCurrentInjectionComment(textField.getText());
		});
		super.okPressed();
	}
};