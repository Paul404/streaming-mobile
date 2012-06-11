package sm.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.Timer;

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
	
	// Variaveis RTP:
	// ----------------
	DatagramPacket rcvdp; // pacote UDP recebido do servidor
	DatagramSocket RTPsocket; // socket usado para enviar e receber pacotes UDP
	static int RTP_RCV_PORT = 25000; // porta onde o cliente vai receber os pacotes RTP

	byte[] buf; // buffer usado para armazenar dados recebidos do servidor

	
	
	// Variaveis RTSP:
	// ----------------
	
	// estados rtsp
	final static int INIT = 0;
	final static int READY = 1;
	final static int PLAYING = 2;
	static int state; // estado RTSP == INIT ou READY ou PLAYING
	Socket RTSPsocket; // socket usado para enviar/receber mensagens RTSP
	
	// filtros de fluxo de entrada e saida
	static BufferedReader RTSPBufferedReader;
	static BufferedWriter RTSPBufferedWriter;
	static String VideoFileName; // arquivo de video para solicitar ao servidor
	int RTSPSeqNb = 0; // numero de sequencia mensagens RTSP dentro da sessao
	int RTSPid = 0; // ID da sessao RTSP (obtida do servidor RTSP)

	final static String CRLF = "\r\n";

	// constantes de video:
	// ------------------
	static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
        /*String imagefile ="/sdcard/DSC01576.JPG";
        ImageView image = (ImageView)findViewById(R.id.imagem_video);
        Bitmap bm = BitmapFactory.decodeFile(imagefile);
        image.setImageBitmap(bm);*/
        
        buf = new byte[15000];   	
        
        
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
							VideoFileName = arquivo.getText().toString();
							
							
							// Conexao TCP com o servidor para troca de mensagens RTSP
					    	RTSPsocket = new Socket(ServerIPAddr, RTSP_server_port);
					    	
					    	// Set input and output stream filters:
					    	RTSPBufferedReader = new BufferedReader(new InputStreamReader(
					    			RTSPsocket.getInputStream()));
					    	RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(
					    			RTSPsocket.getOutputStream()));
	
					    	// estado inicial do RTSP:
					    	state = INIT;
					    	
					    	if (state == INIT) {
								try {
									RTPsocket = new DatagramSocket(RTP_RCV_PORT);
									RTPsocket.setSoTimeout(5); // 5 milissegundos

								} catch (SocketException se) {
									System.out.println("Socket exception: " + se);
									System.exit(0);
								}

								// inicializa o numero de sequencial RTSP
								RTSPSeqNb = 1;

								// Send SETUP message to the server
								send_RTSP_request("SETUP");

								// Wait for the response
								if (parse_server_response() != 200)
									System.out.println("Invalid Server Response");
								else {
									// change RTSP state and print new state
									// state = ....
									// System.out.println("New RTSP state: ....");
									state = READY;
									System.out.println("Novo estado RTSP: READY");
								}
							}// else if state != INIT then do nothing
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
    
    
    private void send_RTSP_request(String request_type) {
		try {
			// Use the RTSPBufferedWriter to write to the RTSP socket

			// write the request line:
			RTSPBufferedWriter.write(request_type + " " + VideoFileName
					+ " RTSP/1.0" + CRLF);

			// write the CSeq line:
			// ......
			RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);

			// check if request_type is equal to "SETUP" and in this case write
			// the Transport: line advertising to the server the port used to
			// receive the RTP packets RTP_RCV_PORT
			// if ....
			// otherwise, write the Session line from the RTSPid field
			// else ....
			if (request_type.equals("SETUP")) {
				RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= "
						+ RTP_RCV_PORT + CRLF);
			} else {
				RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
			}

			RTSPBufferedWriter.flush();

			// RTSPSeqNb++;
		} catch (Exception ex) {
			System.out.println("Exception caught: " + ex);
			System.exit(0);
		}
	}
    
    
 // ------------------------------------
 	// Parse Server Response
 	// ------------------------------------
 	private int parse_server_response() {
 		int reply_code = 0;

 		try {
 			// parse status line and extract the reply_code:
 			String StatusLine = RTSPBufferedReader.readLine();
 			// System.out.println("RTSP Client - Received from Server:");
 			System.out.println(StatusLine);

 			StringTokenizer tokens = new StringTokenizer(StatusLine);
 			tokens.nextToken(); // skip over the RTSP version
 			reply_code = Integer.parseInt(tokens.nextToken());

 			// if reply code is OK get and print the 2 other lines
 			if (reply_code == 200) {
 				String SeqNumLine = RTSPBufferedReader.readLine();
 				System.out.println(SeqNumLine);

 				String SessionLine = RTSPBufferedReader.readLine();
 				System.out.println(SessionLine);

 				// if state == INIT gets the Session Id from the SessionLine
 				tokens = new StringTokenizer(SessionLine);
 				tokens.nextToken(); // skip over the Session:
 				RTSPid = Integer.parseInt(tokens.nextToken());
 			}
 		} catch (Exception ex) {
 			System.out.println("Exception caught: " + ex);
 			System.exit(0);
 		}

 		return (reply_code);
 	}
    
    
}