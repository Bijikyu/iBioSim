package biomodel.gui.schematic;


import javax.swing.JPopupMenu;

import main.Gui;

import biomodel.gui.comp.Grid;
import biomodel.gui.comp.GridAction;
import biomodel.gui.util.ComponentAction;
import biomodel.util.GlobalConstants;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.util.mxGraphActions;

public class EditorPopupMenu extends JPopupMenu
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	/**
	 * constructor
	 * @param editor the schematic creating the popup menu
	 * @param cell 
	 * @param biosim 
	 */
	public EditorPopupMenu(Schematic editor, mxCell cell, Gui biosim) {
		
		Grid grid = editor.getGrid();

		boolean selected = !editor.getGraphComponent().getGraph().isSelectionEmpty();
		add(editor.bind("Delete", mxGraphActions.getDeleteAction())).setEnabled(selected);
		addSeparator();
		
		if (cell != null && editor.getGraph().getCellType(cell).equals(GlobalConstants.COMPONENT)) {
			add(new ComponentAction("Open Component in New Tab", editor.getGraph().getModelFileName(cell.getId()), biosim));
			addSeparator();
		}

		if (grid.isEnabled()) {
			
			add(new GridAction("Select All Locations", editor));
			add(new GridAction("De-select All Locations", editor))
				.setEnabled(editor.getGrid().isALocationSelected());
			add(new GridAction("Clear Selected Location(s)", editor))
				.setEnabled(editor.getGrid().isALocationSelected());
			add(new GridAction("Add Component(s) to (Non-Occupied) Selected Location(s)", 
				editor)).setEnabled(editor.getGrid().isALocationSelected());
		}
		else {	
			
			add(editor.bind("Select Vertices", mxGraphActions.getSelectVerticesAction()));
			add(editor.bind("Select Edges", mxGraphActions.getSelectEdgesAction()));
			addSeparator();
			add(editor.bind("Select All", mxGraphActions.getSelectAllAction()));
		}
	}
}
