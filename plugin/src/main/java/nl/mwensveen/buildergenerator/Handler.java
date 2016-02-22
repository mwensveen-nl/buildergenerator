package nl.mwensveen.buildergenerator;

import nl.mwensveen.buildergenerator.data.GeneratorData;
import nl.mwensveen.buildergenerator.generator.BuilderGenerator;
import nl.mwensveen.buildergenerator.prefs.BuilderPreferencesManager;
import nl.mwensveen.buildergenerator.util.BuilderFieldUtil;
import nl.mwensveen.buildergenerator.view.BuilderGeneratorDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The Class Handler handles the executon for the Builder Generatr.
 */
public class Handler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell parentShell = HandlerUtil.getActiveShell(event);
		IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		IEditorInput editorInput = editor.getEditorInput();
		try {
			manager.connect(editorInput);
			ICompilationUnit workingCopy = manager.getWorkingCopy(editorInput);

			// get the data for this compilation unit
			GeneratorData data = BuilderPreferencesManager.loadGeneratorData();
			BuilderFieldUtil.findAllFields(workingCopy, data);

			// show the dialog
			BuilderGeneratorDialog dialog = new BuilderGeneratorDialog(parentShell, 0);
			dialog.setData(data);
			dialog.open();

			if (data.isExecute()) {
				BuilderPreferencesManager.saveGeneratorData(data);
				BuilderGenerator generator = new BuilderGenerator(data);
				generator.generate(workingCopy);
				synchronized (workingCopy) {
					workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			manager.disconnect(editorInput);
		}
		return null;
	}

	/**
	 * Selection changed.
	 *
	 * @param action the action
	 * @param selection the selection
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}