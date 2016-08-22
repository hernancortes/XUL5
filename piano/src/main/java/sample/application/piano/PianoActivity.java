package sample.application.piano;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class PianoActivity extends Activity implements OnTouchListener{

	static final int numWk = 11, numBk = 7, numKeys = numWk + numBk;
	Region[] kb = new Region[numKeys];
	MediaPlayer[] key = new MediaPlayer[numKeys];
	int sw, sh;
	int[] activePointers = new int[numKeys];
	Drawable drawable_white, drawable_black, drawable_white_pressed, drawable_black_pressed;
	Timer timer;
	Bitmap bitmap_keyboard;
	ImageView iv;
	boolean[] lastPlayingNotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano);

        TypedArray notes = getResources().obtainTypedArray(R.array.notes);
        for (int i = 0; i < notes.length(); i ++){
        	int k = notes.getResourceId(i, -1);
        	if (k != -1){
        		this.key[i] = MediaPlayer.create(this, k);
        	} else this.key[i] = null;
        }

        Resources res = getResources();
        /*
		drawable_white = res.getDrawable(R.drawable.white);
        drawable_black = res.getDrawable(R.drawable.black);
        drawable_white_pressed = res.getDrawable(R.drawable.white_pressed);
        drawable_black_pressed = res.getDrawable(R.drawable.black_pressed);
*/
        Display disp = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        this.sw = disp.getWidth();
        this.sh = disp.getHeight();

        makeRegions();
        for (int i = 0; i < numKeys; i ++) this.activePointers[i] = -1;
        this.iv = (ImageView) findViewById(R.id.imageView1);
        this.iv.setOnTouchListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_piano, menu);
        return true;
    }

	public boolean onTouch(View v, MotionEvent event) {
		int pointerIndex = event.getAction();
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);

		for (int j = 0; j < numKeys; j ++){
			if (kb[j].contains((int) x, (int) y)){

				switch(event.getActionMasked()){

				//タッチしたときの処理
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:

				playNote(key[j]);
				this.activePointers[pointerIndex] = j;
				break;

				//離した時の処理
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				stopNote(key[j]);
				activePointers[pointerIndex] = - 1;
				break;

				//ドラッグしたときの処理
				case MotionEvent.ACTION_MOVE:
				if (activePointers[pointerIndex] != j){
					if (activePointers[pointerIndex] != - 1)
						stopNote(key[activePointers[pointerIndex]]);
					playNote(key[j]);
					activePointers[pointerIndex] = j;
				}
			}
			break;
		}
	}
		return true;
	}

	@Override
	protected void onPause() {

		super.onPause();
		timer.cancel();
	}

	@Override
	protected void onResume() {

		super.onResume();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				//各MediaPlayerオブジェクトの再生状態を取得
				boolean[] playingNotes = new boolean[numKeys];
				for (int i = 0; i < playingNotes.length; i++) {
					playingNotes[i] = key[i].isPlaying();

					//前回実行時とは再生状態が変わった場合のみ画面書き換えを実行
					if (!Arrays.equals(playingNotes, lastPlayingNotes)) {
						bitmap_keyboard = drawKeys();

						//UIスレッドでImageViewに画像をセット
						runOnUiThread(new Runnable() {

							public void run() {
								iv.setImageBitmap(bitmap_keyboard);
							}
						});
					}

					//再生状態を変数に保存
					lastPlayingNotes = playingNotes;
				}
			}


		}, 0, 100);
	}
	void makeRegions(){
		int kw, kh, bkw, bkh;

		//画面サイズからキーの大きさを計算する
		kw = (int) (this.sw / numWk);
		kh = (int) (this.sh * 0.8);
		bkw = (int) (kw * 0.6);
		bkh = (int) (this.sh * 0.5);

		//キーの形に合わせたPathオブジェクトの作成
		Path[] path = new Path[4];
		path[0] = new Path();
		path[1] = new Path();
		path[2] = new Path();
		path[3] = new Path();

		//右に黒鍵のある白鍵
		path[0].lineTo(0, kh);
		path[0].lineTo(kw, kh);
		path[0].lineTo(kw, bkh);
		path[0].lineTo(kw - (bkw / 2), bkh);
		path[0].lineTo(kw - (bkw / 2), 0);
		path[0].close();

		//左右に黒鍵のある白鍵
		path[1].moveTo(bkw / 2, 0);
		path[1].lineTo(bkw / 2, bkh);
		path[1].lineTo(0, bkh);
		path[1].lineTo(0, kh);
		path[1].lineTo(kw, kh);
		path[1].lineTo(kw, bkh);
		path[1].lineTo(kw - (bkw / 2), bkh);
		path[1].lineTo(kw - (bkw / 2), 0);
		path[1].close();

		//左に黒鍵のある白鍵
		path[2].moveTo(bkw / 2, 0);
		path[2].lineTo(bkw / 2, bkh);
		path[2].lineTo(0, bkh);
		path[2].lineTo(0, kh);
		path[2].lineTo(kw, kh);
		path[2].lineTo(kw, 0);
		path[2].close();

		//黒鍵
		path[3].addRect(0, 0, bkw, bkh, Direction.CCW);

		//Pathオブジェクトの情報を使用してRegionオブジェクトを作成し、キーごとに割り当てる
		Region region = new Region(0, 0, this.sw, this.sh);
		int kt[] = new int[]{0, 1, 2, 0, 1, 1, 2, 0, 1, 2, 0, 3, 3, -1, 3, 3, 3, -1, 3, 3};

		for (int i = 0; i < this.numWk; i ++){
			this.kb[i] = new Region();
			Path pathtmp = new Path();
			pathtmp.addPath(path[kt[i]], i * kw, 0);
			this.kb[i].setPath(pathtmp, region);
			}

		int j = this.numWk;
		for (int i = this.numWk; i < kt.length; i ++){
			if (kt[i] != -1){
				this.kb[j] = new Region();
				Path pathtmp = new Path();
				pathtmp.addPath(path[kt[i]], (i - this.numWk + 1) * kw - (bkw / 2), 0);
				this.kb[j].setPath(pathtmp, region);
				j = j + 1;

			}
		}

}

	Bitmap drawKeys(){
		Bitmap bm = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);

		for (int i = 0; i < this.numWk; i ++){
			if (this.key[i].isPlaying()){
				drawable_white_pressed.setBounds(kb[i].getBounds());
				drawable_white_pressed.draw(canvas);
			} else {
					drawable_white.setBounds(kb[i].getBounds());
					drawable_white.draw(canvas);
			}
		}

		for (int i = this.numWk; i < this.numKeys; i ++){
			if (this.key[i].isPlaying()){
				drawable_black_pressed.setBounds(kb[i].getBounds());
				drawable_black_pressed.draw(canvas);
			} else {
					drawable_black.setBounds(kb[i].getBounds());
					drawable_black.draw(canvas);
			}
		}
		return bm;
	}

	private void playNote(MediaPlayer mp){
		mp.seekTo(0);
		mp.start();
	}

	private void stopNote(MediaPlayer mp){
		mp.pause();
	}
}
