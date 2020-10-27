package com.swufe.moneyexchange;

import androidx.annotation.NonNull;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class  RateListActivity extends ListActivity implements Runnable {
    Handler handler;
    String waitdata[] = {"wait for a moment......"};
    private String logDate = "";
    private final String DATE_SP_KEY = "lastRateDateStr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_rate_list);

        SharedPreferences sp = getSharedPreferences("myrate", Context.MODE_PRIVATE);
        logDate = sp.getString(DATE_SP_KEY, "");
        Log.i("List","lastRateDateStr=" + logDate);

        List<String> list1 = new ArrayList<String>();
        for (int i = 1; i < 100; i++) {
            list1.add("item" + i);
        }

        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, waitdata);
        setListAdapter(adapter);

        Thread t = new Thread(this);
        t.start();

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    List<String> list2 = (List<String>) msg.obj;

                    ListAdapter adapter = new ArrayAdapter<String>(RateListActivity.this, android.R.layout.simple_list_item_1, list2);
                    setListAdapter(adapter);
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void run() {
        //获取网络数据，放到list中，带回主线程
        List<String> retList = new ArrayList<>();
        String curDateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
        Log.i("run","curDateStr:" + curDateStr + " logDate:" + logDate);


        if(curDateStr.equals(logDate)){
            //如果相等，则不从网络中获取数据
            Log.i("run","日期相等，从数据库中获取数据");
            RateManager manager=new RateManager(this);
           for(RateItem item:manager.listAll()){
               retList.add(item.getCurName() + "-->" + item.getCurRate());
           }
        }
        else {
            Log.i("run","日期不相等，从网络中获取在线数据");
            Document doc = null;//把url路径里获取doc对象
            try {
                Thread.sleep(3000);
                doc = Jsoup.connect("https://www.usd-cny.com/bankofchina.htm").get();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            //doc=Jsoup.parse(html);也可以直接把之前获取的html源文件作为对象
            Log.i("getNetSource", "run: " + doc.title());
            Elements tables = doc.getElementsByTag("table");//获取html里所有table
            Element table = tables.get(0);//需要的汇率信息就在第一个table里
            Elements tds = table.getElementsByTag("td");//获取这个table中的所有td
            List<RateItem> rateList =new ArrayList<RateItem>();
            for (int i = 0; i < tds.size(); i = i + 6) {
                Element td0 = tds.get(i);//币种名的td
          /*  Element td1=tds.get(i+1);//币种汇买价
            Element td2=tds.get(i+2);//币种钞买价
            Element td3=tds.get(i+3);//币种汇卖价
            Element td4=tds.get(i+4);//币种钞卖价*/
                Element td5 = tds.get(i + 5);//折算价
                String str1 = td0.text();
                String val = td5.text();
                retList.add(str1 + ":" + val);
                rateList.add(new RateItem(str1,val));
            }
              RateManager manager=new RateManager(this);
              manager.deleteAll();
              manager.addAll(rateList);

            //更新记录日期
            SharedPreferences sp = getSharedPreferences("myrate", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(DATE_SP_KEY, curDateStr);
            edit.commit();
            Log.i("run","更新日期结束：" + curDateStr);

            Message msg = handler.obtainMessage(1);
            //msg.what=1;
            msg.obj = retList;//把bundle放进消息对象里
            handler.sendMessage(msg);//交由主线程处理
        }
        }


}