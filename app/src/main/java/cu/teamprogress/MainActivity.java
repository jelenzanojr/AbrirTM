package cu.teamprogress;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import cu.teamprogress.permission.PermissionHandlerActivity;
import cu.teamprogress.view.OpenTMFragment;
import cu.teamprogress.view.ZoomFragment;
import cu.teamprogress.viewmodel.MainViewModel;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends PermissionHandlerActivity implements OpenTMFragment.OnFragmentInteractionListener {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MainViewModel mainViewModel = new ViewModelProvider(this)
                .get(MainViewModel.class);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_scan_qr) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void changeFragment(){
        FragmentManager fm = getSupportFragmentManager();
        ZoomFragment fa = new ZoomFragment();
        fm.beginTransaction().replace(R.id.nav_host_fragment,fa).commit();

    }
}