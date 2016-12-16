package thejeshgn.com.bleuartperipheral;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);



        TextView text = new TextView(this);
        text.setPadding(20, 20, 20, 10);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        setContentView(text);
        StringBuilder content =new StringBuilder("<p>BleUARTPeripheral is an Android app that simulates the UART Peripheral on phone. This is for developers to test their client app.</p>");
        content.append("<p> Its a UART simple server, once connected it responds to commands like whoami or date etc. If it can’t figure then it just echos whatever client has sent.</p>");
        content.append("<p> Its Free and Open Source App. You can use the app as is or use the code in your project. More details on <a href='https://github.com/thejeshgn/BleUARTPeripheral'>GitHub</a></p>");
        content.append("<p> Logo is based on Serial Port image by Dalpat Prajapati. </p>");
        content.append("<p> <a href=\"https://thejeshgn.com\">Thejesh GN</a></p>");
        text.setText(Html.fromHtml(content.toString()));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
