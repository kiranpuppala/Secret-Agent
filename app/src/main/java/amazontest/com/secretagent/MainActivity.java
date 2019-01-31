package amazontest.com.secretagent;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;


public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}
