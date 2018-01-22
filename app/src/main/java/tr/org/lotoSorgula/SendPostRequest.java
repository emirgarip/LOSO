package tr.org.lotoSorgula;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by emir on 30.12.2017.
 */

public class SendPostRequest extends AsyncTask<String, Void, String> {

    protected void onPreExecute(){}
    private String resp;
    private LotoSorgula ls;
    public void setClass(LotoSorgula ls)
    {
        this.ls = ls;
    }

    public String getResp() {
        return resp;
    }

    public void setResp(String resp) {
        this.resp = resp;
    }

    byte[] data;
    public void setData(byte[] input)
    {
        data = input;
    }
    protected String doInBackground(String... arg0) {

        try {

            URL url = new URL("http://78.186.122.179:14000/test"); // here is your URL path

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(data);
//            OutputStream os = conn.getOutputStream();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream(os);
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(data);

//            writer.flush();
//            writer.close();
//            os.close();
            dos.flush();
            dos.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader in=new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
                resp = sb.toString();
                return sb.toString();

            }
            else {
                return new String("false : "+responseCode);
            }
        }
        catch(Exception e){
            Log.d("Exception: ",e.getMessage());
            return new String("Exception: " + e.getMessage());
        }

    }

    @Override
    protected void onPostExecute(String result) {
//        ls.findViewById(R.id.progressbar).setVisibility(View.GONE);
        ls.setOutAnimation(new AlphaAnimation(1f, 0f));
        ls.getOutAnimation().setDuration(200);
        ls.findViewById(R.id.progressBarHolder).setAnimation(ls.getOutAnimation());
        ls.findViewById(R.id.progressBarHolder).setVisibility(View.GONE);
        ls.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Toast.makeText(ls.getApplicationContext(), result,
                Toast.LENGTH_LONG).show();
    }
}
