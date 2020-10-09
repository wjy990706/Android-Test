package com.swufe.moneyexchange;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {
    EditText inp1,inp2,inp3  ;
    float newdollar_rate,neweuro_rate,newwon_rate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent newActivity = getIntent();
        float dollar_rate2=newActivity.getFloatExtra("dollar_rate_key",0.0f);
        float euro_rate2=newActivity.getFloatExtra("euro_rate_key",0.0f);
        float won_rate2=newActivity.getFloatExtra("won_rate_key",0.0f);//用float对象接收传过来的汇率


        TextView text =findViewById(R.id.input1);
        text.setText(String.valueOf(dollar_rate2));//输入框显示传过来的值
        TextView text2 =findViewById(R.id.input2);
        text2.setText(String.valueOf(euro_rate2));
        TextView text3 =findViewById(R.id.input3);
        text3.setText(String.valueOf(won_rate2));

        inp1 = findViewById(R.id.input1);
        inp2 = findViewById(R.id.input2);
        inp3 = findViewById(R.id.input3);






    }

    public void save(View btn)
    {
        newdollar_rate = Float.parseFloat(inp1.getText().toString());
        neweuro_rate = Float.parseFloat(inp2.getText().toString());
        newwon_rate = Float.parseFloat(inp3.getText().toString());



        Intent newActivity=getIntent();
        Bundle bdl=new Bundle();//建立包束
        bdl.putFloat("dollar_rate_key",newdollar_rate);
        bdl.putFloat("euro_rate_key",neweuro_rate);
        bdl.putFloat("won_rate_key",newwon_rate);//把新的汇率值打包放入包束
        newActivity.putExtras(bdl);//把包束放入intent传回去

        Log.i("onActivityResult", "dollar_rate= "+newdollar_rate);
        Log.i("onActivityResult", "euro_rate= "+neweuro_rate);
        Log.i("onActivityResult", "won_rate= "+newwon_rate);

        setResult(1,newActivity);

        finish();//一定要关


    }
}