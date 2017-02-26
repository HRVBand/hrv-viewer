package controller;

import java.util.EventListener;
import java.util.EventObject;

import javax.swing.event.EventListenerList;

import hrv.RRData;

public class HRVDataController {

	EventListenerList rrDataChangedlisteners = new EventListenerList();
	
	public void rrDataChanged(RRData data) {
		notifyAll(new RRDataChangedEvent(this, data));
	}
	
	public void addRRDataChangedListener(RRDataChangedListener listener) {
		rrDataChangedlisteners.add(RRDataChangedListener.class, listener);
	}
	
	private void notifyAll(RRDataChangedEvent e) {
		for (RRDataChangedListener l : rrDataChangedlisteners.getListeners(RRDataChangedListener.class)) {
			l.rrDataChanged(e);
		}
	}
	
	@FunctionalInterface
	public interface RRDataChangedListener extends EventListener {
		
		void rrDataChanged(RRDataChangedEvent e);
	}
	
	public class RRDataChangedEvent extends EventObject {

		private RRData data;
		
		public RRDataChangedEvent(Object source, RRData data) {
			super(source);
			this.data = data;
		}
		
		public RRData getRRData() {
			return data;
		}
	}
}
