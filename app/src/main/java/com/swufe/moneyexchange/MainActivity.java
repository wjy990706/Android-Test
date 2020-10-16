package com.swufe.moneyexchange;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Runnable
{
    EditText inp;
    TextView text;

    float dollar_rate;
    float euro_rate;
    float won_rate;

    String updateTime;//上次更新时间
    Date today;//本机时间

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    Handler handler;//线程间通信对象
    Message msg;

    URL url=null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inp = findViewById(R.id.input);
        text = findViewById(R.id.textView);//获取输入框和显示文本对象
        today=Calendar.getInstance().getTime();//获取当前系统时间

        sharedPreferences = getSharedPreferences("myrate", MODE_PRIVATE);//从myrate.xml文件中取数据保存（没有myrate会自动生成）
        PreferenceManager.getDefaultSharedPreferences(this);
        dollar_rate=sharedPreferences.getFloat("dollar_rate",0.0f);
        euro_rate=sharedPreferences.getFloat("euro_rate",0.0f);
        won_rate=sharedPreferences.getFloat("won_rate",0.0f);
        updateTime=sharedPreferences.getString("updateTime","");//从sharePrefrences里取数据

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        final String todayStr=sdf.format(today);//date对象转为字符串，便于比较

        if(!todayStr.equals(updateTime)) //判断系统当前时间是否和shareprefrence中存放的上次更新时间一致，不一致说明没有更新，开启子线程爬取网页汇率
         {
           Thread t = new Thread(this);
           t.start();//开启新线程
         }

        handler=new Handler() {//线程里的数据交给handle来处理
                    public void handleMessage (Message msg){
                        if (msg.what == 0) {
                            Bundle bdl = (Bundle) msg.obj;
                            dollar_rate=bdl.getFloat("dollar-rate");
                            euro_rate=bdl.getFloat("euro-rate");
                            won_rate=bdl.getFloat("won-rate");

                            editor = sharedPreferences.edit();
                            editor.putFloat("dollar_rate",dollar_rate);
                            editor.putFloat("won_rate",won_rate);
                            editor.putFloat("euro_rate",euro_rate);
                            editor.putString("uodateTime",todayStr);
                            editor.apply();

                            Log.i("getNetRate", "handleMessage: dollar:"+dollar_rate);
                            Log.i("getNetRate", "handleMessage: euro:"+euro_rate);
                            Log.i("getNetRate", "handleMessage: won:"+won_rate);

                            Toast.makeText(MainActivity.this, "获取网络汇率", Toast.LENGTH_SHORT).show();
                        }
                super.handleMessage(msg);
            }
        };
    }
    public void exchange(View btn)
    {
        Log.i("exchange", " ");
        String inputrmb;
        inputrmb = inp.getText().toString();//取出输入框对象里的值转成string
        float rmb = Float.parseFloat(inputrmb);//值转成float

        if (btn.getId() == R.id.btn_dollar) {

            float dollar = rmb * dollar_rate;
            text.setText(String.valueOf(dollar) + "元");
            Toast.makeText(MainActivity.this, "美元换算成功！", Toast.LENGTH_SHORT).show();
        }//区分点击的哪个按钮，事件处理
        else if (btn.getId() == R.id.btn_euro) {
            float euro = rmb * euro_rate;
            text.setText(String.valueOf(euro) + "元");
            Toast.makeText(MainActivity.this, "欧元换算成功！", Toast.LENGTH_SHORT).show();
        } else {
            float won = rmb * won_rate;
            text.setText(String.valueOf(won) + "元");
            Toast.makeText(MainActivity.this, "韩元换算成功！", Toast.LENGTH_SHORT).show();
        }
    }
    public void open(View v)//点击open按钮，打开新界面，将汇率传值到新的activity
    {
        Intent newActvity = new Intent(this, MainActivity2.class);//intent意图对象，是动作的抽象描述，保存要执行的操作
        newActvity.putExtra("dollar_rate_key", dollar_rate);
        newActvity.putExtra("euro_rate_key", euro_rate);
        newActvity.putExtra("won_rate_key", won_rate);//将汇率传值给新activity

        Log.i("open", "dollar_rate= " + dollar_rate);
        Log.i("open", "euro_rate= " + euro_rate);
        Log.i("open", "won_rate= " + won_rate);

        startActivityForResult(newActvity, 0);
    }
    public void openlist(View btn)
    {
       if(btn.getId()==R.id.btn_openlist){
           Intent list=new Intent(this,RateListActivity.class);
           startActivity(list);
       }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)//处理返回的值
    {
        if (requestCode == 0 && resultCode == 1) {
            Bundle bdl = data.getExtras();
            dollar_rate = bdl.getFloat("dollar_rate_key", 0.1f);
            euro_rate = bdl.getFloat("euro_rate_key", 0.1f);
            won_rate = bdl.getFloat("won_rate_key", 0.1f);

            Log.i("onActivityResult", "dollar_rate= " + dollar_rate);
            Log.i("onActivityResult", "euro_rate= " + euro_rate);
            Log.i("onActivityResult", "won_rate= " + won_rate);

        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    public void run()
    {
        Log.i("run", "run: ");
        Bundle bundle;//存放网络获取的汇率


        bundle=getNetSource();//获取getnetsource方法中存入bundle的汇率值

        Message msg=handler.obtainMessage(0);
        //msg.what=0;
        msg.obj=bundle;//把bundle放进消息对象里
        handler.sendMessage(msg);//交由主线程处理

    }
    public Bundle getNetSource()
    {
        Bundle bundle = new Bundle();//存放网络获取的汇率
        /* URL url=null;
        url=new URL("https://www.usd-cny.com/bankofchina.htm");
        HttpURLConnection http=(HttpURLConnection)url.openConnection();
        InputStream in =http.getInputStream();//获得源文件html

        String html=inputStream2String(in);
        Log.i("getNetsource", "getNetSource: "+html);//检查html文本是否成功获取
        */
        Document doc = null;//把url路径里获取doc对象
        try {
            doc = Jsoup.connect("https://www.usd-cny.com/bankofchina.htm").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //doc=Jsoup.parse(html);也可以直接把之前获取的html源文件作为对象
        Log.i("getNetSource", "run: " + doc.title());
        Elements tables = doc.getElementsByTag("table");//获取html里所有table
        Element table = tables.get(0);//需要的汇率信息就在第一个table里
        Elements tds = table.getElementsByTag("td");//获取这个table中的所有td
        for (int i = 0; i < tds.size(); i = i + 6) {
            Element td0 = tds.get(i);//币种名的td
          /*  Element td1=tds.get(i+1);//币种汇买价
            Element td2=tds.get(i+2);//币种钞买价
            Element td3=tds.get(i+3);//币种汇卖价
            Element td4=tds.get(i+4);//币种钞卖价*/
            Element td5 = tds.get(i + 5);//折算价


            Log.i("getNetSource: ", "td=" + td0.text() + "td5=" + td5.text());
            if ("美元".equals(td0.text())) {
                bundle.putFloat("dollar-rate", 100f / Float.parseFloat(td5.text()));
            } else if ("欧元".equals(td0.text())) {
                bundle.putFloat("euro-rate", 100f / Float.parseFloat(td5.text()));
            } else if ("韩元".equals(td0.text())) {
                bundle.putFloat("won-rate", 100f / Float.parseFloat(td5.text()));
            }


        }//获取td中的汇率值，存入bundle中

            return bundle;//bundle返回函数值
    }
       private String inputStream2String(InputStream inputStream) throws IOException
    {
        final int buffersize=1024;
        final char[] buffer =new char[buffersize];
        final StringBuilder out = new StringBuilder();
        Reader in =new InputStreamReader(inputStream,"gb2312");
        while (true){
            int rsz=in.read(buffer,0,buffer.length);
            if(rsz<0)
                break;
            out.append(buffer,0,rsz);
        }
        return out.toString();
        //return"";
    }

}
