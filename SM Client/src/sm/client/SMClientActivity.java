package sm.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import sm.Client;
import sm.client.R;
import sm.client.SMClientActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class SMClientActivity extends Activity {	
	
	// objeto Client
	Client theClient;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
        /*String imagefile ="/sdcard/DSC01576.JPG";
        ImageView image = (ImageView)findViewById(R.id.imagem_video);
        Bitmap bm = BitmapFactory.decodeFile(imagefile);
        image.setImageBitmap(bm);*/
        
        
        theClient = new Client();    	
    	
        
        
        // acao do botao setup
        final Button botaoSetup = (Button) findViewById(R.id.botao_setup);
        botaoSetup.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
				final Dialog dialog = new Dialog(SMClientActivity.this);
				dialog.setContentView(R.layout.setup);
				dialog.setTitle("Configurar");
				
				//botao de confirmar do dialog setup
				Button botaoConfirmar = (Button) dialog.findViewById(R.id.botao_confirmar);
				botaoConfirmar.setOnClickListener(new View.OnClickListener() {					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						try{
							// IP e porta do servidor RTSP
							EditText servidor = (EditText)findViewById(R.id.label_servidor);
							InetAddress ServerIPAddr = InetAddress.getByName(servidor.getText().toString());
							
							EditText porta = (EditText)findViewById(R.id.label_porta);
							int RTSP_server_port = Integer.parseInt(porta.getText().toString());
							
							// arquivo a ser requisitado
							EditText arquivo = (EditText)findViewById(R.id.label_arquivo);
							theClient.VideoFileName = arquivo.getText().toString();
							
							
							// Conexao TCP com o servidor para troca de mensagens RTSP
					    	theClient.RTSPsocket = new Socket(ServerIPAddr, RTSP_server_port);
					    	
					    	// Set input and output stream filters:
					    	theClient.RTSPBufferedReader = new BufferedReader(new InputStreamReader(
					    			theClient.RTSPsocket.getInputStream()));
					    	theClient.RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(
					    			theClient.RTSPsocket.getOutputStream()));
	
					    	// init RTSP state:
					    	theClient.state = theClient.INIT;
						}
						catch(Exception e){
							//erro no setup
						}
						
						dialog.cancel();
						
					}
				});
				
				//botao de cancelar do dialog de setup
				Button botaoCancelar = (Button) dialog.findViewById(R.id.botao_cancelar);
				botaoCancelar.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						dialog.cancel();						
					}
				});
				
				dialog.show();
			}
		});
        
        
        // acao do botao de play
        final Button botaoPlay = (Button) findViewById(R.id.botao_play);
        botaoPlay.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
        
        
        // acao do botao de pause
        final Button botaoPause = (Button) findViewById(R.id.botao_pause);
        botaoPause.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
        
        
     // acao do botao teardown
        final Button botaoTeardown = (Button) findViewById(R.id.botao_teardown);
        botaoTeardown.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
        
        
    }
}