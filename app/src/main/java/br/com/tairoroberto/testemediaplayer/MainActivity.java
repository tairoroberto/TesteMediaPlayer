package br.com.tairoroberto.testemediaplayer;

import android.annotation.TargetApi;
import android.content.pm.FeatureGroupInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnPreparedListener,
                                                               MediaPlayer.OnSeekCompleteListener,
                                                               MediaPlayer.OnCompletionListener,
                                                               MediaPlayer.OnBufferingUpdateListener,
                                                               MediaPlayer.OnErrorListener{
    private MediaPlayer player;
    private MediaPlayer nextPlayer;
    private VideoView videoPlayer;
    private TextView txtTime;
    private long currentTime;
    private long duration;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTime = (TextView )findViewById(R.id.txtTime);

        //verify if has an instance saved
        if (savedInstanceState != null){
            duration = savedInstanceState.getLong("duration");
            currentTime = savedInstanceState.getLong("currentTime");
            isPlaying = savedInstanceState.getBoolean("isPlaying");

            if (isPlaying == true){
                playMusic(null);
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null){
            duration = player.getDuration();
            currentTime = player.getCurrentPosition();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Verify if player is null, to release resources
        if (player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("duration",duration);
        outState.putLong("currentTime",currentTime);
        outState.putBoolean("isPlaying",isPlaying);
    }

    /********************************************************************************************/
    /**                                  methods to play mediaplayer                            */
    /********************************************************************************************/
    public void playMusic(View view){
        if(player == null){

            try {
                //get music from raw folder
                /*player = MediaPlayer.create(MainActivity.this,R.raw.linkin_park_figure_09);
                player.start();
                Uri uriRaw = Uri.parse("android.resource://"+getPackageName()+"/raw/linkin_park_figure_09");
                acessMetadata(uriRaw);*/

               /* //get music from SDCARD
                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard,"Music/03 - Red Hot Chili Peppers - Scar Tissue.mp3");
                player = new MediaPlayer();
                player.setDataSource(file.getAbsolutePath().toString());
                //call the prepareSync to Syncronize file in backbround thread
                player.prepareAsync();
                //get metadata and image from music
                acessMetadata(file.getAbsolutePath().toString());*/


                //get next music from SDCARD
                File sdcardNext = Environment.getExternalStorageDirectory();
                File fileNext = new File(sdcardNext,"Music/Red Hot Chili Peppers - Soul To Squeeze.mp3");
                nextPlayer = new MediaPlayer();
                nextPlayer.setDataSource(fileNext.getAbsolutePath().toString());
                //call the prepareSync to Syncronize file in backbround thread
                nextPlayer.prepareAsync();
                //get metadata and image from music
                acessMetadata(fileNext.getAbsolutePath().toString());


                //get music from URL
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource("http://www.tairoroberto.kinghost.net/packages/teste_webservice/02 Scar Tissue.mp3");
                //call the prepareSync to Syncronize file in backbround thread
                player.prepareAsync();
                //get metadata and image from music
                acessMetadata("http://www.tairoroberto.kinghost.net/packages/teste_webservice/02 Scar Tissue.mp3");


                //get Video from URL
                videoPlayer = (VideoView)findViewById(R.id.videoView);
                videoPlayer.setVideoURI(Uri.parse("http://www.tairoroberto.kinghost.net/packages/teste_webservice/Red_Hot_Chili_Peppers-Dani_California.3gp"));
                videoPlayer.setMediaController(new MediaController(MainActivity.this));
                videoPlayer.start();
                acessMetadata("http://www.tairoroberto.kinghost.net/packages/teste_webservice/Red_Hot_Chili_Peppers-Dani_California.3gp");

               /* //get music from ASSETS
                AssetFileDescriptor asset = getAssets().openFd("Musics/Scorpions - Cause I love you _ (www.sat98.in) .mp3");
                player = new MediaPlayer();
                player.setDataSource(asset.getFileDescriptor(),asset.getStartOffset(),asset.getLength());
                //call the prepareSync to Syncronize file in backbround thread
                player.prepareAsync();
                //get metadata and image from music
                acessMetadata(asset);*/

                //set the listeners to capture the events
                player.setOnPreparedListener(this);
                player.setOnBufferingUpdateListener(this);
                player.setOnErrorListener(this);
                player.setOnSeekCompleteListener(this);
                player.setOnCompletionListener(this);

            } catch (IllegalStateException e) { e.printStackTrace(); }
              catch (IOException e) {e.printStackTrace();}

        }else{
            //Play music
            player.start();
            //set isplaying to true
            isPlaying = true;
            //Update the TextView
            updateTimeMusicThread(player, txtTime);
        }
    }
    public void pauseMusic(View view){
        //set isplaying to false
        isPlaying = false;

        //Verify if player is null, to release resources
        if (player != null){
            player.pause();
        }
    }

    public void stopMusic(View view){
        //set isplaying to false
        isPlaying = false;

        //Verify if player is null, to release resources
        if (player != null){
            player.stop();
            player.release();
            player = null;
            currentTime = 0;
            txtTime.setText("");
        }
    }

    //method to update textview with music time
    public void updateTimeMusicThread(final long duration, final long currentTime,final TextView textView){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long aux;
                int minute, second;

                //calcule duration
                aux = duration/1000;
                minute = (int)(aux / 60);
                second = (int)(aux % 60);
                String stringDuration = minute < 10 ? "0"+minute : ""+minute;
                stringDuration += ":" +(second < 10 ? "0"+second : second);

                //calcule currentTime
                aux = currentTime/1000;
                minute = (int)(aux / 60);
                second = (int)(aux % 60);
                String stringCurrentTime = minute < 10 ? "0"+minute : ""+minute;
                stringCurrentTime += ":" +(second < 10 ? "0"+second : second);

                textView.setText(stringDuration+" / "+stringCurrentTime);
            }
        });
    }


    //method to set textview with music time
    public void updateTimeMusicThread(final MediaPlayer mediaPlayer,final TextView view){
        new Thread(){
            public void run(){
                while (isPlaying){
                    try {
                        updateTimeMusicThread(mediaPlayer.getDuration(),mediaPlayer.getCurrentPosition(),view);
                        Thread.sleep(100);
                    }catch (IllegalStateException e){ e.printStackTrace();}
                    catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }.start();
    }
    /********************************************************************************************/
    /**                     Listeners to get events fom mediaplayer                             */
    /********************************************************************************************/
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //onBufferingUpdate is call every time, so not need call him
        //Log.i("Script","onBufferingUpdate(): percent: "+percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("Script","onCompletion()");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("Script","onError()");
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i("Script","onPrepared()");
        //set isPlaying to true;
        isPlaying = true;
        //start music when prepared is done
        mp.start();

        //set loop with true to music continue playing after finish
        //mp.setLooping(true);

        //set next music that will be play
        mp.setNextMediaPlayer(nextPlayer);
        //set seek to currentTime
        mp.seekTo((int) currentTime);
        //call method to update textView
        updateTimeMusicThread(mp,txtTime);

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.i("Script","onSeekComplete()");
    }

    /********************************************************************************************/
    /**                           Methods to acess music metadata                              */
    /******************************************************************************************/

    public void acessMetadata(String link){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        //verify the android version for prevent bug
        if(Build.VERSION.SDK_INT >= 14){
            retriever.setDataSource(link,new HashMap<String, String>());
        }else{
            retriever.setDataSource(link);
        }
        showMetaData(retriever);
    }

    public void acessMetadata(Uri uriRaw) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(MainActivity.this,uriRaw);
        showMetaData(retriever);
    }

    public void acessMetadata(AssetFileDescriptor asset) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(asset.getFileDescriptor(),asset.getStartOffset(),asset.getLength());
        showMetaData(retriever);
    }

    public void acessMetadata(File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(file.getAbsolutePath().toString());
        showMetaData(retriever);
    }

    private void showMetaData(MediaMetadataRetriever retriever) {
        //verify if metadataRetriver is null
        if (retriever != null){
            Log.i("Script","METADATA_KEY_ALBUM "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            Log.i("Script","METADATA_KEY_ALBUMARTIST "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
            Log.i("Script","METADATA_KEY_ARTIST "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            Log.i("Script","METADATA_KEY_AUTHOR "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR));
            Log.i("Script","METADATA_KEY_BITRATE "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            Log.i("Script","METADATA_KEY_CD_TRACK_NUMBER "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
            Log.i("Script","METADATA_KEY_COMPILATION "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION));
            Log.i("Script","METADATA_KEY_COMPOSER "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER));
            Log.i("Script","METADATA_KEY_DATE "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));
            Log.i("Script","METADATA_KEY_DISC_NUMBER "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER));
            Log.i("Script","METADATA_KEY_DURATION "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            Log.i("Script","METADATA_KEY_GENRE "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            Log.i("Script","METADATA_KEY_HAS_VIDEO "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO));
            Log.i("Script","METADATA_KEY_LOCATION "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION));
            Log.i("Script","METADATA_KEY_MIMETYPE "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
            Log.i("Script","METADATA_KEY_NUM_TRACKS "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS));
            Log.i("Script","METADATA_KEY_TITLE "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            Log.i("Script","METADATA_KEY_YEAR "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR));
            Log.i("Script","METADATA_KEY_VIDEO_HEIGHT "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            Log.i("Script","METADATA_KEY_VIDEO_ROTATION "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            Log.i("Script","METADATA_KEY_VIDEO_WIDTH "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            Log.i("Script","METADATA_KEY_WRITER "+retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER));

            //get byte array with picture
            byte[] imgBytes = retriever.getEmbeddedPicture();
            Bitmap bitmap;

            //verify id imgBytes is null
            if(imgBytes != null){
                //get the image from imgBytes
                bitmap = BitmapFactory.decodeByteArray(imgBytes,0,imgBytes.length);
                ImageView imageView = (ImageView)findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            }

            bitmap = retriever.getFrameAtTime(14000,MediaMetadataRetriever.OPTION_CLOSEST);
            if (bitmap != null){
                ImageView imageFrame = (ImageView)findViewById(R.id.imageFrame);
                imageFrame.setImageBitmap(bitmap);
            }
        }
    }
}
