package br.com.simplepass.cadevan.drawer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.activity.LoginActivity;
import br.com.simplepass.cadevan.activity.MainActivity;
import br.com.simplepass.cadevan.utils.Constants;


/* Listener que é chamado quando usuário perta botão do drawer */
public class DrawerItemClickListener implements NavigationView.OnNavigationItemSelectedListener{
    private Activity mActivity;
    private DrawerLayout mDrawerLayout;

    public DrawerItemClickListener(Activity activity, DrawerLayout drawerLayout){
        mActivity = activity;
        mDrawerLayout = drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawers();

        switch (item.getItemId()){
            case R.id.drawer_item_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mActivity.getString(R.string.share_message));
                shareIntent.setType("text/plain");
                mActivity.startActivity(shareIntent);
                return true;
            case R.id.drawer_item_leave:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AccountManager.get(mActivity).removeAccount(
                            new Account(Constants.ARG_ACCOUNT_NAME, Constants.ACCOUNT_TYPE),
                            mActivity,
                            null,
                            null);

                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                            mActivity);

                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    mActivity.startActivity(intent, activityOptions.toBundle());
                } else {
                    AccountManager.get(mActivity).removeAccount(
                            new Account(Constants.ARG_ACCOUNT_NAME, Constants.ACCOUNT_TYPE),
                            null,
                            null);
                    mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                }
                return true;
            default:
                return true;
        }
    }
}
