package deyuan.cn.cnicg.saodemo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTvRet;
    private Button mBtScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        mTvRet = (TextView) findViewById(R.id.tv_result);
        mBtScan = (Button) findViewById(R.id.bt_scan);
        //
        mBtScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
    }

    public void startScan(){
        this.startActivityForResult(new Intent(this,CaptureActivity.class),0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String ret = data.getStringExtra("result");
        mTvRet.setText(ret);


    }
}
