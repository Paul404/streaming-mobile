package sm.server;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class SMServerActivity extends Activity {
	
	
	//final Handler mHandler = new Handler();
	public int currentFrame = 0;
	Handler refresh;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);
        
        refresh = new Handler(Looper.getMainLooper());
        
        final TextView logText = (TextView)findViewById(R.id.texto_log);
        final Button button = (Button)findViewById(R.id.botao_servidor);
        final EditText portaText = (EditText)findViewById(R.id.input_configurar_porta);
        final ScrollView scroll = (ScrollView)findViewById(R.id.scrollView1);
        
	
        log("\nO IP do servidor Ž: " + this.getIpAddress() + "\n");

        
        final SMServerActivity that = this;
        
        button.setOnClickListener(new View.OnClickListener() {
        	
        	boolean running = false;
        	
            public void onClick(View v) {
            	
            	if(!running) {
	                try {
	                	running = true;
	                	Server.starta(Integer.parseInt(portaText.getText().toString()), that);
	                }
	                catch(Exception e){
	                	running = false;
	                	//e.printStackTrace();
	                	log("Erro: " + e.getMessage() + "\n");
	                }
            	}
                
            }
        });
        
    }
    
    public void log(String mensagem) {
    	
    	TextView logText = (TextView)findViewById(R.id.texto_log);
        ScrollView scroll = (ScrollView)findViewById(R.id.scrollView1);
    	
    	logText.append(mensagem);
    	//logText.invalidate();
    	scroll.smoothScrollTo(0, logText.getBottom());
    }
    
    public String getIpAddress() {
    	WifiManager myWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int myIp = myWifiInfo.getIpAddress();
        
        return android.text.format.Formatter.formatIpAddress(myIp);
    }
    
    public void updateSendFrame() {
    	
    	refresh.post(new Runnable() {
            public void run()
            {
            	TextView currentFrameText = (TextView)findViewById(R.id.texto_frame_enviado);
            	currentFrameText.setText("Enviando frame #" + currentFrame);
            }
        });
    	
    	
    }
}