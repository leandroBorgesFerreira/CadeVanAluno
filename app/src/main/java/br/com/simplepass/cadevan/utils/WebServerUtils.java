package br.com.simplepass.cadevan.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by leandro on 2/16/16.
 */
public class WebServerUtils {
    //Constante recebida caso o servidor tenha retornado a resposta esperada
    public static final String SERVER_RESULT_OK = "ok";
    //Constante para pegar no json a resposta doservidor
    public static final String SERVER_RESULT = "serverResult";
    //Constante recebida caso o servidor tenha retornado um erro
    public static final String SERVER_RESULT_ERRO = "serverErro";
    //Endereço do servidor
    public static final String SERVER_ADDRESS = "http://simplepass.teramundi.com/";
    //Página em PHP para registrar o gcm register ID
    public static final String SERVER_GCM_REGISTER = "simplepass/gcm/register.php";
    //Index da mensagem do JSON que o servidor retorna
    public static final String SERVER_MESSAGE = "message";
    //Erro ao conectar: sem internet
    public static final String ERROR_NO_INTERNET = "Sem Internet";
    //Resposta do pedido ao servidor cancelado
    public static final String SERVER_RESULT_CANCELED = "canceled";
    //Tempo de conexão máximo com o servidor
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds


    public static JSONObject sendToServer(FormUtils.Form form, String address){
        //Log.d("Web Server", "Url: " + address);

        try {
            URL url = new URL(address);

            //Log.d("sendToServer", "mandado ao servidor: " + form.toJSONObject().toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
            connection.setDoOutput(true);

            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(form.toJSONObject().toString());
            out.close();

            return readStream(connection.getInputStream());
        } catch(Exception e) {
            e.printStackTrace();
            //Log.d("Simple Funcionario", "erro ao se conectar com o host: " + e.getMessage());

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(SERVER_RESULT, WebServerUtils.SERVER_RESULT_ERRO);
                jsonObject.put(SERVER_MESSAGE, ERROR_NO_INTERNET);
            } catch(JSONException erro){
                Log.d("Json", "erro sendToServer: " + e.getMessage());
            }

            return jsonObject;
        }
    }

    public static JSONObject readStream(InputStream in) {
        BufferedReader reader = null;
        String awnser = "";
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while ((line = reader.readLine()) != null) {
                //Log.d("Web Server", "Lido: " + line);
                awnser += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try{
            return new JSONObject(awnser);
        } catch(JSONException erro){
            //Log.d("Json", "erro sendToServer: " + erro.getMessage());
            return null;
        }
    }
}
