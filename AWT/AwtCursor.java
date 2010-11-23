package AWT;

import Tools.Cursor;

public class AwtCursor implements Cursor {

	private java.awt.Cursor cursor;
	
	public AwtCursor(java.awt.Cursor cursor) {
		this.cursor = cursor;
	}
	
	public java.awt.Cursor getCursor() {
		return cursor;
	}
}
