package gcm.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import main.Gui;

public class ComponentAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7810208239930810196L;

	private String id;

	private Gui biosim;

	public ComponentAction(String name, String id, Gui biosim) {
		super(name);
		this.id = id;
		this.biosim = biosim;
	}

	public void actionPerformed(ActionEvent e) {
		biosim.openGCM(id, false);
	}

}
