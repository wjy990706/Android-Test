package com.swufe.moneyexchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyListActivity extends ListActivity implements Runnable,AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    Handler handler;
    private ArrayList<HashMap<String, String>> listItems;//存放文字和图片信息
    private SimpleAdapter listItemAdapter;//声明适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initListView();

        this.setListAdapter(listItemAdapter);

        Thread t = new Thread(this);
        t.start();

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 2) {
                    List<HashMap<String, String>> list2 = (List<HashMap<String, String>>) msg.obj;
                    listItemAdapter = new SimpleAdapter(MyListActivity.this,//当前上下文
                            list2,//adapter适配器的数据源
                            R.layout.list_item,//xml布局文件
                            new String[]{"ItemTitle", "ItemDetail"},
                            new int[]{R.id.itemTitle, R.id.itemDetail});//创建adapter，一一对应关系
                    setListAdapter(listItemAdapter);
                }
                super.handleMessage(msg);
            }
        };

        getListView().setOnItemClickListener(this);//监听列表事件，类要继承AdapterView.OnItemClickListener
        getListView().setOnItemLongClickListener(this);//长按监听，也要继承
    }

    private void initListView() {
        listItems = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemTitle", "请稍后");//键值对
            map.put("ItemDetail", "正在获取");//键值对
            listItems.add(map);//map值放入listItems中

        }
        listItemAdapter = new SimpleAdapter(this,//当前上下文
                listItems,//adapter适配器的数据源
                R.layout.list_item,//xml布局文件
                new String[]{"ItemTitle", "ItemDetail"},
                new int[]{R.id.itemTitle, R.id.itemDetail});//创建adapter，一一对应关系
    }

    public void run() {
        //获取网络数据，放到list中，带回主线程
        List<HashMap<String, String>> retList = new ArrayList<HashMap<String, String>>();


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
        for (int i = 0; i < tds.size(); i = i + 6) {
            Element td0 = tds.get(i);//币种名的td
          /*  Element td1=tds.get(i+1);//币种汇买价
            Element td2=tds.get(i+2);//币种钞买价
            Element td3=tds.get(i+3);//币种汇卖价
            Element td4=tds.get(i+4);//币种钞卖价*/
            Element td5 = tds.get(i + 5);//折算价
            String str1 = td0.text();
            String val = td5.text();

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("ItemTitle", str1);
            map.put("ItemDetail", val);


            retList.add(map);
        }


        Message msg = handler.obtainMessage(2);
        //msg.what=1;
        msg.obj = retList;//把bundle放进消息对象里
        handler.sendMessage(msg);//交由主线程处理
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)//点击列表，调用方法
    {


        TextView title=(TextView)view.findViewById(R.id.itemTitle);
        TextView detail=(TextView)view.findViewById(R.id.itemDetail);//通过点击获取view的内容
        String titleStr= String.valueOf( title.getText());
        String detailStr= String.valueOf( detail.getText());

        Log.i("onItemClick: ", "titleStr= "+titleStr);
        Log.i("onItemClick: ", "detailStr= "+detailStr);


        Intent Calculer=new Intent(this,CalculerActivity.class);
        Calculer.putExtra("title",titleStr);
        Calculer.putExtra("detail",Float.parseFloat(detailStr));//传值
        startActivity(Calculer);//打开新界面

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;//返回true，长按之后短按不会生效。返回false的话短按也会生效
    }
}