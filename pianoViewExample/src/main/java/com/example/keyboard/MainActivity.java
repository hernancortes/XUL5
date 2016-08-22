package com.example.keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.twobard.pianoview.Piano;
import com.twobard.pianoview.Piano.PianoKeyListener;

public class MainActivity extends Activity {
	
	private static final String DEBUG_TAG = "PianoView";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Programmatic setup
		//
		LinearLayout main = (LinearLayout)findViewById(R.id.main_content);
		Piano piano;
		piano = new Piano(this);
		piano.setPianoKeyListener(onPianoKeyPress);
		main.addView(piano);
	}
	
	private PianoKeyListener onPianoKeyPress=
	        new PianoKeyListener() {

                @Override
                public void keyPressed(int id, int action) {
                    Log.e(DEBUG_TAG,"Key pressed: " + id);
                }
	       
	      };
	      
}
