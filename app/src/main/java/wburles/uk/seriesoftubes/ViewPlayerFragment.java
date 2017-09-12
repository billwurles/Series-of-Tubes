package wburles.uk.seriesoftubes;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewPlayerFragment extends DialogFragment{


    TextView textHealth;
    TextView textWeapon;
    TextView textCash;
    TextView textBoost;

    ImageView imgWeapon;
    ImageView imgBoost;

    int weapon;
    int health;
    float cash;
    int boost;

    @Override
    public void setArguments(Bundle args){
        health = args.getInt("health");
        weapon = args.getInt("weapon");
        cash = args.getFloat("cash");
        boost = args.getInt("boost");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_player, container);

        textWeapon = (TextView) view.findViewById(R.id.text_weapon);
        textHealth = (TextView) view.findViewById(R.id.text_health);
        textCash = (TextView) view.findViewById(R.id.text_cash);
        textBoost = (TextView) view.findViewById(R.id.text_boost);
        imgWeapon = (ImageView) view.findViewById(R.id.image_weapon);
        imgBoost = (ImageView) view.findViewById(R.id.image_boost);

        String weapStr = getString(R.string.weap_null);
        if(weapon == 5){
            weapStr = getString(R.string.weap_bottle);
            imgWeapon.setImageResource(R.drawable.bottle);
        } else if(weapon == 10){
            weapStr = getString(R.string.weap_knife);
            imgWeapon.setImageResource(R.drawable.knife);
        } else if(weapon == 30){
            weapStr = getString(R.string.weap_gun);
            imgWeapon.setImageResource(R.drawable.gun);
        }
        if(boost > 0){
            imgBoost.setImageResource(R.drawable.boost);
        }
        textHealth.setText(health+getString(R.string.hp));
        textWeapon.setText(weapStr);
        textCash.setText(getString(R.string.pound)+cash);
        textBoost.setText(boost+" "+getString(R.string.damage_boosters));

        return view;
    }
}
