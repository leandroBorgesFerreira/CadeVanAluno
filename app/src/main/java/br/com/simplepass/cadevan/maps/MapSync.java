package br.com.simplepass.cadevan.maps;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;

import java.util.Map;

import br.com.simplepass.cadevan.activity.ProgressShower;
import br.com.simplepass.cadevan.domain.Van;
import br.com.simplepass.cadevan.retrofit.AccessToken;
import br.com.simplepass.cadevan.retrofit.CadeVanAlunoClient;
import br.com.simplepass.cadevan.retrofit.ServiceGenerator;
import br.com.simplepass.cadevan.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.HTTP;

/**
 * Classe de sincronização com o mapa. Aqui é pedido ao servidor a posição das vans e depois a
 * classe dispara o método da classe VanPointsDrawer que a invocou.
 */
public class MapSync {
    private Handler mHandler;
    private Runnable mRunnable;
    private Context mContext;
    private ProgressShower mProgressShower;

    private static final int DEFAULT_UPDATE_FREQUENCY = 5000; //5 segungos
    private static final int LOW_BATTERY_UPDATE_FREQUENCY = 10000; //10 segungos
    private static final double LOW_BATTERY_PERCENTAGE = 0.15;
    private static int UPDATE_FREQUENCY; //5 segungos

    public MapSync(final VanPointsDrawer drawer, final Context context, ProgressShower progressShower){
        init(context, progressShower);


        mRunnable = new Runnable() {

            @Override
            public void run() {
            //Aqui vem o código de recebimento da informação
                CadeVanAlunoClient clientService = ServiceGenerator.createService(
                        CadeVanAlunoClient.class);

                Call<Map<Integer, Van>> callLocation = clientService.getAllVans();
                callLocation.enqueue(new Callback<Map<Integer, Van>>() {
                    @Override
                    public void onResponse(Call<Map<Integer, Van>> call, retrofit2.Response<Map<Integer, Van>> response) {
                        if(response.isSuccessful() && response.body() != null){
                            if(mProgressShower.isOnProgress()){
                                mProgressShower.showProgress(false);
                            }
                            drawer.drawVanPoints(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<Integer, Van>> call, Throwable t) {
                        if(mProgressShower.isOnProgress()){
                            mProgressShower.showProgress(false);
                        }

                        Toast.makeText(context.getApplicationContext(), "Falha ao localizar vans",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                mHandler.postDelayed(this, UPDATE_FREQUENCY);
            }
        };
    }

    public MapSync(final VanPointsDrawer drawer, final Context context, final long vanId,
                   ProgressShower progressShower){
        init(context, progressShower);

        mRunnable = new Runnable() {

            @Override
            public void run() {
                //Aqui vem o código de recebimento da informação
                CadeVanAlunoClient clientService = ServiceGenerator.createService(
                        CadeVanAlunoClient.class);

                Call<Van> callLocation = clientService.getVanById(vanId);
                callLocation.enqueue(new Callback<Van>() {
                    @Override
                    public void onResponse(Call<Van> call, retrofit2.Response<Van> response) {
                        if(response.isSuccessful() && response.body() != null){
                            if(mProgressShower.isOnProgress()){
                                mProgressShower.showProgress(false);
                            }

                            drawer.drawSingleVan(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Van> call, Throwable t) {
                        Toast.makeText(context.getApplicationContext(), "Falha ao localizar vans",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                mHandler.postDelayed(this, UPDATE_FREQUENCY);
            }
        };
    }

    public void init(Context context, ProgressShower progressShower){
        mContext = context;

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        mProgressShower = progressShower;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        float batteryPct = level / (float)scale;

        if(batteryPct < LOW_BATTERY_PERCENTAGE && !isCharging){
            UPDATE_FREQUENCY = LOW_BATTERY_UPDATE_FREQUENCY;
        } else{
            UPDATE_FREQUENCY = DEFAULT_UPDATE_FREQUENCY;
        }


        HandlerThread handlerThread = new HandlerThread("Map Sync");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper());
    }

    public void start(){
        if(!mProgressShower.isOnProgress()) {
            mProgressShower.showProgress(true);
        }

        mHandler.post(mRunnable);
    }

    public void stop(){
        mHandler.removeCallbacks(mRunnable);
    }

}
