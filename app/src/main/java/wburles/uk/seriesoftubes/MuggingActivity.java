package wburles.uk.seriesoftubes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MuggingActivity extends AppCompatActivity{

    Intent intent;
    Mugger mugger;

    int health;
    float cash;
    int weapon;
    int boost;
    int boostHits;

    TextView muggerInfo;
    TextView playerInfo;
    Button buttonRun;
    Button buttonFight;
    Button buttonBoost;
    TextView textRun;
    TextView textFight;
    TextView textBoost;

    String weapStr;

    public void getArguments(Intent args) {
        weapon = args.getIntExtra("weapon", 0);
        health = args.getIntExtra("health", 1);
        cash = args.getFloatExtra("cash", 1);
        boost = args.getIntExtra("boost", 0);
        mugger = args.getParcelableExtra("mugger");
        Log.d("Maps","MuggingActvity running: "+health+" "+weapon+" "+cash+" "+boost);
    }

    public void update(){
        muggerInfo.setText(getString(R.string.mugger_info_1)+mugger.getHealth()+
                getString(R.string.mugger_info_2)+mugger.getWeapon()+
                getString(R.string.mugger_info_3)+mugger.getCost());
        playerInfo.setText(getString(R.string.player_info_1)+health+
                getString(R.string.player_info_2)+weapon+
                getString(R.string.player_info_3)+cash);
        textFight.setText(getString(R.string.mugging_attack)+weapStr);
        buttonBoost.setText(getString(R.string.use_damage_boost)+ "("+boost+")");
        if(boostHits <= 0){
            textBoost.setText(getString(R.string.boost_1)+boost+getString(R.string.boost_2));
        } else {
            textBoost.setText(boostHits+getString(R.string.boost_3));
        }
    }

    public void endMugging(int result){
        intent = new Intent();
        intent.putExtra("weapon",weapon);
        intent.putExtra("health",health);
        intent.putExtra("cash",cash);
        intent.putExtra("boost",boost);
        setResult(result, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mugging);
        //this.setTitle(R.string.new_task_title);
        intent = getIntent();
        getArguments(intent);

        mugger = new Mugger();

        muggerInfo = (TextView) findViewById(R.id.text_mug_mugger);
        playerInfo = (TextView) findViewById(R.id.text_mug_player);
        buttonRun = (Button) findViewById(R.id.button_mug_run);
        buttonFight = (Button) findViewById(R.id.button_mug_fight);
        buttonBoost = (Button) findViewById(R.id.button_mug_boost);
        textRun = (TextView) findViewById(R.id.text_mug_run);
        textFight = (TextView) findViewById(R.id.text_mug_fight);
        textBoost = (TextView) findViewById(R.id.text_mug_boost);

        textRun.setText(getString(R.string.run_1)+mugger.getCost()+getString(R.string.run_2));

        textFight.setText(getString(R.string.need_weapon));
        if(weapon == 0){
            buttonFight.setEnabled(false);
        } else {
            buttonFight.setEnabled(true);
            if(weapon == 5){
                weapStr = getString(R.string.weap_bottle);
            } else if(weapon == 10){
                weapStr = getString(R.string.weap_knife);
            } else if(weapon == 30){
                weapStr = getString(R.string.weap_gun);
            }
        }
        if(boost > 0){
            buttonBoost.setEnabled(true);
        }
        update();
        if(cash < mugger.getCost() && weapon == 0){
            Toast.makeText(getBaseContext(),getString(R.string.no_cash_killed),Toast.LENGTH_LONG).show();
            endMugging(GameActivity.RESULT_DEAD);
        }
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mugger.getCost() <= cash){
                    cash -= mugger.getCost();
                    Toast.makeText(view.getContext(),getString(R.string.got_away),Toast.LENGTH_LONG).show();
                    endMugging(GameActivity.RESULT_WON);
                } else {
                    Toast.makeText(view.getContext(),getString(R.string.must_fight),Toast.LENGTH_LONG).show();
                    buttonRun.setEnabled(false);
                }
            }
        });
        buttonFight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                health -= mugger.getWeapon();
                mugger.hit(weapon);
                if(boostHits > 0){
                    boostHits -= 1;
                    mugger.hit(weapon / 2);
                }
                if(health <= 0){
                    Toast.makeText(view.getContext(), getString(R.string.killed_by_mugger),Toast.LENGTH_LONG).show();
                    endMugging(GameActivity.RESULT_DEAD);
                } else if(mugger.getHealth() <= 0){
                    Random rand = new Random();
                    int earnings = rand.nextInt(14) + 1;
                    cash += earnings;
                    Toast.makeText(view.getContext(), getString(R.string.killed_the_mugger) +earnings,Toast.LENGTH_LONG).show();
                    cash += rand.nextInt(15)+1;
                    boost += 1;
                    endMugging(GameActivity.RESULT_WON);
                }
                update();
            }
        });
        buttonBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boost -= 1;
                boostHits = 3;
                if(boost == 0){
                    buttonBoost.setEnabled(false);
                }
                update();
            }
        });
    }
}
