package wburles.uk.seriesoftubes;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class ShopFragment extends DialogFragment {

    int weapon;
    int health;
    float cash;

    Button buyBottle;
    Button buyKnife;
    Button buyGun;
    Button buyChocolate;
    Button buyMeal;
    Button payButton;

    @Override
    public void setArguments(Bundle args){
        weapon = args.getInt("weapon");
        health = args.getInt("health");
        cash = args.getFloat("cash");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container);

        buyBottle = (Button) view.findViewById(R.id.buy_bottle_button);
        buyKnife = (Button) view.findViewById(R.id.buy_knife_button);
        buyGun = (Button) view.findViewById(R.id.buy_gun_button);
        buyChocolate = (Button) view.findViewById(R.id.buy_chocolate_button);
        buyMeal = (Button) view.findViewById(R.id.buy_meal_button);
        payButton = (Button) view.findViewById(R.id.pay_button);

        final Toast notEnoughCash = Toast.makeText(view.getContext(),getString(R.string.shop_not_enough_cash),Toast.LENGTH_SHORT);
        final Toast betterWeapon = Toast.makeText(view.getContext(),getString(R.string.shop_better_weapon),Toast.LENGTH_SHORT);
        final Toast sameWeapon = Toast.makeText(view.getContext(),getString(R.string.shop_same_weapon),Toast.LENGTH_SHORT);
        final Toast alreadyFullHealth = Toast.makeText(view.getContext(),getString(R.string.shop_full_health),Toast.LENGTH_SHORT);

        buyBottle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cash >= 5){
                    if(weapon == 5){
                        sameWeapon.show();
                    } else if (weapon > 5){
                        betterWeapon.show();
                    } else {
                        weapon = 5;
                        cash -= 5;
                        buyBottle.setEnabled(false);
                    }
                } else {
                    notEnoughCash.show();
                }
            }
        });
        buyKnife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cash >= 10){
                    if(weapon == 10){
                        sameWeapon.show();
                    } else if (weapon > 10){
                        betterWeapon.show();
                    } else {
                        weapon = 10;
                        cash -= 10;
                        buyKnife.setEnabled(false);
                    }
                } else {
                    notEnoughCash.show();
                }
            }
        });
        buyGun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cash >= 20){
                    if (weapon == 10){
                        sameWeapon.show();
                    } else {
                        weapon = 30;
                        cash -= 20;
                        buyGun.setEnabled(false);
                    }
                } else {
                    notEnoughCash.show();
                }
            }
        });
        buyChocolate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cash >= 4){
                    if(health == 100){
                        alreadyFullHealth.show();
                    } else if (health > 90){
                        health = 100;
                        cash -= 4;
                    } else {
                        health += 10;
                        cash -= 4;
                    }
                } else {
                    ;
                }
            }
        });
        buyMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cash >= 10){
                    if(health == 100){
                        alreadyFullHealth.show();
                    } else if (health > 70){
                        health = 100;
                        cash -= 10;
                    } else {
                        health += 30;
                        cash -= 10;
                    }
                } else {
                    notEnoughCash.show();
                }
            }
        });
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((GameActivity)getActivity()).retrieveShopItems(health, weapon, cash);
                dismiss();
            }
        });

        return view;
    }
}
