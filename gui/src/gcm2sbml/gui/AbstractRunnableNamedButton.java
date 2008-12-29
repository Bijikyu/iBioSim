package gcm2sbml.gui;

import javax.swing.JButton;

public abstract class AbstractRunnableNamedButton extends JButton implements NamedObject,
		Runnable {		
	
	public AbstractRunnableNamedButton(String name) {
		super(name);
		this.setName(name);
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}
	
	private String name = "";
}
