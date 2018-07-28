package navigate.uploader.navigateinsideuploader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import navigate.uploader.navigateinsideuploader.R;

public class AddStuff extends AppCompatActivity {
    
    private Button add, relate, addroom;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stuff);

        initView();
    }

    private void initView(){

        addroom = (Button) findViewById(R.id.addRoom_btn);
        add = (Button) findViewById(R.id.addnode);
        relate = (Button) findViewById(R.id.relaetnode);

        addroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(AddStuff.this, AddRoomActivity.class);
                startActivity(intent);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddStuff.this, AddNodeActivity.class);
                startActivity(intent);
            }
        });
        relate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddStuff.this, ADNEBERACtivity.class);
                startActivity(intent);
            }
        });
    }



}

