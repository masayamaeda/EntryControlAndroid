package jp.isaplan.nfcentrycontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class ConfirmRegistActivity extends Activity {

	private String TAG = "ConfirmRegistActivity";
	private String mCardId;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comfirm_regist);
		
		mCardId = "";
		
        Intent intent = getIntent();
        if (intent != null) {
        	mCardId = intent.getStringExtra("CardID");
        }
        
        Button buttonExit = (Button) findViewById(R.id.button1);
        buttonExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickExit();
			}
		});

        Button buttonRegist = (Button) findViewById(R.id.button2);
        buttonRegist.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickRegist();
			}
		});    
    }
    
    private void onClickExit() {
    	this.finish();
    }

    private void onClickRegist() {
    	Intent intent = new Intent();
        intent.setClassName("jp.isaplan.nfcentrycontrol","jp.isaplan.nfcentrycontrol.SelectCardUserActivity");
        intent.putExtra("CardID", mCardId);
        startActivity(intent);
    }

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ConfirmRegistActivity.this.finish();
			return true;
		}
		return false;
	}
}
