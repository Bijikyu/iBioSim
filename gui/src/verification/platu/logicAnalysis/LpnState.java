package verification.platu.logicAnalysis;


import java.util.*;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;

import verification.platu.common.PlatuObj;
import verification.platu.lpn.LPN;
import verification.platu.lpn.LPNTran;
import verification.platu.stategraph.*;

/**
 * State
 * @author Administrator
 */
public class LpnState extends PlatuObj {
    
    private LhpnFile lpn;
    private State state;
    private HashSet<Transition> enabledSet;
    int index;
    
    

    public LpnState() {
        this.lpn = null;
        this.state = null;
        this.enabledSet = null;
        this.index = -1;
    }

    public LpnState(final LhpnFile lhpnFile, final State newState, final HashSet<Transition> lpnEnabledSet) {
    	this.lpn = lhpnFile;
    	this.state = newState;
    	this.enabledSet = lpnEnabledSet;
    }
    
    public void setLpn(final LhpnFile thisLpn) {
    	this.lpn = thisLpn;
    }
    
    public LhpnFile getLpn() {
    	return this.lpn;
    }
    
    public void setState(State newState) {
    	this.state = newState;
    }
    
    public void setLabel(String lbl) {
    	
    }
    
    public String getLabel() {
    	return null;
    }
    
    public void setIndex(int i) {
    	this.index = i;
    }
    
    public int getIndex() {
    	return this.index;
    }
    
    @Override
    public LpnState clone() {
    	LpnState copy = new LpnState();
    	copy.lpn = this.lpn;
    	copy.state = this.state;
    	copy.enabledSet = this.enabledSet;
        return copy;
    }

	@Override
	public int hashCode() {
		final int prime = 13;
		int result = Integer.rotateLeft(this.state.hashCode(), prime) ^ Integer.rotateLeft(this.enabledSet.hashCode(), prime);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		
		LpnState other = (LpnState) obj;
		if(this.lpn != other.lpn)
			return false;
	
		if(this.state != other.state)
			return false;
		
		if(this.enabledSet.equals(other.enabledSet)==false)
			return false;
		
		return true;
	}


    public State getState() {
        return this.state;
    }

    public HashSet<Transition> getEnabled() {
    	return this.enabledSet;
    }
}

