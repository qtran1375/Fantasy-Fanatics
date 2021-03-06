package psu.ajm6684.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class gamesimulator extends AppCompatActivity {
    MediaPlayer player;
    Integer team1Score;
    Integer team2Score;

    TextView team1ScoreView;
    TextView team2ScoreView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Query team = db.collectionGroup("Teams");
    Random r;
    Handler handler8;
    int possession = 0; //Even means team1 has the ball, Odd means team2 has the ball
    int eventType;
    String event;

    Button playGame;
    TextView gameFeed;

    String teamName1;
    String Guard1;
    String ForwardGuard1;
    String GuardForward1;
    String ForwardCenter1;
    String Center1;

    String teamName2;
    String Guard2;
    String ForwardGuard2;
    String GuardForward2;
    String ForwardCenter2;
    String Center2;
    Button backButton;


    //for dark mode
    DocumentReference mode;
    FirebaseUser current;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    //


    @Override
    public void onBackPressed() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamesimulator);
        player = MediaPlayer.create(gamesimulator.this, R.raw.intro);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        backButton = (Button) findViewById(R.id.backButton);

        stopService(new Intent(this, MyService.class));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MediaPlayer mp = MediaPlayer.create(gamesimulator.this, R.raw.backboardshot);
                mp.start();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent confirmPage = new Intent(gamesimulator.this, MyTeamsPage.class);
                        confirmPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startService(new Intent(gamesimulator.this, MyService.class));
                        startActivity(confirmPage);
                        finish();

                    }
                }, 600);


            }
        });

        team1ScoreView = (TextView) findViewById(R.id.team1Score);
        team2ScoreView = (TextView) findViewById(R.id.team2Score);

        playGame = (Button) findViewById(R.id.rungame);
        gameFeed = (TextView) findViewById(R.id.dataFeed);
        gameFeed.setMovementMethod(new ScrollingMovementMethod());


        team1Score = 0;
        team2Score = 0;


        //for dark mode
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        current = firebaseAuth.getCurrentUser();
        mode = firestore.collection("Users").document(current.getUid());

        mode.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> list = new ArrayList<>();

                        Map<String, Object> map = document.getData();
                        if (map != null) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                list.add(entry.getValue().toString());
                            }
                        }


                        for (String s : list) {

                            if (s.equals("light")) {

                                getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_NO));
                                break;
                            }

                            if (s.equals("dark")) {
                                getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_YES));
                                break;
                            }

                        }
                    }
                }
            }
        });

        Intent intent = getIntent();

        teamName1 = intent.getStringExtra("Team1").trim();


        teamName2 = intent.getStringExtra("Team2").trim();


        db.collectionGroup("Teams").whereEqualTo("TeamName", teamName1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //  Teams team = document.toObject(Teams.class);

                        String teamName = document.get("TeamName").toString();
//                        Toast.makeText(gamesimulator.this, teamName, Toast.LENGTH_SHORT).show();
                        Guard1 = document.get("Guard").toString();
                        ForwardGuard1 = document.get("ForwardGuard").toString();
                        GuardForward1 = document.get("GuardForward").toString();
                        ForwardCenter1 = document.get("ForwardCenter").toString();
                        Center1 = document.get("Center").toString();
//                        Toast.makeText(gamesimulator.this, "Guard " + Guard1, Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Log.d("Error", task.getException().toString());

                }


            }
        });


        db.collectionGroup("Teams").whereEqualTo("TeamName", teamName2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //  Teams team = document.toObject(Teams.class);

//                                    String teamName = document.get("TeamName").toString();
                        Guard2 = document.get("Guard").toString();
                        ForwardGuard2 = document.get("ForwardGuard").toString();
                        GuardForward2 = document.get("GuardForward").toString();
                        ForwardCenter2 = document.get("ForwardCenter").toString();
                        Center2 = document.get("Center").toString();
//                        Toast.makeText(gamesimulator.this, "Guard 2 " + Guard2, Toast.LENGTH_SHORT).show();


                    }


                } else {

                    Log.d("Error", task.getException().toString());

                }


            }
        });


        playGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                playGame.setAlpha(.5f);
                playGame.setClickable(false);

//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Do something after 100ms
                play(gamesimulator.this);

                tipOff();
//                    }
//                }, 10000);


                final Handler handler22 = new Handler();
                handler22.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms

                        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                            //   Toast.makeText(gamesimulator.this, "The First Quarter Is Now Underway!", Toast.LENGTH_SHORT).show();

//                            player = MediaPlayer.create(gamesimulator.this, R.raw.buzzer);
                            play(gamesimulator.this);
                            firstQ();
                        }


                    }
                }, 15000);


                final Handler handler23 = new Handler();
                handler23.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                            secondQ();
                        }
                    }
                }, 50000);


                final Handler handler24 = new Handler();
                handler24.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                            halftime();
                        }
                    }
                }, 85000);


                final Handler handler25 = new Handler();
                handler25.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                            thirdQ();

                        }
                    }
                }, 120000);


                final Handler handler26 = new Handler();
                handler26.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                            fourthQ();
                        }
                    }
                }, 155000);

                final Handler handler27 = new Handler();
                handler27.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                            endGame();
                        }
                    }
                }, 190000);
//


            }
        });


    }

    private void tipOff() {

        gameFeed.append("Welcome to Today's game!\n");
        gameFeed.append("\nToday's match up is between....\n"
                + teamName1 + " and " + teamName2 + "\n");

        gameFeed.append("\n" + teamName1 + "'s" + " Starting Five! \n");

        gameFeed.append("\nStarting at the Guard Position\n" + Guard1 + "!\n");
        gameFeed.append("\nStarting at the Shooting Guard Position\n" + GuardForward1 + "!\n");
        gameFeed.append("\nStarting at the Small Forward Position\n" + ForwardGuard1 + "!\n");
        gameFeed.append("\nStarting at the Power Forward Position\n" + ForwardCenter1 + "!\n");
        gameFeed.append("\nStarting at the Center Position\n" + Center1 + "!\n");


        gameFeed.append("\n\n\n\n" + teamName2 + "'s" + " Starting Five! \n");

        gameFeed.append("\nStarting at the Guard Position\n" + Guard2 + "!\n");
        gameFeed.append("\nStarting at the Shooting Guard Position\n" + GuardForward2 + "!\n");
        gameFeed.append("\nStarting at the Small Forward Position\n" + ForwardGuard2 + "!\n");
        gameFeed.append("\nStarting at the Power Forward Position\n" + ForwardCenter2 + "!\n");
        gameFeed.append("\nStarting at the Center Position\n" + Center2 + "!\n");


        gameFeed.append("\nLets get ready for Tip off!\n\n");
        gameFeed.append("\n15 seconds.......\n\n");


    }

    private void firstQ() {
        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
            Toast.makeText(gamesimulator.this, "The First Quarter Is Now Underway!", Toast.LENGTH_SHORT).show();

        }

        gameFeed.append("\nGet Ready.......\n");


        int tip = (int) Math.round(Math.random());

        if ((tip % 2) == 0) {

            gameFeed.append("\n" + teamName1 + " Won the tip!\n");

            possession = 0;

        } else {

            gameFeed.append("\n" + teamName2 + " Won the tip!\n");
            possession = 1;

        }


        //for(int x = 0; x < 15; x++) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 2000);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 4000);

        Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 6000);

        Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 8000);


        Handler handler5 = new Handler();
        handler5.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 10000);

        Handler handler6 = new Handler();
        handler6.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 12000);

        Handler handler7 = new Handler();
        handler7.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 14000);


        Handler handler8 = new Handler();
        handler8.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 16000);

        Handler handler9 = new Handler();
        handler9.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 18000);

        Handler handler10 = new Handler();
        handler10.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 20000);

        Handler handler11 = new Handler();
        handler11.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 22000);

        Handler handler12 = new Handler();
        handler12.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 24000);

        Handler handler13 = new Handler();
        handler13.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 26000);

        Handler handler14 = new Handler();
        handler14.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 28000);


        Handler handler15 = new Handler();
        handler15.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 30000);

        Handler handler16 = new Handler();
        handler16.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                    player = MediaPlayer.create(gamesimulator.this, R.raw.buzzer);
                    play(gamesimulator.this);

                    Toast.makeText(gamesimulator.this, "End of the First Quarter", Toast.LENGTH_SHORT).show();
                }
                gameFeed.append("\n That Concludes the First Quarter\n");

            }
        }, 30000);


    }

    private void eventFucntion() {


        r = new Random();

        eventType = r.nextInt(15);


        switch (eventType) {

            case 0:
                if (possession % 2 == 0) {

                    event = teamName1 + " Scored!";

                    team1Score = team1Score + 2;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " Scored!";

                    team2Score = team2Score + 2;
                    team2ScoreView.setText(team2Score.toString());

                }

                possession++;

                break;

            case 1:

                if (possession % 2 == 0) {

                    event = teamName2 + " stole the ball from " + teamName1;

                } else {

                    event = teamName1 + " stole the ball from " + teamName2;


                }

                possession++;
                break;

            case 2:
                if (possession % 2 == 0) {

                    event = teamName1 + " Scored!";

                    team1Score = team1Score + 2;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " Scored!";

                    team2Score = team2Score + 2;
                    team2ScoreView.setText(team2Score.toString());

                }

                possession++;

                break;

            case 3:
                if (possession % 2 == 0) {

                    event = teamName1 + " Missed!";


                } else {

                    event = teamName2 + " Missed!";


                }

                possession++;

                break;
            case 4:
                if (possession % 2 == 0) {

                    event = teamName2 + " committed a foul! On the Floor!";


                } else {
                    event = teamName1 + " committed a foul! On the Floor!";


                }


                break;
            case 5:

                Random s = new Random();

                int freethrows = s.nextInt(2);

                if (possession % 2 == 0) {

                    if (freethrows == 0) {

                        event = teamName1 + " was fouled on the shot attempt!\n" + "Both Free throw Missed";


                    } else if (freethrows == 1) {

                        event = teamName1 + " was fouled on the shot attempt!\n" + "1 Free throw Made";

                        team1Score = team1Score + 1;
                        team1ScoreView.setText(team1Score.toString());
                    } else if (freethrows == 2) {

                        event = teamName1 + " was fouled on the shot attempt!\n" + "Both Free throws Made";

                        team1Score = team1Score + 2;
                        team1ScoreView.setText(team1Score.toString());
                    }


                } else {
                    if (freethrows == 0) {

                        event = teamName2 + " was fouled on the shot attempt!\n" + "Both Free throw Missed";


                    } else if (freethrows == 1) {

                        event = teamName2 + " was fouled on the shot attempt!\n" + "1 Free throw Made";

                        team2Score = team2Score + 1;
                        team2ScoreView.setText(team2Score.toString());
                    } else if (freethrows == 2) {

                        event = teamName2 + " was fouled on the shot attempt!\n" + "Both Free throws Made";

                        team2Score = team2Score + 2;
                        team2ScoreView.setText(team2Score.toString());
                    }

                }
                possession++;

                break;

            case 6:
                Random r = new Random();

                int freethrows1 = r.nextInt(1);

                if (possession % 2 == 0) {
//                    event = teamName1 + " was fouled! AND1 !!!!!";

                    if (freethrows1 == 0) {

                        event = teamName1 + " was fouled! AND1 !!!!!\n"
                                + "Free Throw Missed";
                        team1Score = team1Score + 2;
                        team1ScoreView.setText(team1Score.toString());
                    } else if (freethrows1 == 1) {

                        event = teamName1 + " was fouled! AND1 !!!!!\n"
                                + "Free Throw Made";
                        team1Score = team1Score + 3;
                        team1ScoreView.setText(team1Score.toString());
                    }


                } else {
                    if (freethrows1 == 0) {

                        event = teamName2 + " was fouled! AND1 !!!!!\n"
                                + "Free Throw Missed";
                        team2Score = team2Score + 2;
                        team2ScoreView.setText(team2Score.toString());
                    } else if (freethrows1 == 1) {

                        event = teamName2 + " was fouled! AND1 !!!!!\n"
                                + "Free Throw Made";
                        team2Score = team2Score + 3;
                        team2ScoreView.setText(team2Score.toString());
                    }
                }
                possession++;

                break;
            case 7:
                if (possession % 2 == 0) {
                    event = teamName1 + " made a 3!";

                    team1Score = team1Score + 3;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " made a 3!";

                    team2Score = team2Score + 3;
                    team2ScoreView.setText(team2Score.toString());

                }
                possession++;

                break;
            case 8:
                if (possession % 2 == 0) {
                    event = teamName1 + " missed a shot! Offensive rebound!";


                } else {
                    event = teamName2 + " missed a shot! Offensive rebound!";


                }
                break;
            case 9:
                if (possession % 2 == 0) {

                    event = teamName1 + " Scored!";

                    team1Score = team1Score + 2;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " Scored!";

                    team2Score = team2Score + 2;
                    team2ScoreView.setText(team2Score.toString());

                }

                possession++;

                break;
            case 10:
                if (possession % 2 == 0) {

                    event = teamName1 + " Scored!";

                    team1Score = team1Score + 2;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " Scored!";

                    team2Score = team2Score + 2;
                    team2ScoreView.setText(team2Score.toString());

                }

                possession++;

                break;
            case 11:
                if (possession % 2 == 0) {

                    event = teamName1 + " Scored!";

                    team1Score = team1Score + 2;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " Scored!";

                    team2Score = team2Score + 2;
                    team2ScoreView.setText(team2Score.toString());

                }

                possession++;

                break;
            case 12:
                if (possession % 2 == 0) {

                    event = teamName1 + " Missed!";


                } else {

                    event = teamName2 + " Missed!";


                }

                possession++;

                break;
            case 13:
                if (possession % 2 == 0) {
                    event = teamName1 + " made a 3!";

                    team1Score = team1Score + 3;
                    team1ScoreView.setText(team1Score.toString());
                } else {
                    event = teamName2 + " made a 3!";

                    team2Score = team2Score + 3;
                    team2ScoreView.setText(team2Score.toString());

                }
                possession++;

                break;
            case 14:
                if (possession % 2 == 0) {

                    event = teamName1 + " Missed!";


                } else {

                    event = teamName2 + " Missed!";


                }

                possession++;

                break;
            case 15:
                if (possession % 2 == 0) {

                    event = teamName1 + " Missed!";


                } else {

                    event = teamName2 + " Missed!";


                }

                possession++;

                break;

            default:
                event = "Test Error";


        }


        gameFeed.append("\n" + event + "\n");


    }


    private void secondQ() {
        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
            Toast.makeText(gamesimulator.this, "Start of the Second Quarter", Toast.LENGTH_SHORT).show();
        }


        gameFeed.append("\nGet Ready.......\n");


        //for(int x = 0; x < 15; x++) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 2000);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 4000);

        Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 6000);

        Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 8000);


        Handler handler5 = new Handler();
        handler5.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 10000);

        Handler handler6 = new Handler();
        handler6.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 12000);

        Handler handler7 = new Handler();
        handler7.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 14000);


        Handler handler8 = new Handler();
        handler8.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 16000);

        Handler handler9 = new Handler();
        handler9.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 18000);

        Handler handler10 = new Handler();
        handler10.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 20000);

        Handler handler11 = new Handler();
        handler11.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 22000);

        Handler handler12 = new Handler();
        handler12.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 24000);

        Handler handler13 = new Handler();
        handler13.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 26000);

        Handler handler14 = new Handler();
        handler14.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 28000);


        Handler handler15 = new Handler();
        handler15.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 30000);

        Handler handler16 = new Handler();
        handler16.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                    play(gamesimulator.this);

                    Toast.makeText(gamesimulator.this, "End of the Second Quarter", Toast.LENGTH_SHORT).show();
                }
                gameFeed.append("\n That Concludes the Second Quarter\n");

            }
        }, 30000);
    }

    private void halftime() {


        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
            Toast.makeText(gamesimulator.this, "Halftime", Toast.LENGTH_SHORT).show();

        }

    }


    private void thirdQ() {

        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
            Toast.makeText(gamesimulator.this, "Start of the Third Quarter", Toast.LENGTH_SHORT).show();

        }


        gameFeed.append("\nGet Ready.......\n");


        int tip = (int) Math.round(Math.random());


        //for(int x = 0; x < 15; x++) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 2000);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 4000);

        Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 6000);

        Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 8000);


        Handler handler5 = new Handler();
        handler5.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 10000);

        Handler handler6 = new Handler();
        handler6.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 12000);

        Handler handler7 = new Handler();
        handler7.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 14000);


        Handler handler8 = new Handler();
        handler8.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 16000);

        Handler handler9 = new Handler();
        handler9.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 18000);

        Handler handler10 = new Handler();
        handler10.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 20000);

        Handler handler11 = new Handler();
        handler11.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 22000);

        Handler handler12 = new Handler();
        handler12.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 24000);

        Handler handler13 = new Handler();
        handler13.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 26000);

        Handler handler14 = new Handler();
        handler14.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 28000);


        Handler handler15 = new Handler();
        handler15.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 30000);

        Handler handler16 = new Handler();
        handler16.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                    play(gamesimulator.this);

                    Toast.makeText(gamesimulator.this, "End of the Third Quarter", Toast.LENGTH_SHORT).show();
                }
                gameFeed.append("\n That Concludes the Third Quarter\n");

            }
        }, 30000);

    }

    private void fourthQ() {
        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
            Toast.makeText(gamesimulator.this, "Start of the Fourth Quarter", Toast.LENGTH_SHORT).show();

        }

        gameFeed.append("\nGet Ready.......\n");


        //for(int x = 0; x < 15; x++) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 2000);

        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 4000);

        Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 6000);

        Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 8000);


        Handler handler5 = new Handler();
        handler5.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 10000);

        Handler handler6 = new Handler();
        handler6.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 12000);

        Handler handler7 = new Handler();
        handler7.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 14000);


        Handler handler8 = new Handler();
        handler8.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 16000);

        Handler handler9 = new Handler();
        handler9.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 18000);

        Handler handler10 = new Handler();
        handler10.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 20000);

        Handler handler11 = new Handler();
        handler11.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 22000);

        Handler handler12 = new Handler();
        handler12.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 24000);

        Handler handler13 = new Handler();
        handler13.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 26000);

        Handler handler14 = new Handler();
        handler14.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 28000);


        Handler handler15 = new Handler();
        handler15.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                eventFucntion();


            }
        }, 30000);

        Handler handler16 = new Handler();
        handler16.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
                    play(gamesimulator.this);

                    Toast.makeText(gamesimulator.this, "End of the Fourth Quarter", Toast.LENGTH_SHORT).show();
                }
                gameFeed.append("\n That Concludes the Fourth Quarter\n");

            }
        }, 30000);
    }

    private void endGame() {

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Scores");

        String Match = teamName1 + " vs. " + teamName2;
        String Score;


        if (gamesimulator.this.getWindow().getDecorView().getRootView().isShown() == true) {
            Toast.makeText(gamesimulator.this, "BUZZZER SOUNDS!!! THATS IT!!!", Toast.LENGTH_SHORT).show();
        }

            if (team1Score > team2Score) {


                gameFeed.append("\nFinal Score\n");


                gameFeed.append("\n" + team1Score + " - " + team2Score + "\n");
                gameFeed.append("\n" + teamName1 + " Wins!!!!!!!" + "\n");


                Score = team1Score + " - " + team2Score;

            } else if (team1Score < team2Score) {


                gameFeed.append("\nFinal Score\n");


                gameFeed.append("\n" + team1Score + " - " + team2Score + "\n");
                gameFeed.append("\n" + teamName2 + " Wins!!!!!!!" + "\n");
                Score = team1Score + " - " + team2Score;


            } else {

                gameFeed.append("\nFinal Score\n");


                gameFeed.append("\n" + team1Score + " - " + team2Score + "\n");
                gameFeed.append("\n" + "Its a TIE!!!!!!!" + "\n");

                Score = team1Score + " - " + team2Score;



            }

        collectionReference.add(new ScoreModel(Match,Score));



        takeScreenshot();


    }

    public void back2Teams(View view) {


        final MediaPlayer mp = MediaPlayer.create(gamesimulator.this, R.raw.singledribble);
        mp.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);


    }

    public void play(gamesimulator v) {
        if (player == null) {
//            player = MediaPlayer.create(this, R.raw.song);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer();
                }
            });
        }

        player.start();
    }

    public void pause(View v) {
        if (player != null) {
            player.pause();
        }
    }

    public void stop(View v) {
        stopPlayer();
    }

    private void stopPlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
    }
    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            Random rn = new Random();

            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();


        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }


}
