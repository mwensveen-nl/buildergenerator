package nl.mwensveen.buildergenerator.view;

import java.util.ArrayList;
import java.util.List;
import nl.mwensveen.buildergenerator.data.BuilderField;
import nl.mwensveen.buildergenerator.data.GeneratorData;
import nl.mwensveen.buildergenerator.data.Option;
import nl.mwensveen.buildergenerator.util.BuilderFieldUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * The Class BuilderGeneratorDialog is the modal dialog for the plugin.
 */
public class BuilderGeneratorDialog extends org.eclipse.swt.widgets.Dialog {

	private GeneratorData data;
	private Shell shell;
	private final List<Button> fieldButtons = new ArrayList<>();

	/**
	 * Create the dialog.
	 *
	 * @param parent
	 * @param style
	 */
	public BuilderGeneratorDialog(Shell parent, int style) {
		super(parent, style);
		setText("Builder Generator");
	}

	/**
	 * Open the dialog.
	 *
	 * @return the result
	 */
	public Object open() {
		createContents();
		placeDialogInCenter();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return data;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		shell.setSize(500, 550);
		shell.setText(getText());
		shell.setLayout(new FormLayout());

		ScrolledComposite scrolledComposite = createFieldCheckBoxes();

		Button btnSelectAll = createSelectAllButton(scrolledComposite);
		createDeselectAllButton(scrolledComposite, btnSelectAll);

		Group grpOptions = createOptionCheckBoxes(scrolledComposite);

		Button btnCancel = createCancelButton(btnSelectAll, grpOptions);
		createGenerateButton(btnCancel);
	}

	private void createGenerateButton(Button btnCancel) {
		Button btnGenerate = new Button(shell, SWT.NONE);
		FormData fd_btnGenerate = new FormData();
		fd_btnGenerate.bottom = new FormAttachment(btnCancel, -10, SWT.TOP);
		fd_btnGenerate.left = new FormAttachment(btnCancel, 0, SWT.LEFT);
		btnGenerate.setLayoutData(fd_btnGenerate);
		btnGenerate.setText("Generate");
		btnGenerate.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				data.setExecute(true);
				shell.dispose();
				SWTResourceManager.dispose();
			}
		});
	}

	private Button createCancelButton(Button btnSelectAll, Group grpOptions) {
		Button btnCancel = new Button(shell, SWT.NONE);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(grpOptions, 0, SWT.BOTTOM);
		fd_btnCancel.left = new FormAttachment(btnSelectAll, 0, SWT.LEFT);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				data.setExecute(false);
				shell.dispose();
				SWTResourceManager.dispose();
			}
		});
		return btnCancel;
	}

	private void createDeselectAllButton(ScrolledComposite scrolledComposite, Button btnSelectAll) {
		Button btnDeselectAll = new Button(shell, SWT.NONE);
		FormData fd_btnDeselectAll = new FormData();
		fd_btnDeselectAll.top = new FormAttachment(btnSelectAll, 10);
		fd_btnDeselectAll.left = new FormAttachment(scrolledComposite, 6);
		btnDeselectAll.setLayoutData(fd_btnDeselectAll);
		btnDeselectAll.setText("Deselect All");
		btnDeselectAll.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (Button button : fieldButtons) {

					BuilderField field = (BuilderField) button.getData();
					if (field.isSelected()) {
						button.setSelection(false);
						field.switchSelected();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private Button createSelectAllButton(ScrolledComposite scrolledComposite) {
		Button btnSelectAll = new Button(shell, SWT.NONE);
		FormData fd_btnSelectAll = new FormData();
		fd_btnSelectAll.top = new FormAttachment(scrolledComposite, 0, SWT.TOP);
		fd_btnSelectAll.left = new FormAttachment(scrolledComposite, 6);
		btnSelectAll.setLayoutData(fd_btnSelectAll);
		btnSelectAll.setText("Select All");
		btnSelectAll.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (Button button : fieldButtons) {
					BuilderField field = (BuilderField) button.getData();
					if (!field.isSelected()) {
						button.setSelection(true);
						field.switchSelected();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		return btnSelectAll;
	}

	private Group createOptionCheckBoxes(ScrolledComposite scrolledComposite) {
		Group grpOptions = new Group(shell, SWT.NONE);
		grpOptions.setText("Options");
		grpOptions.setLayout(new RowLayout(SWT.VERTICAL));
		FormData fd_grpOptions = new FormData();
		fd_grpOptions.top = new FormAttachment(scrolledComposite, 20, SWT.BOTTOM);
		fd_grpOptions.bottom = new FormAttachment(scrolledComposite, 260, SWT.BOTTOM);
		fd_grpOptions.right = new FormAttachment(scrolledComposite, 0, SWT.RIGHT);
		fd_grpOptions.left = new FormAttachment(scrolledComposite, 0, SWT.LEFT);
		grpOptions.setLayoutData(fd_grpOptions);

		for (Option option : data.getOptions()) {
			final Button btn = new Button(grpOptions, SWT.CHECK);
			btn.setText(option.getDescription());
			btn.setSelection(option.isSelected());
			btn.setData(option);
			btn.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					((Option) btn.getData()).switchSelected();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub

				}
			});

		}
		return grpOptions;
	}

	private ScrolledComposite createFieldCheckBoxes() {

		Label label = new Label(shell, SWT.NONE);
		// txtSelectTheFields.setBackground(SWTResourceManager.getColor(242,
		// 241, 240));
		label.setText("Select the Fields to include in the Builder");
		FormData fd_txtSelectTheFields = new FormData();
		fd_txtSelectTheFields.top = new FormAttachment(0, 0);
		fd_txtSelectTheFields.left = new FormAttachment(0, 10);
		label.setLayoutData(fd_txtSelectTheFields);

		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite_1 = new FormData();
		fd_scrolledComposite_1.bottom = new FormAttachment(0, 250);
		fd_scrolledComposite_1.right = new FormAttachment(0, 400);
		fd_scrolledComposite_1.top = new FormAttachment(label, 10);
		fd_scrolledComposite_1.left = new FormAttachment(label, 0, SWT.LEFT);
		scrolledComposite.setLayoutData(fd_scrolledComposite_1);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Group grpFields = new Group(scrolledComposite, SWT.NONE);
		grpFields.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION_TEXT));
		grpFields.setLayout(new RowLayout(SWT.VERTICAL));
		// add all fields to the dialog
		for (BuilderField field : data.getFields()) {
			final Button btn = new Button(grpFields, SWT.CHECK);
			fieldButtons.add(btn);
			btn.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION_TEXT));
			btn.setText(BuilderFieldUtil.getName(field));
			btn.setData(field);
			btn.setSelection(field.isSelected());
			btn.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					((BuilderField) btn.getData()).switchSelected();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
				}
			});
		}

		scrolledComposite.setContent(grpFields);
		scrolledComposite.setMinSize(grpFields.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return scrolledComposite;
	}

	public void setData(GeneratorData data) {
		this.data = data;
	}

	private void placeDialogInCenter() {
		Rectangle parentSize = getParent().getBounds();
		Rectangle mySize = shell.getBounds();

		int locationX, locationY;
		locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
		locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;

		shell.setLocation(new Point(locationX, locationY));
	}
}
